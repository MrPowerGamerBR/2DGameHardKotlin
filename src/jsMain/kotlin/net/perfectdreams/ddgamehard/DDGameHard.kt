package net.perfectdreams.ddgamehard

import js.date.Date
import net.perfectdreams.ddgamehard.logic.Easings
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.GameRoom
import net.perfectdreams.ddgamehard.logic.Sprites
import net.perfectdreams.ddgamehard.logic.entities.Coin
import net.perfectdreams.ddgamehard.logic.entities.Fire
import net.perfectdreams.ddgamehard.logic.entities.Player
import net.perfectdreams.ddgamehard.logic.entities.RenderableEntity
import net.perfectdreams.harmony.math.*
import net.perfectdreams.renderer.HarmonyGL
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_ARRAY_BUFFER
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_FLOAT
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_ONE_MINUS_SRC_ALPHA
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_SRC_ALPHA
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_STATIC_DRAW
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_TEXTURE0
import net.perfectdreams.renderer.HarmonyGL.Companion.GL_TEXTURE_2D
import net.perfectdreams.renderer.HarmonyGL.Companion.glBindBuffer
import net.perfectdreams.renderer.HarmonyGL.Companion.glBindVertexArray
import net.perfectdreams.renderer.HarmonyGL.Companion.glBlendFunc
import net.perfectdreams.renderer.HarmonyGL.Companion.glBufferData
import net.perfectdreams.renderer.HarmonyGL.Companion.glClearColor
import net.perfectdreams.renderer.HarmonyGL.Companion.glEnableVertexAttribArray
import net.perfectdreams.renderer.HarmonyGL.Companion.glGenBuffers
import net.perfectdreams.renderer.HarmonyGL.Companion.glGetUniformLocation
import net.perfectdreams.renderer.HarmonyGL.Companion.glUniform1f
import net.perfectdreams.renderer.HarmonyGL.Companion.glUniform3f
import net.perfectdreams.renderer.HarmonyGL.Companion.glUniformMatrix4fv
import net.perfectdreams.renderer.HarmonyGL.Companion.glUseProgram
import net.perfectdreams.renderer.HarmonyGL.Companion.glVertexAttribPointer
import web.animations.requestAnimationFrame
import web.dom.document
import web.events.EventHandler
import web.events.addEventListener
import web.gl.*
import web.html.HTMLCanvasElement
import web.performance.performance
import web.uievents.KeyboardEvent
import web.uievents.TouchEvent
import kotlin.math.sin

class DDGameHard(val virtualFileSystem: VirtualFileSystem) {
    companion object {
        const val PHYSICS_TICK = 60
        const val PHYSICS_TIME = 1000000000 / PHYSICS_TICK
    }

    // The window handle
    private var window: Long = 0

    private val windowWidth = 1280
    private val windowHeight = 720

    val logic = GameLogic()

    // A map that contains all the char -> UV map on the file
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:;= "
    val charactersUV = mutableMapOf<Char, Vector4f>()

    fun start() {
        println("Hello from Kotlin/JS ${KotlinVersion.CURRENT}!")

        logic.switchToRoom(GameRoom.Level1(logic))

        var currentX = 0
        var currentY = 0
        for (char in characters) {
            if (currentX >= 256) {
                currentX = 0
                currentY += 32
            }

            val uMin = currentX / 256f
            val vMin = currentY / 256f
            val uMax = uMin + 0.125f
            val vMax = vMin + 0.125f

            val uvMap = Vector4f(uMin, vMin, uMax, vMax)
            println("Character ${char} UV Map is ${uvMap.x}, ${uvMap.y}, ${uvMap.z}, ${uvMap.w}")
            charactersUV[char] = uvMap
            currentX += 32
        }

        init()
        loop()
    }

    private fun init() {
        val glCanvas = document.querySelector("#glCanvas") as HTMLCanvasElement
        val gl = glCanvas.getContext(WebGL2RenderingContext.ID)
        HarmonyGL.setTarget(gl)

        document.addEventListener(
            KeyboardEvent.KEY_DOWN,
            {
                logic.keyboard.pressedKeys.add(it.asDynamic().keyCode)
            }
        )

        document.addEventListener(
            KeyboardEvent.KEY_UP,
            {
                logic.keyboard.pressedKeys.remove(it.asDynamic().keyCode)
            }
        )
    }

