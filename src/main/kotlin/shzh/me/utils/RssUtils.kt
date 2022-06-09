package shzh.me.utils

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL

object RssUtils {
    fun fetchTitle(url: String): String {
        val feed = SyndFeedInput().build(XmlReader(URL(url)))
        return feed.title!!
    }

    fun fetchLatestEntry(url: String): SyndEntry {
        val feed = SyndFeedInput().build(XmlReader(URL(url)))
        return feed.entries[0]
    }
}