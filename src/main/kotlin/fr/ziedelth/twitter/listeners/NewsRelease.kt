package fr.ziedelth.twitter.listeners

import com.google.gson.Gson
import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest
import fr.ziedelth.events.NewsReleaseEvent
import fr.ziedelth.twitter.TwitterPlugin
import fr.ziedelth.twitter.config.OpenAIConfiguration
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import twitter4j.StatusUpdate
import twitter4j.Twitter
import java.io.File

class NewsRelease(private val twitterPlugin: TwitterPlugin, private val twitter: Twitter) : Listener {
    private fun prompt(question: String): String? {
        val file = File(twitterPlugin.dataFolder, "openai.json")
        val gson = Gson()

        if (!file.exists()) {
            println("Creating OpenAI file...")
            file.createNewFile()
            file.writeText(gson.toJson(OpenAIConfiguration()))
            throw RuntimeException("Please fill the OpenAI file before restarting the plugin.")
        }

        val config = gson.fromJson(file.readText(), OpenAIConfiguration::class.java)

        if (config.token.isBlank()) {
            throw RuntimeException("Please fill the OpenAI file before restarting the plugin.")
        }

        return OpenAiService(config.token)
            .createCompletion(
                CompletionRequest.builder()
                    .prompt(question)
                    .model("text-davinci-003")
                    .temperature(0.7)
                    .maxTokens(256)
                    .topP(1.0)
                    .frequencyPenalty(0.0)
                    .presencePenalty(0.0)
                    .bestOf(1)
                    .build()
            ).choices.firstOrNull()?.text?.trim()
    }

    @EventHandler
    fun onNewsRelease(event: NewsReleaseEvent) {
        val tweets = twitter.tweets()

        event.news.forEach { news ->
            try {
                val prompt = prompt("Extrais-moi les informations importantes de cette news pour faire un tweet avec celles-ci\n" +
                        "\n" +
                        "Titre : ${news.title}\n" +
                        "\n" +
                        "Contenu : ${news.description}\n" +
                        "\n" +
                        "Tweet : ")

                val s = "\uD83C\uDF89 $prompt\n" +
                        "\n" +
                        twitterPlugin.getTinyUrl(news.url)
                println(s)
                tweets.updateStatus(StatusUpdate(s))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
