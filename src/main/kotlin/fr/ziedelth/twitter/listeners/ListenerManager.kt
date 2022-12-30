package fr.ziedelth.twitter.listeners

import fr.ziedelth.twitter.TwitterPlugin
import fr.ziedelth.utils.plugins.PluginManager
import twitter4j.Twitter

class ListenerManager(twitterPlugin: TwitterPlugin, twitter: Twitter) {
    init {
        PluginManager.registerEvents(
            EpisodesRelease(twitterPlugin, twitter),
            NewsRelease(twitterPlugin, twitter)
        )
    }
}