    private fun loop() {
        // Required for transparent textures!
        HarmonyGL.glEnable(HarmonyGL.GL_BLEND)
        println("Enable HarmonyGL")
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Set the clear color
        HarmonyGL.glClearColor(0.8f, 0.0f, 0.0f, 1.0f)

        val shaderManager = ShaderManager(virtualFileSystem)
        val programId = shaderManager.loadShader("game.vsh", "game.fsh")

        val makeYellowProgramId = shaderManager.loadShader("coin.vsh", "makeyellow.fsh")
        val blurHorizontalProgramId = shaderManager.loadShader("coin.vsh", "horizontal_blur.fsh")
        val blurVerticalProgramId = shaderManager.loadShader("coin.vsh", "vertical_blur.fsh")
        val coinProgramId = shaderManager.loadShader("coin.vsh", "coin.fsh")
        val textProgramId = shaderManager.loadShader("text.vsh", "text.fsh")
        val darkenProgramId = shaderManager.loadShader("text.vsh", "darken.fsh")
        val gradientProgramId = shaderManager.loadShader("game.vsh", "background.fsh")

        println("Successfully compiled all shaders!")
        glUseProgram(programId)

        val vao = createSpriteVAO()
        val framebufferVAO = createFramebufferVAO()

        val textureFileNameToTextureIds = mutableMapOf<String, ResourceManager.LoadedImage>()

        val resourceManager = ResourceManager(virtualFileSystem)

        for (sprite in Sprites.registeredSprites) {
            println("Loading sprite ${sprite.fileName}")
            textureFileNameToTextureIds[sprite.fileName] = resourceManager.loadTexture(sprite.fileName)
        }
        println("Successfully loaded all sprites!")

        // Load font
        val fontImage = resourceManager.loadTexture("chars_transparent.png")

        var totalElapsedMS = PHYSICS_TIME.toLong() // We always want first render tick to ALSO do a physics tick to process the game

        var lastDeltaTime: Long = 0
        var lastRenderTime: Long = 0
        var lastGamePhysicsUpdate = nanoTime()

        val framebufferGame = createFramebuffer(windowWidth, windowHeight)
        val framebufferCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferYellowCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferHorizontalBlurCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferVerticalBlurCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferOverlay = createFramebuffer(windowWidth, windowHeight)
        val framebufferGui = createFramebuffer(windowWidth, windowHeight)

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        fun renderLoop(timestamp: Double) {
            // // println("Processing render...")
            val nanoTimestamp = (timestamp * 1_000_000).toLong()
            val delta = nanoTimestamp - lastRenderTime
            val startedProcessingAt = nanoTimestamp

            // Clear the default framebuffer (this is not really needed but it is useful when testing effects)
            HarmonyGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, null)
            HarmonyGL.glClear()

            fun clearFramebuffer(framebuffer: FramebufferResult) {
                // Clear the framebuffer
                HarmonyGL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebuffer.framebufferObject)
                HarmonyGL.glClear()
            }

            clearFramebuffer(framebufferGame)
            clearFramebuffer(framebufferCoins)
            clearFramebuffer(framebufferYellowCoins)
            clearFramebuffer(framebufferHorizontalBlurCoins)
            clearFramebuffer(framebufferVerticalBlurCoins)
            clearFramebuffer(framebufferOverlay)
            clearFramebuffer(framebufferGui)

            // Fully black because we will get the room's color for the gradient (ooo, fancy!)
            glClearColor(0f, 0f, 0f, 1.0f)

            // Listen up PUNK
            // We will first BIND OUR CUSTOM FRAMEBUFFER BECAUSE WE ARE BUILT LIKE THAT FR FR
            HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

            // The game runs at 20 ticks per second (50ms)
            while (totalElapsedMS >= PHYSICS_TIME) {
                // println("Processing logic! $totalElapsedMS")
                logic.tick()

                lastGamePhysicsUpdate = startedProcessingAt
                totalElapsedMS -= PHYSICS_TIME
            }

