package fr.ziedelth.twitter

import com.google.gson.GsonBuilder
import fr.ziedelth.twitter.listeners.ListenerManager
import fr.ziedelth.utils.plugins.JaisPlugin
import org.pf4j.PluginWrapper
import twitter4j.Twitter
import java.io.File

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
            throw RuntimeException("Please fill the config file before restarting the plugin.")
        }

        val config = gson.fromJson(file.readText(), Configuration::class.java)

        if (!config.isValid()) {
            throw RuntimeException("Please fill the config file before restarting the plugin.")
        }

        println("Starting TwitterTemplate...")

        try {
            val twitter = Twitter.newBuilder().oAuthConsumer(config.oAuthConsumerKey, config.oAuthConsumerSecret)
                .oAuthAccessToken(config.oAuthAccessToken, config.oAuthAccessTokenSecret).build()
            // Test the connection
            twitter?.v1()?.tweets()?.lookup(1588739131815112704)

            ListenerManager(twitter)
        } catch (e: Exception) {
            throw RuntimeException("An error occurred while creating the Twitter instance. Please check your credentials.")
        }
    }
}