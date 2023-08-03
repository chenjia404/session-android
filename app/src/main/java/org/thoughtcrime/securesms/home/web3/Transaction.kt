package org.thoughtcrime.securesms.home.web3

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    // 自定义
    var key: String,
    var isNative: Boolean = false,
    var chainId: Int = 0,
    var showDecimals: Int = 0,
    var data: String? = null,
    var gasDefault: Boolean = false,
    // 主网代币
    var blockNumber: String? = null,

    // 秒为单位
    var timeStamp: Long = 0,
    var hash: String,
    var nonce: String? = null,
    var blockHash: String? = null,
    var transactionIndex: String? = null,
    var from: String? = null,
    var to: String? = null,
    var value: String? = null,
    //var gas: BigInteger? = null,
    var gasPrice: String? = null,
    var gas: String? = null,
    var isError: Int = 0,
    var txreceipt_status: Int = 0,
    var input: String? = null,
    var contractAddress: String? = null,
    var gasUsed: String? = null,
    var confirmations: Long? = null,
    var methodId: String? = null,
    var functionName: String? = null,

    // erc20 资产
    var tokenName: String? = null,
    var tokenSymbol: String? = null,
    var tokenDecimal: Int = 0,
    var tokenValue: String? = null,
    // web3
    var callbackId: Long = 0
) : Parcelable