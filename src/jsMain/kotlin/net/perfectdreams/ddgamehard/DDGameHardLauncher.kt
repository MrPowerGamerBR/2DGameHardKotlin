package net.perfectdreams.ddgamehard

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import js.promise.Promise
import js.typedarrays.Uint8Array
import js.typedarrays.toUint8Array
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import web.blob.Blob
import web.events.EventHandler
import web.html.Image
import web.url.URL
import web.window.window

// Uh oh, hacky!!
// Used for the GameRoom level loader
lateinit var VIRTUAL_FILE_SYSTEM: VirtualFileSystem

fun main() {
    println("Attempting to load game assets...")
    val http = HttpClient {}

    GlobalScope.launch {
        val currentFolder = window.location.href.substring(0, window.location.href.lastIndexOf("/"))

        val response = http.get("$currentFolder/assets.zip")

        val jszip = JSZip()

        jszip.loadAsync(response.bodyAsBytes().toUint8Array()).await()
        val jsZipObjects = mutableMapOf<String, JSZipObject>()

        jszip.forEach { relativePath, file ->
            println("$relativePath: $file")

            jsZipObjects[relativePath] = file
        }

        val zipImages = mutableMapOf<String, Image>()
        val zipOthers = mutableMapOf<String, ByteArray>()

        // Now we FINALLY attempt to actually read the damn things
        // We do it like this because "async" is, guess what, async and it uses a promise
        // And don't get fooled, the "jszip.forEach" function is NOT Kotlin's forEach, which is why we can't do await there
        for ((relativePath, file) in jsZipObjects) {
            if (relativePath.endsWith(".png")) {
                val blob = file.async("blob").await() as Blob

                val image = Image()
                image.awaitLoad(URL.createObjectURL(blob))
                zipImages[relativePath] = image
            } else {
                val uint8array = file.async("uint8array").await() as Uint8Array<*>
                val byteArray = uint8array.toByteArray()

                zipOthers[relativePath] = byteArray
            }
        }

        val vfs = VirtualFileSystem(zipImages, zipOthers)
        VIRTUAL_FILE_SYSTEM = vfs

        val m = DDGameHard(vfs)
        m.start()
    }
}

suspend fun Image.awaitLoad(src: String): Image {
    return Promise { resolve, reject ->
        this.onload = EventHandler { resolve.invoke(this) }
        this.onerror = {
            reject.invoke()
        }
        this.src = src
    }.await()
}