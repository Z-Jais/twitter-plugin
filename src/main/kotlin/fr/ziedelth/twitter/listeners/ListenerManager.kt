package fr.ziedelth.twitter.listeners

import fr.ziedelth.utils.plugins.PluginManager
import twitter4j.Twitter

class ListenerManager(twitter: Twitter) {
    init {
        PluginManager.registerEvents(EpisodesRelease(twitter))
    }
}