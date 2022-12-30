package fr.ziedelth.twitter

import com.google.gson.GsonBuilder
import fr.ziedelth.twitter.config.Configuration
import fr.ziedelth.twitter.listeners.ListenerManager
import fr.ziedelth.utils.plugins.JaisPlugin
import org.pf4j.PluginWrapper
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private const val CONFIG_FILE_ERROR = "Please fill the config file before restarting the plugin."

class TwitterPlugin(wrapper: PluginWrapper) : JaisPlugin(wrapper) {
    override fun start() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val file = File(dataFolder, "config.json")
        val gson = GsonBuilder().setPrettyPrinting().create()

        if (!file.exists()) {
            println("Creating config file...")
            file.createNewFile()
            file.writeText(gson.toJson(Configuration()))
            println(CONFIG_FILE_ERROR)
            throw RuntimeException(CONFIG_FILE_ERROR)
        }

        val config = gson.fromJson(file.readText(), Configuration::class.java)

        if (!config.isValid()) {
            println(CONFIG_FILE_ERROR)
            throw RuntimeException(CONFIG_FILE_ERROR)
        }

        println("Starting TwitterTemplate...")

        try {
            val twitter = TwitterFactory(
                ConfigurationBuilder()
                    .setDebugEnabled(true)
                    .setOAuthConsumerKey(config.oAuthConsumerKey)
                    .setOAuthConsumerSecret(config.oAuthConsumerSecret)
                    .setOAuthAccessToken(config.oAuthAccessToken)
                    .setOAuthAccessTokenSecret(config.oAuthAccessTokenSecret)
                    .build()
            ).instance
            // Test the connection
            twitter?.tweets()?.lookup(1588739131815112704)

            ListenerManager(this, twitter)
            println("Started TwitterTemplate.")
        } catch (e: Exception) {
            val errorMessage = "An error occurred while creating the Twitter instance. Please check your credentials."

            println(errorMessage)
            throw RuntimeException(errorMessage)
        }
    }

    fun getTinyUrl(url: String?): String {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://zdh.fr/"))
                .POST(HttpRequest.BodyPublishers.ofString("$url"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            return URL("https://urlz.fr/api_new.php?url=$url").readText()
        }

        return response.body()
    }
}