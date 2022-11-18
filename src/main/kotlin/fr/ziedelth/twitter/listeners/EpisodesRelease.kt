package fr.ziedelth.twitter.listeners

import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import twitter4j.StatusUpdate
import twitter4j.Twitter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.imageio.ImageIO

class EpisodesRelease(private val twitter: Twitter) : Listener {
    private fun String.onlyLettersAndDigits(): String = this.filter { it.isLetterOrDigit() }

    private fun getTinyUrl(url: String?): String {
        // URL("https://urlz.fr/api_new.php?url=$url").readText()

        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://zdh.fr/"))
                .POST(HttpRequest.BodyPublishers.ofString("$url"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).body()
    }

    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        val tweets = twitter.tweets()

        event.episodes.forEach { episode ->
            try {
                val bufferedImage = ImageIO.read(URL(episode.image))
                val baos = ByteArrayOutputStream()
                ImageIO.write(bufferedImage, "jpg", baos)
                val inputStream = ByteArrayInputStream(baos.toByteArray())

                val media = tweets.uploadMedia(
                    "${UUID.randomUUID().toString().replace("-", "")}.jpg",
                    inputStream
                )

                val statusUpdate = StatusUpdate("\uD83C\uDF89 ${episode.anime?.name}\n" +
                        "Saison ${episode.season} • ${
                            when (episode.episodeType?.name) {
                                "SPECIAL" -> "Spécial"
                                "MOVIE" -> "Film"
                                else -> "Épisode"
                            }
                        } ${episode.number} ${
                            when (episode.langType?.name) {
                                "SUBTITLES" -> "VOSTFR"
                                "VOICE" -> "VF"
                                else -> ""
                            }
                        }\n" +
                        "URL : ${getTinyUrl(episode.url)}\n" +
                        "#Anime #${episode.platform?.name?.onlyLettersAndDigits()} #${episode.anime?.name?.onlyLettersAndDigits()}"
                )
                statusUpdate.setMediaIds(media.mediaId)
                tweets.updateStatus(statusUpdate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
