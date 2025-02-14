package net.perfectdreams.ddgamehard

import net.perfectdreams.renderer.HarmonyGL
import web.gl.WebGL2RenderingContext
import web.gl.WebGLTexture

class ResourceManager(val virtualFileSystem: VirtualFileSystem) {
    fun loadTexture(path: String): LoadedImage {
        val textureID = HarmonyGL.glGenTextures()
        HarmonyGL.glBindTexture(WebGL2RenderingContext.TEXTURE_2D, textureID)

        val image = virtualFileSystem.images["sprites/$path"]!!
        println("Loaded file: ${image}")
        HarmonyGL.glTexImage2D(
            WebGL2RenderingContext.TEXTURE_2D,
            0,
            WebGL2RenderingContext.RGBA,
            WebGL2RenderingContext.RGBA,
            WebGL2RenderingContext.UNSIGNED_BYTE,
            image
        )

        HarmonyGL.glTexParameteri(
            WebGL2RenderingContext.TEXTURE_2D,
            WebGL2RenderingContext.TEXTURE_MIN_FILTER,
            WebGL2RenderingContext.NEAREST
        )

        HarmonyGL.glTexParameteri(
            WebGL2RenderingContext.TEXTURE_2D,
            WebGL2RenderingContext.TEXTURE_MAG_FILTER,
            WebGL2RenderingContext.NEAREST
        )

        return LoadedImage(textureID, image.width, image.height)
    }

    data class LoadedImage(val textureId: WebGLTexture, val width: Int, val height: Int)
}