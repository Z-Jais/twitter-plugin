package fr.ziedelth.twitter.listeners

import fr.ziedelth.events.EpisodesReleaseEvent
import fr.ziedelth.utils.plugins.events.EventHandler
import fr.ziedelth.utils.plugins.events.Listener
import twitter4j.Twitter
import twitter4j.v1.StatusUpdate
import java.net.URL
import java.util.*

class EpisodesRelease(private val twitter: Twitter) : Listener {
    private fun getTinyUrl(url: String?) = URL("https://urlz.fr/api_new.php?url=$url").readText()

    @EventHandler
    fun onEpisodesRelease(event: EpisodesReleaseEvent) {
        event.episodes.forEach { episode ->
            try {
                val uploadedMedia = this.twitter.v1().tweets().uploadMedia(
                    "${UUID.randomUUID().toString().replace("-", "")}.jpg",
                    URL(episode.image).openStream()
                )

                if (uploadedMedia == null) {
                    println("An error occurred while uploading the media.")
                    return
                }

                val statusUpdate = StatusUpdate.of("\uD83C\uDF89 ${episode.anime?.name}\n" +
                        "${episode.title?.ifBlank { "＞﹏＜" } ?: "＞﹏＜"}\n" +
                        "\n" +
                        getTinyUrl(episode.url))
                statusUpdate.mediaIds(uploadedMedia.mediaId)
                twitter.v1().tweets().updateStatus(statusUpdate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
