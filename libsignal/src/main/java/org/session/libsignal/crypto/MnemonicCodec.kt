package org.session.libsignal.crypto

import io.github.novacrypto.bip32.ExtendedPrivateKey
import io.github.novacrypto.bip32.networks.Bitcoin
import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.wordlists.English
import io.github.novacrypto.bip44.AddressIndex
import io.github.novacrypto.bip44.BIP44
import org.session.libsignal.utilities.Hex
import org.session.libsignal.utilities.Log
import org.session.libsignal.utilities.toHexString
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import java.util.zip.CRC32


/**
 * Based on [mnemonic.js](https://github.com/loki-project/loki-messenger/blob/development/libloki/modules/mnemonic.js) .
 */
class MnemonicCodec(private val loadFileContents: (String) -> String) {

    class Language(private val loadFileContents: (String) -> String, private val configuration: Configuration) {

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
        object Generic : DecodingError("Something went wrong. Please check your mnemonic and try again.")
        object InputTooShort : DecodingError("Looks like you didn't enter enough words. Please check your mnemonic and try again.")
        object MissingLastWord : DecodingError("You seem to be missing the last word of your mnemonic. Please check what you entered and try again.")
        object InvalidWord : DecodingError("There appears to be an invalid word in your mnemonic. Please check what you entered and try again.")
        object VerificationFailed : DecodingError("Your mnemonic couldn't be verified. Please check what you entered and try again.")
    }


    fun encode(hexEncodedString: String, languageConfiguration: Language.Configuration = Language.Configuration.english): String {
        Log.d("key","hexEncodedString: "+hexEncodedString)
        val sb = StringBuilder()
//        val entropy = ByteArray(Words.TWENTY_FOUR.byteLength())
        var entropy = Hex.fromStringCondensed(hexEncodedString)
        Log.d("key","entropy toHexString seed: "+entropy.toHexString())
        MnemonicGenerator( English.INSTANCE).createMnemonic(
            entropy,
            MnemonicGenerator.Target { s: CharSequence? -> sb.append(s) })
        val mnemonics: String = sb.toString()
        Log.d("key","web3j mnemonics:"+mnemonics)
        val seed = MnemonicUtils.generateEntropy(mnemonics)

        Log.d("key","toHexString seed: "+seed.toHexString())
        return  mnemonics
    }

    fun decode(mnemonic: String, languageConfiguration: Language.Configuration = Language.Configuration.english): String {
        val seed = MnemonicUtils.generateEntropy(mnemonic)
        Log.d("key","seed: "+seed.toHexString())
        return  seed.toHexString()
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
}