            val diffBetweenGamePhysicsAndNow = startedProcessingAt - lastGamePhysicsUpdate
            // println("Diff: $diffBetweenGamePhysicsAndNow")
            val interpolationPercent = (diffBetweenGamePhysicsAndNow / PHYSICS_TIME.toFloat())

            println("Interpolation Percent: $interpolationPercent")

            HarmonyGL.glClear() // clear the framebuffer

            val gameWidth = (windowWidth / 2f)
            val gameHeight = (windowHeight / 2f)

            val halfScreenWidth = (windowWidth / 1.5f)
            val halfScreenHeight = (windowHeight / 1.5f)

            val cameraX = Easings.easeLinear(logic.cameraCenterPrevious.x, logic.cameraCenterTarget.x, interpolationPercent)
            val cameraY = Easings.easeLinear(logic.cameraCenterPrevious.y, logic.cameraCenterTarget.y, interpolationPercent)

            run {
                glUseProgram(gradientProgramId)

                val projectionLocation = glGetUniformLocation(gradientProgramId, "projection")
                val colorTopLocation = glGetUniformLocation(gradientProgramId, "colorTop")
                val colorBottomLocation = glGetUniformLocation(gradientProgramId, "colorBottom")

                val left = cameraX - (halfScreenWidth / 2)
                val right = (halfScreenWidth) + cameraX - (halfScreenWidth / 2)
                val bottom = (halfScreenHeight) + cameraY - (halfScreenHeight / 2)
                val top = cameraY - (halfScreenHeight / 2)

                // println("Left: $left")
                // println("Right: $right")
                // println("Bottom: $bottom")
                // println("Top: $top")
                val projection = Matrix4f().ortho(
                    left,
                    right,
                    bottom,
                    top,
                    1f,
                    -1f
                ).getAsFloatArray()

                glUniformMatrix4fv(
                    projectionLocation,
                    false,
                    projection
                )

                glUniform3f(
                    colorTopLocation,
                    logic.room.topRoomColor.red / 255f,
                    logic.room.topRoomColor.green / 255f,
                    logic.room.topRoomColor.blue / 255f
                )

                glUniform3f(
                    colorBottomLocation,
                    logic.room.bottomRoomColor.red / 255f,
                    logic.room.bottomRoomColor.green / 255f,
                    logic.room.bottomRoomColor.blue / 255f,
                )

                drawSprite(gradientProgramId, framebufferVAO, null, Vector3f(-(gameWidth / 2f), -(gameHeight / 2f), 0f), Vector2f(10000f, 2000f))
            }

            glUseProgram(programId)

            val projectionLocation = glGetUniformLocation(programId, "projection")

            val left = cameraX - (halfScreenWidth / 2)
            val right = (halfScreenWidth) + cameraX - (halfScreenWidth / 2)
            val bottom = (halfScreenHeight) + cameraY - (halfScreenHeight / 2)
            val top = cameraY - (halfScreenHeight / 2)

            // println("Left: $left")
            // println("Right: $right")
            // println("Bottom: $bottom")
            // println("Top: $top")
            val projection = Matrix4f().ortho(
                left,
                right,
                bottom,
                top,
                1f,
                -1f
            ).getAsFloatArray()

            glUniformMatrix4fv(
                projectionLocation,
                false,
                projection
            )

