package net.perfectdreams.renderer

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import web.gl.*
import web.timers.Interval

class HarmonyGL {
    companion object {
        var WEBGL: WebGL2RenderingContext? = null

        fun setTarget(webgl: WebGL2RenderingContext?) {
            this.WEBGL = webgl
            println("Set new WebGL2RenderingContext")
        }

        val GL_FRAMEBUFFER = WebGL2RenderingContext.FRAMEBUFFER
        val GL_FRAMEBUFFER_COMPLETE = WebGL2RenderingContext.FRAMEBUFFER_COMPLETE
        val GL_TRIANGLES = WebGL2RenderingContext.TRIANGLES
        val GL_TEXTURE0 = WebGL2RenderingContext.TEXTURE0
        val GL_TEXTURE_2D = WebGL2RenderingContext.TEXTURE_2D
        val GL_STATIC_DRAW = WebGL2RenderingContext.STATIC_DRAW
        val GL_ARRAY_BUFFER = WebGL2RenderingContext.ARRAY_BUFFER
        val GL_FLOAT = WebGL2RenderingContext.FLOAT
        val GL_BLEND = WebGL2RenderingContext.BLEND
        val GL_ONE = WebGL2RenderingContext.ONE
        val GL_SRC_ALPHA = WebGL2RenderingContext.SRC_ALPHA
        val GL_ONE_MINUS_SRC_ALPHA = WebGL2RenderingContext.ONE_MINUS_SRC_ALPHA
        val GL_RGBA = WebGL2RenderingContext.RGBA
        val GL_UNSIGNED_BYTE = WebGL2RenderingContext.UNSIGNED_BYTE
        val GL_TEXTURE_MIN_FILTER = WebGL2RenderingContext.TEXTURE_MIN_FILTER
        val GL_TEXTURE_MAG_FILTER = WebGL2RenderingContext.TEXTURE_MAG_FILTER
        val GL_LINEAR = WebGL2RenderingContext.LINEAR
        val GL_COLOR_ATTACHMENT0 = WebGL2RenderingContext.COLOR_ATTACHMENT0

        val _WEBGL
            get() = WEBGL!!

        fun glEnable(capability: GLenum) {
            return _WEBGL.enable(capability)
        }

        fun glBlendFunc(sfactor: GLenum, dfactor: GLenum) {
            return _WEBGL.blendFunc(sfactor, dfactor)
        }

        fun glClearColor(r: Float, g: Float, b: Float, a: Float) {
            return _WEBGL.clearColor(r, g, b, a)
        }

        fun glClear() {
            return _WEBGL.clear(WebGL2RenderingContext.COLOR_BUFFER_BIT + WebGL2RenderingContext.DEPTH_BUFFER_BIT)
        }

        fun glGenFramebuffers(): WebGLFramebuffer {
            return _WEBGL.createFramebuffer()
        }

        fun glGenTextures(): WebGLTexture {
            return _WEBGL.createTexture()
        }

        fun glCheckFramebufferStatus(target: GLenum): GLenum {
            return _WEBGL.checkFramebufferStatus(target)
        }

        fun glGetUniformLocation(program: WebGLProgram, param: String): WebGLUniformLocation {
            return _WEBGL.getUniformLocation(program, param) ?: error("Unknown uniform location!")
        }

        fun glDrawArrays(mode: GLenum, first: Int, size: Int) {
            _WEBGL.drawArrays(mode, first, size)
        }

        fun glBindFramebuffer(target: GLenum, framebuffer: WebGLFramebuffer?) {
            _WEBGL.bindFramebuffer(target, framebuffer)
        }

        fun glUseProgram(program: WebGLProgram) {
            _WEBGL.useProgram(program)
        }

        fun glBindVertexArray(vao: WebGLVertexArrayObject?) {
            _WEBGL.bindVertexArray(vao)
        }

        fun glActiveTexture(texture: GLenum) {
            _WEBGL.activeTexture(texture)
        }

        fun glBindTexture(target: GLenum, texture: WebGLTexture) {
            _WEBGL.bindTexture(target, texture)
        }

        fun glGenBuffers(): WebGLBuffer {
            return _WEBGL.createBuffer()
        }

        fun glGenVertexArrays(): WebGLVertexArrayObject {
            return _WEBGL.createVertexArray()
        }

        fun glBindBuffer(target: GLenum, buffer: WebGLBuffer?) {
            _WEBGL.bindBuffer(target, buffer)
        }

        fun glBufferData(target: GLenum, buffer: FloatArray, usage: GLenum) {
            _WEBGL.bufferData(target, toFloat32Array(buffer), usage)
        }

        fun glEnableVertexAttribArray(index: Int) {
            _WEBGL.enableVertexAttribArray(index)
        }

        fun glVertexAttribPointer(index: Int, size: Int, type: GLenum, normalized: Boolean, stride: Int, offset: Int) {
            _WEBGL.vertexAttribPointer(index, size, type, normalized, stride, offset)
        }

        fun glUniformMatrix4fv(location: WebGLUniformLocation, transpose: Boolean, matrix: FloatArray) {
            _WEBGL.uniformMatrix4fv(location, transpose, toFloat32Array(matrix), null, null)
        }

        fun glUniform1f(location: WebGLUniformLocation, x: Float) {
            _WEBGL.uniform1f(location, x)
        }

        fun glUniform3f(location: WebGLUniformLocation, x: Float, y: Float, z: Float) {
            _WEBGL.uniform3f(location, x, y, z)
        }

        fun glUniform4f(location: WebGLUniformLocation, x: Float, y: Float, z: Float, w: Float) {
            _WEBGL.uniform4f(location, x, y, z, w)
        }

        fun glTexImage2D(target: GLenum, level: Int, internalformat: GLenum, format: GLenum, type: GLenum, pixels: TexImageSource) {
            _WEBGL.texImage2D(target, level, internalformat, format, type, pixels)
        }

        fun glTexImage2D(target: GLenum, level: Int, internalformat: GLenum, width: Int, height: Int, border: Int, format: GLenum, type: GLenum, pixels: js.buffer.ArrayBufferView<*>?) {
            _WEBGL.texImage2D(target, level, internalformat, width, height, border, format, type, pixels)
        }

        fun glTexParameteri(target: GLenum, pname: GLenum, param: GLenum) {
            _WEBGL.texParameteri(target, pname, param.fixToInt())
        }

        fun glFramebufferTexture2D(target: GLenum, attachment: GLenum, textarget: GLenum, texture: WebGLTexture?, level: Int) {
            _WEBGL.framebufferTexture2D(target, attachment, textarget, texture, level)
        }

        private fun toFloat32Array(array: FloatArray): Float32Array<ArrayBuffer> {
            return Float32Array<ArrayBuffer>(array.toTypedArray())
        }

        fun GLenum.fixToInt(): Int {
            return this.toString().toInt()
        }
    }
}