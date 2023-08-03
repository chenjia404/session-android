package org.thoughtcrime.securesms.home.web3

enum class DAppMethod {
    SIGNTRANSACTION,
    SIGNPERSONALMESSAGE,
    SIGNMESSAGE,
    SIGNTYPEDMESSAGE,
    ECRECOVER,
    REQUESTACCOUNTS,
    WATCHASSET,
    ADDETHEREUMCHAIN,
    SWITCHETHEREUMCHAIN,
    READTIME,
    UNKNOWN;

    companion object {
        fun fromValue(value: String): DAppMethod {
            return when (value) {
                "signTransaction" -> SIGNTRANSACTION
                "signPersonalMessage" -> SIGNPERSONALMESSAGE
                "signMessage" -> SIGNMESSAGE
                "signTypedMessage" -> SIGNTYPEDMESSAGE
                "ecRecover" -> ECRECOVER
                "requestAccounts" -> REQUESTACCOUNTS
                "watchAsset" -> WATCHASSET
                "addEthereumChain" -> ADDETHEREUMCHAIN
                "switchEthereumChain" -> SWITCHETHEREUMCHAIN
                "requestReadTime" -> READTIME
                else -> UNKNOWN
            }
        }
    }
}
