package net.perfectdreams.ddgamehard

import net.perfectdreams.renderer.HarmonyGL
import web.gl.WebGL2RenderingContext
import web.gl.WebGLProgram
import web.gl.WebGLRenderingContext
import web.gl.WebGLShader

class ShaderManager(val virtualFileSystem: VirtualFileSystem) {
    /**
     * Loads the vertex shader and fragment shader by their file name from the application's resources
     */
    fun loadShader(vertexShaderFileName: String, fragmentShaderFileName: String): WebGLProgram {
        val vertexShaderId = HarmonyGL._WEBGL.createShader(WebGL2RenderingContext.VERTEX_SHADER) ?: error("Failed to create vertex shader")
        val fragmentShaderId = HarmonyGL._WEBGL.createShader(WebGL2RenderingContext.FRAGMENT_SHADER) ?: error("Failed to create fragment shader")

        // Compile Vertex Shader
        println("Attempting to compile shader $vertexShaderFileName")
        checkAndCompile(vertexShaderId, virtualFileSystem.files[vertexShaderFileName]!!.decodeToString())

        // Compile Fragment Shader
        println("Attempting to compile shader $fragmentShaderFileName")
        checkAndCompile(fragmentShaderId, virtualFileSystem.files[fragmentShaderFileName]!!.decodeToString())

        val programId = HarmonyGL._WEBGL.createProgram() ?: error("Failed to create WebGL shader program")
        HarmonyGL._WEBGL.attachShader(programId, vertexShaderId)
        HarmonyGL._WEBGL.attachShader(programId, fragmentShaderId)
        HarmonyGL._WEBGL.linkProgram(programId)

        // Check the program
        val result = HarmonyGL._WEBGL.getProgramParameter(programId, WebGLRenderingContext.LINK_STATUS)
        val infoLog = HarmonyGL._WEBGL.getProgramInfoLog(programId)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != true) {
            error("Something went wrong while linking shader! Status: $result; Info: $infoLog")
        }

        HarmonyGL._WEBGL.detachShader(programId, vertexShaderId)
        HarmonyGL._WEBGL.detachShader(programId, fragmentShaderId)

        HarmonyGL._WEBGL.deleteShader(vertexShaderId)
        HarmonyGL._WEBGL.deleteShader(fragmentShaderId)

        return programId
    }

    private fun checkAndCompile(shaderId: WebGLShader, code: String) {
        // Compile Shader
        HarmonyGL._WEBGL.shaderSource(shaderId, code)
        HarmonyGL._WEBGL.compileShader(shaderId)

        // Check Shader
        val result = HarmonyGL._WEBGL.getShaderParameter(shaderId, WebGLRenderingContext.COMPILE_STATUS)
        val infoLog = HarmonyGL._WEBGL.getShaderInfoLog(shaderId)

        // YES DON'T FORGET THAT WE NEED TO USE GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != true) {
            error("Something went wrong while compiling shader $shaderId! Status: $result; Info: $infoLog")
        }
    }
}