            val room = logic.room
            for (entity in room.entities) {
                if (entity is RenderableEntity) {
                    if (!entity.isVisible)
                        continue

                    val pos = Vector3f(
                        Easings.easeLinear(entity.renderState.previous.x, entity.renderState.target.x, interpolationPercent),
                        Easings.easeLinear(entity.renderState.previous.y, entity.renderState.target.y, interpolationPercent),
                        Easings.easeLinear(entity.renderState.previous.z, entity.renderState.target.z, interpolationPercent)
                    )

                    if (entity is Player) {
                        // println("TICK ${logic.elapsedTicks}")
                        // println("Previous: ${entity.renderState.previous.x}, ${entity.renderState.previous.y}")
                        // println("Target: ${entity.renderState.target.x}, ${entity.renderState.target.y}")
                        // println("Real Position: ${entity.x}, ${entity.y}")
                        // println("Interpolated: ${pos.x}, ${pos.y} ($interpolationPercent)")
                    }
                    val scale = Vector2f(entity.width, entity.height)

                    // TODO: The reason it looks stuttery is because we need to take the easing in consideration!
                    if (entity.flipSprite) {
                        scale.x *= -1
                        // This now takes the easing in consideration... it looks ugly tho
                        pos.x += entity.width
                    }

                    if (entity is Coin) {
                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferCoins.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferOverlay.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    } else if (entity is Fire) {
                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

                        // Attempting to animate this using this instead of shaders
                        val originalScale = scale.y
                        val newScale = scale.y - (mapSinToZeroOne(glfwGetTime().toDouble()) * 4f)
                        val diffScale = newScale - originalScale

                        pos.y += diffScale.toFloat() * -1f
                        scale.y = newScale.toFloat()

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    } else {
                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    }
                }
            }

            // The right way would be to cache the locations on shader load tbh
            fun setOrthographicProjectionBecauseImLazy(targetProgramId: WebGLProgram) {
                val targetProjectionLocation = glGetUniformLocation(targetProgramId, "projection")

                glUniformMatrix4fv(
                    targetProjectionLocation,
                    false,
                    Matrix4f().ortho(
                        0f,
                        windowWidth.toFloat(),
                        windowHeight.toFloat(),
                        0f,
                        1f,
                        -1f
                    ).getAsFloatArray()
                )
            }

            // Now we apply glow to the coins
            fun renderToFramebuffer(sourceTexture: WebGLTexture, targetFramebuffer: FramebufferResult, targetProgramId: WebGLProgram) {
                // THIS IS HARD!
                HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, targetFramebuffer.framebufferObject)
                // First we render the make yellow onto the framebuffer
                glUseProgram(targetProgramId)

                setOrthographicProjectionBecauseImLazy(targetProgramId)

                val fTimeLocation = glGetUniformLocation(coinProgramId, "fTime")

                glUniform1f(
                    fTimeLocation,
                    glfwGetTime().toFloat()
                )

