package org.session.libsignal.crypto

import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.wordlists.English
import org.session.libsignal.utilities.Hex
import org.session.libsignal.utilities.Log
import org.session.libsignal.utilities.toHexString
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.util.zip.CRC32


/**
 * Based on [mnemonic.js](https://github.com/loki-project/loki-messenger/blob/development/libloki/modules/mnemonic.js) .
 */
class MnemonicCodec(private val loadFileContents: (String) -> String) {

    class Language(
        private val loadFileContents: (String) -> String, private val configuration: Configuration
    ) {

        data class Configuration(val filename: String, val prefixLength: Int) {

            companion object {
                val english = Configuration("english", 3)
                val japanese = Configuration("japanese", 3)
                val portuguese = Configuration("portuguese", 4)
                val spanish = Configuration("spanish", 4)
            }
        }

        companion object {
            internal val wordSetCache = mutableMapOf<Language, List<String>>()
            internal val truncatedWordSetCache = mutableMapOf<Language, List<String>>()
        }

        internal fun loadWordSet(): List<String> {
            val cachedResult = wordSetCache[this]
            if (cachedResult != null) {
                return cachedResult
            } else {
                val contents = loadFileContents(configuration.filename)
                val result = contents.split(",")
                wordSetCache[this] = result
                return result
            }
        }

        internal fun loadTruncatedWordSet(): List<String> {
            val cachedResult = wordSetCache[this]
            if (cachedResult != null) {
                return cachedResult
            } else {
                val prefixLength = configuration.prefixLength
                val result = loadWordSet().map { it.substring(0 until prefixLength) }
                truncatedWordSetCache[this] = result
                return result
            }
        }
    }

    sealed class DecodingError(val description: String) : Exception(description) {
        object Generic :
            DecodingError("Something went wrong. Please check your mnemonic and try again.")

        object InputTooShort :
            DecodingError("Looks like you didn't enter enough words. Please check your mnemonic and try again.")

        object MissingLastWord :
            DecodingError("You seem to be missing the last word of your mnemonic. Please check what you entered and try again.")

        object InvalidWord :
            DecodingError("There appears to be an invalid word in your mnemonic. Please check what you entered and try again.")

        object VerificationFailed :
            DecodingError("Your mnemonic couldn't be verified. Please check what you entered and try again.")
    }


    fun encode(
        hexEncodedString: String,
        languageConfiguration: Language.Configuration = Language.Configuration.english
    ): String {
        Log.d("key", "hexEncodedString: " + hexEncodedString)
        val sb = StringBuilder()
//        val entropy = ByteArray(Words.TWENTY_FOUR.byteLength())
        var entropy = Hex.fromStringCondensed(hexEncodedString)
        Log.d("key", "entropy toHexString seed: " + entropy.toHexString())
        MnemonicGenerator(English.INSTANCE).createMnemonic(entropy,
            MnemonicGenerator.Target { s: CharSequence? -> sb.append(s) })
        val mnemonics: String = sb.toString()
        Log.d("key", "web3j mnemonics:" + mnemonics)
        val seed = MnemonicUtils.generateEntropy(mnemonics)

        Log.d("key", "toHexString seed: " + seed.toHexString())
        return mnemonics
    }

    fun decode(
        mnemonic: String,
        languageConfiguration: Language.Configuration = Language.Configuration.english
    ): String {
        val words = mnemonic.split(" ").toMutableList()
        // support private key
        if (words.size == 1 && mnemonic.length == 64) {
            return mnemonic
        }

        // support private key
        if (words.size == 1 && mnemonic.length == 66 && mnemonic.startsWith("0x")) {
            return mnemonic.substring(2)
        }
        // support session style
        if (words.size == 13) {
            return decodeOld(mnemonic, languageConfiguration)
        }
        val seed = MnemonicUtils.generateEntropy(mnemonic)
        Log.d("key", "seed: " + seed.toHexString())
        return seed.toHexString()
    }

