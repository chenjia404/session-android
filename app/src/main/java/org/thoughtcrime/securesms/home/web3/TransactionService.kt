package org.thoughtcrime.securesms.home.web3

import org.session.libsignal.crypto.Wallet
import org.thoughtcrime.securesms.util.Logger
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Response
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by Yaakov on
 * Describe:
 */
object TransactionService {

    fun getBalance(
        web3j: Web3j,
        address: String
    ): BigDecimal {

        return try {
            val ethCall = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            if (ethCall.hasError()) {
                Logger.e("${ethCall.error.code}: ${ethCall.error.message}")
                BigDecimal.ZERO
            } else {
                BigDecimal(ethCall.balance)
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    fun checkRpcStatus(rpc: String): Boolean {
        var send = Web3j.build(HttpService(rpc)).ethBlockNumber().send()
        return send.hasError()
    }

    fun loadGas(web3j: Web3j, chainId: Int): BigInteger {
        val response = web3j.ethGasPrice().send()
        if (response.hasError()) {
            Logger.e("${response.error.code}: ${response.error.message}")
            return BigInteger.ZERO
        }
        return response.gasPrice
    }


    fun sendTx(
        web3j: Web3j,
        wallet: Wallet,
        tx: Transaction
    ): Transaction {
        val nonce =
            web3j.ethGetTransactionCount(tx.from, DefaultBlockParameterName.LATEST)
                .send().transactionCount
        if (Numeric.toBigInt(tx.to) == BigInteger.ZERO) {
            tx.to = ""
        }
        if (tx.data.isNullOrBlank()) {
            tx.data = ""
        }
        val rawTx = RawTransaction.createTransaction(
            nonce,
            BigInteger(tx.gasPrice),
            BigInteger(tx.gas),
            if (tx.contractAddress.isNullOrBlank()) {
                tx.to
            } else {
                tx.contractAddress
            },
            BigInteger(tx.value),
            tx.data
        )
        val signMessage: ByteArray = TransactionEncoder.signMessage(
            rawTx,
            tx.chainId.toLong(),
            Credentials.create(wallet.pk)
        )
        val send = web3j.ethSendRawTransaction(Numeric.toHexString(signMessage)).send()
        if (send.hasError()) {
            tx.isError = 1
            tx.timeStamp = System.currentTimeMillis() / 1000
            Logger.e(send.error.message)
        } else {
            tx.txreceipt_status = 0
            tx.isError = 0
            tx.hash = send.transactionHash
            tx.timeStamp = System.currentTimeMillis() / 1000
            // TODO:  insert db
        }
        return tx
    }

    fun signEthereumMessage(wallet: Wallet, message: ByteArray, addPrefix: Boolean): String {
        var data = message
        if (addPrefix) {
            val messagePrefix = "\u0019Ethereum Signed Message:\n"
            val prefix = (messagePrefix + message.size).toByteArray()
            val result = ByteArray(prefix.size + message.size)
            System.arraycopy(prefix, 0, result, 0, prefix.size)
            System.arraycopy(message, 0, result, prefix.size, message.size)
            data = result
        }
        val s = String(data, Charsets.UTF_8)
        val signMessage = Sign.signMessage(
            s.toByteArray(),
            ECKeyPair.create(Numeric.toBigInt(wallet.pk))
        )
        return getSignature(signMessage)
    }


    private fun getSignature(signatureData: SignatureData): String {
        val sig = ByteArray(65)
        System.arraycopy(signatureData.r, 0, sig, 0, 32)
        System.arraycopy(signatureData.s, 0, sig, 32, 32)
        System.arraycopy(signatureData.v, 0, sig, 64, signatureData.v.size)
        return Numeric.toHexString(sig)
    }

    private fun Response<String>.parseValues(outParams: List<TypeReference<Type<*>>>): List<Any> {
        if (outParams.isEmpty()) {
            return emptyList()
        }
        Logger.d(result)
        return FunctionReturnDecoder.decode(result, outParams).map { it.value }
    }
}