                drawSprite(targetProgramId, framebufferVAO, sourceTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))
            }

            // Now we do the glow
            renderToFramebuffer(framebufferCoins.framebufferTexture, framebufferYellowCoins, makeYellowProgramId)
            renderToFramebuffer(framebufferYellowCoins.framebufferTexture, framebufferHorizontalBlurCoins, blurHorizontalProgramId)
            renderToFramebuffer(framebufferHorizontalBlurCoins.framebufferTexture, framebufferVerticalBlurCoins, blurVerticalProgramId)

            // Bind the GUI framebuffer
            HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferGui.framebufferObject)

            glUseProgram(textProgramId)
            setOrthographicProjectionBecauseImLazy(textProgramId)

            // Draw text
            drawString(textProgramId, vao, fontImage, Vector3f(0f, 0f, 0f), Vector2f(32f, 32f), "LORITTA IS CUTE :3")
            drawString(textProgramId, vao, fontImage, Vector3f(0f, 32f, 0f), Vector2f(32f, 32f), "MORTES: ${logic.deaths}")
            drawString(textProgramId, vao, fontImage, Vector3f(0f, 64f, 0f), Vector2f(32f, 32f), "GAME TICK: ${logic.elapsedTicks}")
            drawString(
                textProgramId,
                vao,
                fontImage,
                Vector3f(0f, 96f, 0f),
                Vector2f(32f, 32f),
                "FPS: ${(1_000_000_000  / lastDeltaTime.coerceAtLeast(1))} (${(lastDeltaTime / 1_000_000)}MS)"
            )

            // drawCharacter(textProgramId, vao, fontImage, Vector3f(0f, 0f, 0f), Vector2f(128f, 128f), charactersUV['B']!!)

            // Now we UNBIND the framebuffer
            HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, null)

            glUseProgram(programId)
            setOrthographicProjectionBecauseImLazy(programId)

            // And we render the framebuffer to the screen
            drawSprite(programId, framebufferVAO, framebufferGame.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            glBlendFunc(HarmonyGL.GL_ONE, HarmonyGL.GL_ONE)
            drawSprite(programId, framebufferVAO, framebufferVerticalBlurCoins.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            // A detour here...
            glUseProgram(coinProgramId)
            val fTimeLocation = glGetUniformLocation(coinProgramId, "fTime")

            glUniform1f(
                fTimeLocation,
                glfwGetTime()
            )

            setOrthographicProjectionBecauseImLazy(coinProgramId)
            drawSprite(coinProgramId, framebufferVAO, framebufferOverlay.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))


            // glBlendFunc(GL_ONE, GL_ONE)
            glUseProgram(darkenProgramId)
            setOrthographicProjectionBecauseImLazy(darkenProgramId)
            drawSprite(darkenProgramId, framebufferVAO, framebufferGui.framebufferTexture, Vector3f(2f, 2f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            glUseProgram(programId)
            setOrthographicProjectionBecauseImLazy(programId)
            drawSprite(programId, framebufferVAO, framebufferGui.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            val finishedAt = nanoTime()
            // val delta = finishedAt - startedProcessingAt
            // lastDeltaTime = delta
            // totalElapsedMS += delta
            totalElapsedMS += delta
            lastRenderTime = nanoTimestamp

            requestAnimationFrame {
                renderLoop(it)
            }
        }

        requestAnimationFrame {
            renderLoop(it)
        }
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun createSpriteVAO(): WebGLVertexArrayObject {
        val vbo = glGenBuffers()
        val vertices = floatArrayOf(
            // pos      // tex
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        )

        val quadVAO = HarmonyGL.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, null)
        glBindVertexArray(null)

        return quadVAO
    }

    private fun createFramebufferVAO(): WebGLVertexArrayObject {
        val vbo = glGenBuffers()
        // The reason why we don't use the spriteVAO is this
        // https://stackoverflow.com/a/60089044/7271796
        val vertices = floatArrayOf(
            // pos      // tex
            0.0f, 1.0f, 0.0f, 0.0f, // Bottom Left Point
            1.0f, 0.0f, 1.0f, 1.0f, // Top Right Point
            0.0f, 0.0f, 0.0f, 1.0f, // Top Left Point

            0.0f, 1.0f, 0.0f, 0.0f, // Bottom Left Point
            1.0f, 1.0f, 1.0f, 0.0f, // Bottom Right Point
            1.0f, 0.0f, 1.0f, 1.0f, // Top Right Point
        )

        val quadVAO = HarmonyGL.glGenVertexArrays()

        glBindBuffer(HarmonyGL.GL_ARRAY_BUFFER, vbo)
        glBufferData(HarmonyGL.GL_ARRAY_BUFFER, vertices, HarmonyGL.GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, HarmonyGL.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(HarmonyGL.GL_ARRAY_BUFFER, null)
        glBindVertexArray(null)

        return quadVAO
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun createFontAtlasVAO(): WebGLVertexArrayObject {
        val vbo = glGenBuffers()
        val vertices = floatArrayOf(
            // pos      // tex
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 0.0f
        )

        val quadVAO = HarmonyGL.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, null)
        glBindVertexArray(null)

        return quadVAO
    }

    fun drawSprite(programId: WebGLProgram, quadVAO: WebGLVertexArrayObject, texture: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f) {
        drawSprite(programId, quadVAO, texture.textureId, position, size)
    }

    fun drawSprite(programId: WebGLProgram, quadVAO: WebGLVertexArrayObject, textureId: WebGLTexture?, position: Vector3f, size: Vector2f) {
        glUseProgram(programId)

        HarmonyGL.glActiveTexture(GL_TEXTURE0)
        if (textureId != null)
            HarmonyGL.glBindTexture(GL_TEXTURE_2D, textureId)

        val model = Matrix4f()

        model.translate(position.x, position.y, 0.0f)
        model.rotateZ(HarmonyMath.toRadians(position.z.toDouble()).toFloat())
        model.scale(size.x, size.y, 1.0f)
        // model.translate(0.5f * size.x, 0.5f * size.y, 0.0f)

        // glm::rotate(model, glm::radians(rotate), glm::vec3(0.0f, 0.0f, 1.0f));
        // model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f)


        val location = glGetUniformLocation(programId, "model")
        glUniformMatrix4fv(location, false, model.getAsFloatArray())

        glBindVertexArray(quadVAO)
        HarmonyGL.glDrawArrays(HarmonyGL.GL_TRIANGLES, 0, 6)
        glBindVertexArray(null)
    }

    fun drawString(programId: WebGLProgram, quadVAO: WebGLVertexArrayObject, textureId: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f, text: String) {
        val currentPosition = Vector3f(position)
        for (char in text) {
            drawCharacter(programId, quadVAO, textureId, currentPosition, size, charactersUV[char] ?: charactersUV[' ']!!)
            currentPosition.x += size.x
        }
    }

    fun drawCharacter(programId: WebGLProgram, quadVAO: WebGLVertexArrayObject, textureId: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f, texturePos: Vector4f) {
        glUseProgram(programId)

        HarmonyGL.glActiveTexture(HarmonyGL.GL_TEXTURE0)
        HarmonyGL.glBindTexture(HarmonyGL.GL_TEXTURE_2D, textureId.textureId)

        val model = Matrix4f()

        model.translate(position.x, position.y, 0.0f)
        model.rotateZ(HarmonyMath.toRadians(position.z.toDouble()).toFloat())
        model.scale(size.x, size.y, 1.0f)
        // model.translate(0.5f * size.x, 0.5f * size.y, 0.0f)

        // glm::rotate(model, glm::radians(rotate), glm::vec3(0.0f, 0.0f, 1.0f));
        // model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f)

        val location = glGetUniformLocation(programId, "model")
        glUniformMatrix4fv(location, false, model.getAsFloatArray())

        val location2 = glGetUniformLocation(programId, "subImageCoordinates")
        HarmonyGL.glUniform4f(location2, texturePos.x, texturePos.y, texturePos.z, texturePos.w)

        glBindVertexArray(quadVAO)
        HarmonyGL.glDrawArrays(HarmonyGL.GL_TRIANGLES, 0, 6)
        glBindVertexArray(null)
    }

    /**
     * Creates a framebuffer
     */
    fun createFramebuffer(width: Int, height: Int): FramebufferResult {
        val framebufferObject = HarmonyGL.glGenFramebuffers()
        // You NEED to bind the framebuffer due to our future glCheckFramebufferStatus checks
        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, framebufferObject)

        val framebufferTexture = HarmonyGL.glGenTextures()
        HarmonyGL.glBindTexture(HarmonyGL.GL_TEXTURE_2D, framebufferTexture)
        HarmonyGL.glTexImage2D(GL_TEXTURE_2D, 0, HarmonyGL.GL_RGBA, width, height, 0, HarmonyGL.GL_RGBA, HarmonyGL.GL_UNSIGNED_BYTE, null)
        HarmonyGL.glTexParameteri(GL_TEXTURE_2D, HarmonyGL.GL_TEXTURE_MIN_FILTER, HarmonyGL.GL_LINEAR)
        HarmonyGL.glTexParameteri(GL_TEXTURE_2D, HarmonyGL.GL_TEXTURE_MAG_FILTER, HarmonyGL.GL_LINEAR)
        HarmonyGL.glFramebufferTexture2D(HarmonyGL.GL_FRAMEBUFFER, HarmonyGL.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferTexture, 0)

        if (HarmonyGL.glCheckFramebufferStatus(HarmonyGL.GL_FRAMEBUFFER) != HarmonyGL.GL_FRAMEBUFFER_COMPLETE)
            error("Framebuffer is not complete!")

        // Remove bound FB
        HarmonyGL.glBindFramebuffer(HarmonyGL.GL_FRAMEBUFFER, null)

        return FramebufferResult(framebufferObject, framebufferTexture)
    }

    data class FramebufferResult(
        val framebufferObject: WebGLFramebuffer,
        val framebufferTexture: WebGLTexture
    )

    fun mapSinToZeroOne(angle: Double): Double {
        return (sin(angle) + 1) / 2
    }

    fun nanoTime() = (performance.now() * 1e6).toLong() // Convert milliseconds to nanoseconds

    fun glfwGetTime() = (performance.now() / 1000).toFloat()
}