package com.example.weerapp.network

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

data class KnmiItem(val title: String, val date: String)

suspend fun fetchKnmiEarthquakes(client: okhttp3.OkHttpClient): List<KnmiItem> {
    val url = "https://rdsa.knmi.nl/abcws/event/query?format=rss&limit=30"

    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Android")
            .header("Accept", "application/rss+xml, application/xml")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("KNMI HTTP ${response.code}")

            val bodyStream = response.body?.byteStream() ?: throw Exception("Lege response van KNMI")
            val feed = SyndFeedInput().build(XmlReader(bodyStream))

            feed.entries.map { e ->
                KnmiItem(
                    title = e.title ?: "-",
                    date = e.publishedDate?.toString() ?: "-"
                )
            }
        }
    }
}
