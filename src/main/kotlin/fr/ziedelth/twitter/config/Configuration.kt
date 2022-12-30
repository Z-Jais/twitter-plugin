package fr.ziedelth.twitter.config

data class Configuration(
    val oAuthConsumerKey: String = "",
    val oAuthConsumerSecret: String = "",
    val oAuthAccessToken: String = "",
    val oAuthAccessTokenSecret: String = ""
) {
    fun isValid(): Boolean {
        return oAuthConsumerKey.isNotEmpty() && oAuthConsumerSecret.isNotEmpty() && oAuthAccessToken.isNotEmpty() && oAuthAccessTokenSecret.isNotEmpty()
    }
}
