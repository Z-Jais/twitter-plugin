package fr.ziedelth.twitter.listeners

import fr.ziedelth.entities.Episode
import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.twitter.TwitterPlugin
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import twitter4j.StatusUpdate
import twitter4j.Twitter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

class EpisodesRelease(private val twitterPlugin: TwitterPlugin, private val twitter: Twitter) : Listener {
    private fun String.onlyLettersAndDigits(): String = this.filter { it.isLetterOrDigit() }

    private fun information(episode: Episode): String {
        return when (episode.episodeType?.name) {
            "SPECIAL" -> "L'épisode spécial"
            "FILM" -> "Le film"
            else -> "L'épisode ${episode.number}"
        }
    }

    private fun platformAccount(episode: Episode): String {
        return when(episode.platform?.name) {
            "Animation Digital Network" -> "@ADNanime"
            "Crunchyroll" -> "@Crunchyroll_fr"
            "Netflix" -> "@NetflixFR"
            else -> "${episode.platform?.name}"
        }
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

                val isVoice = if (episode.langType?.name == "VOICE") " en VF" else ""

                val s = "📺 C'est parti !\n" +
                        "\n" +
                        "${information(episode)} de l'anime \"#${episode.anime?.name?.onlyLettersAndDigits()}\" est enfin disponible$isVoice, ne ratez pas ça ! Bon visionnage à tous avec ${platformAccount(episode)} 🔥\n" +
                        "\n" +
                        "▶️ ${twitterPlugin.getTinyUrl(episode.url)}"
                println(s)

                val statusUpdate = StatusUpdate(s)
                statusUpdate.setMediaIds(media.mediaId)
                tweets.updateStatus(statusUpdate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