    fun decodeOld(
        mnemonic: String,
        languageConfiguration: Language.Configuration = Language.Configuration.english
    ): String {
        val words = mnemonic.split(" ").toMutableList()
        val language = Language(loadFileContents, languageConfiguration)
        val truncatedWordSet = language.loadTruncatedWordSet()
        val prefixLength = languageConfiguration.prefixLength
        var result = ""
        val n = truncatedWordSet.size.toLong()
        // Check preconditions
        if (words.size < 12) {
            throw DecodingError.InputTooShort
        }
        if (words.size % 3 == 0) {
            throw DecodingError.MissingLastWord
        }
        // Get checksum word
        val checksumWord = words.removeAt(words.lastIndex)
        // Decode
        for (chunkStartIndex in 0..(words.size - 3) step 3) {
            try {
                val w1 =
                    truncatedWordSet.indexOf(words[chunkStartIndex].substring(0 until prefixLength))
                val w2 =
                    truncatedWordSet.indexOf(words[chunkStartIndex + 1].substring(0 until prefixLength))
                val w3 =
                    truncatedWordSet.indexOf(words[chunkStartIndex + 2].substring(0 until prefixLength))
                val x = w1 + n * ((n - w1 + w2) % n) + n * n * ((n - w2 + w3) % n)
                if (x % n != w1.toLong()) {
                    throw DecodingError.Generic
                }
                val string = "0000000" + x.toString(16)
                result += swap(string.substring(string.length - 8 until string.length))
            } catch (e: Exception) {
                throw DecodingError.InvalidWord
            }
        }
        // Verify checksum
        val checksumIndex = determineChecksumIndex(words, prefixLength)
        val expectedChecksumWord = words[checksumIndex]
        if (expectedChecksumWord.substring(0 until prefixLength) != checksumWord.substring(0 until prefixLength)) {
            throw DecodingError.VerificationFailed
        }
        // Return
        return result
        val seed = MnemonicUtils.generateEntropy(mnemonic)
        Log.d("key", "seed: " + seed.toHexString())
        return seed.toHexString()
    }

    private fun swap(x: String): String {
        val p1 = x.substring(6 until 8)
        val p2 = x.substring(4 until 6)
        val p3 = x.substring(2 until 4)
        val p4 = x.substring(0 until 2)
        return p1 + p2 + p3 + p4
    }

    private fun determineChecksumIndex(x: List<String>, prefixLength: Int): Int {
        val bytes = x.joinToString("") { it.substring(0 until prefixLength) }.toByteArray()
        val crc32 = CRC32()
        crc32.update(bytes)
        val checksum = crc32.value
        return (checksum % x.size.toLong()).toInt()
    }

    companion object {
        @JvmStatic
        fun toAddress(mnemonic: String): String {
            val path = intArrayOf(
                44 or Bip32ECKeyPair.HARDENED_BIT,
                60 or Bip32ECKeyPair.HARDENED_BIT,
                0 or Bip32ECKeyPair.HARDENED_BIT,
                0,
                0
            )
            val words = mnemonic.split(" ").toMutableList()
            // support private key
            return if (words.size == 1 && mnemonic.length == 64) {
                val credentials = Credentials.create(mnemonic)
                credentials.address
            } else {
                val seed = MnemonicUtils.generateSeed(mnemonic, "")
                val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
                val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
                val credentials = Credentials.create(bip44Keypair)
                credentials.address
            }
        }

        @JvmStatic
        fun toWallet(mnemonic: String): Wallet {
            val path = intArrayOf(
                44 or Bip32ECKeyPair.HARDENED_BIT,
                60 or Bip32ECKeyPair.HARDENED_BIT,
                0 or Bip32ECKeyPair.HARDENED_BIT,
                0,
                0
            )
            val words = mnemonic.split(" ").toMutableList()
            // support private key
            return if (words.size == 1 && mnemonic.length == 64) {
                val credentials = Credentials.create(mnemonic)
                val pk = "0x$mnemonic"
                val address = Keys.toChecksumAddress(credentials.address)
                Wallet("", pk, address)
            } else {
                val seed = MnemonicUtils.generateSeed(mnemonic, "")
                val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
                val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
                val credentials = Credentials.create(bip44Keypair)
                val pk = Numeric.toHexStringWithPrefix(bip44Keypair.privateKey)
                val address = Keys.toChecksumAddress(credentials.address)
                Wallet(mnemonic, pk, address)
            }
        }
    }
}
