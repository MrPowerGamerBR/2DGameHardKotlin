package net.perfectdreams.ddgamehard

import net.perfectdreams.ddgamehard.logic.Easings
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.GameRoom
import net.perfectdreams.ddgamehard.logic.Sprites
import net.perfectdreams.ddgamehard.logic.entities.Coin
import net.perfectdreams.ddgamehard.logic.entities.Fire
import net.perfectdreams.ddgamehard.logic.entities.Player
import net.perfectdreams.ddgamehard.logic.entities.RenderableEntity
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glUseProgram
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.GL43
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer
import kotlin.math.sin

class DDGameHard {
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
        println("Hello LWJGL " + Version.getVersion() + "!")

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

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        // Enable core profile
        glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)

        // Create the window
        window = glfwCreateWindow(windowWidth, windowHeight, "2D Game Hard (Kotlin)", NULL, NULL)
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            // We will detect this in the rendering loop
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(
                    window,
                    true
                )
            }

            if (action == GLFW_RELEASE && key == GLFW_KEY_PAGE_UP) {
                logic.goToNextRoom()
            }

            if (action == GLFW_PRESS) {
                logic.keyboard.pressedKeys.add(key)
            }

            if (action == GLFW_RELEASE) {
                logic.keyboard.pressedKeys.remove(key)
            }
        }

        stackPush().use { stack ->
            val pWidth: IntBuffer = stack.mallocInt(1) // int*
            val pHeight: IntBuffer = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth[0]) / 2,
                (vidmode.height() - pHeight[0]) / 2
            )
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync (to disable, use 0)
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Required for transparent textures!
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GL43.glEnable(GL43.GL_DEBUG_OUTPUT)
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS)
        GL43.glDebugMessageCallback({ source: Int, type: Int, id: Int, severity: Int, length: Int, messagePointer: Long, userParamPointer: Long ->
            val debugMessage: String = MemoryUtil.memUTF8(messagePointer, length)

            val sourceStr = when (source) {
                GL43.GL_DEBUG_SOURCE_API -> "API"
                else -> "Unknown ($source)"
            }
            println("[$sourceStr]: $debugMessage")

            try {
                error("test")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MemoryUtil.NULL)

        // Set the clear color
        glClearColor(0.8f, 0.0f, 0.0f, 1.0f)

        val shaderManager = ShaderManager()
        val programId = shaderManager.loadShader("game.vsh", "game.fsh")

        val makeYellowProgramId = shaderManager.loadShader("coin.vsh", "makeyellow.fsh")
        val blurHorizontalProgramId = shaderManager.loadShader("coin.vsh", "horizontal_blur.fsh")
        val blurVerticalProgramId = shaderManager.loadShader("coin.vsh", "vertical_blur.fsh")
        val coinProgramId = shaderManager.loadShader("coin.vsh", "coin.fsh")
        val textProgramId = shaderManager.loadShader("text.vsh", "text.fsh")
        val darkenProgramId = shaderManager.loadShader("text.vsh", "darken.fsh")
        val gradientProgramId = shaderManager.loadShader("game.vsh", "background.fsh")

        glUseProgram(programId)

        val vao = createSpriteVAO()
        val framebufferVAO = createFramebufferVAO()

        val textureFileNameToTextureIds = mutableMapOf<String, ResourceManager.LoadedImage>()

        val resourceManager = ResourceManager()

        for (sprite in Sprites.registeredSprites) {
            textureFileNameToTextureIds[sprite.fileName] = resourceManager.loadTexture(sprite.fileName)
        }

        // Load font
        val fontImage = resourceManager.loadTexture("chars_transparent.png")

        var totalElapsedMS = PHYSICS_TIME.toLong() // We always want first render tick to ALSO do a physics tick to process the game

        var lastDeltaTime: Long = 0
        var lastGamePhysicsUpdate = System.nanoTime()

        val framebufferGame = createFramebuffer(windowWidth, windowHeight)
        val framebufferCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferYellowCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferHorizontalBlurCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferVerticalBlurCoins = createFramebuffer(windowWidth, windowHeight)
        val framebufferOverlay = createFramebuffer(windowWidth, windowHeight)
        val framebufferGui = createFramebuffer(windowWidth, windowHeight)

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            // // println("Processing render...")

            val startedProcessingAt = System.nanoTime()

            // Clear the default framebuffer (this is not really needed but it is useful when testing effects)
            GL43.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0)
            GL43.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            fun clearFramebuffer(framebuffer: FramebufferResult) {
                // Clear the framebuffer
                GL43.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebuffer.framebufferObject)
                GL43.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
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
            GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

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

            // println("Interpolation Percent: $interpolationPercent")

            GL11.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

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
                ).get(FloatArray(16))

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

                drawSprite(gradientProgramId, framebufferVAO, 0, Vector3f(-(gameWidth / 2f), -(gameHeight / 2f), 0f), Vector2f(10000f, 2000f))
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
            ).get(FloatArray(16))

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
                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferCoins.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferOverlay.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    } else if (entity is Fire) {
                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

                        // Attempting to animate this using this instead of shaders
                        val originalScale = scale.y
                        val newScale = scale.y - (mapSinToZeroOne(glfwGetTime()) * 4f)
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

                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    } else {
                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)

                        drawSprite(
                            programId,
                            vao,
                            textureFileNameToTextureIds[entity.sprite.fileName]!!,
                            pos,
                            scale
                        )

                        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGame.framebufferObject)
                    }
                }
            }

            // The right way would be to cache the locations on shader load tbh
            fun setOrthographicProjectionBecauseImLazy(targetProgramId: Int) {
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
                    ).get(FloatArray(16))
                )
            }

            // Now we apply glow to the coins
            fun renderToFramebuffer(sourceTexture: Int, targetFramebuffer: FramebufferResult, targetProgramId: Int) {
                // THIS IS HARD!
                GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, targetFramebuffer.framebufferObject)
                // First we render the make yellow onto the framebuffer
                glUseProgram(targetProgramId)

                setOrthographicProjectionBecauseImLazy(targetProgramId)

                val fTimeLocation = glGetUniformLocation(coinProgramId, "fTime")

                glUniform1f(
                    fTimeLocation,
                    GLFW.glfwGetTime().toFloat()
                )

                drawSprite(targetProgramId, framebufferVAO, sourceTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))
            }

            // Now we do the glow
            renderToFramebuffer(framebufferCoins.framebufferTexture, framebufferYellowCoins, makeYellowProgramId)
            renderToFramebuffer(framebufferYellowCoins.framebufferTexture, framebufferHorizontalBlurCoins, blurHorizontalProgramId)
            renderToFramebuffer(framebufferHorizontalBlurCoins.framebufferTexture, framebufferVerticalBlurCoins, blurVerticalProgramId)

            // Bind the GUI framebuffer
            GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferGui.framebufferObject)

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
            GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0)

            glUseProgram(programId)
            setOrthographicProjectionBecauseImLazy(programId)

            // And we render the framebuffer to the screen
            drawSprite(programId, framebufferVAO, framebufferGame.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            glBlendFunc(GL_ONE, GL_ONE)
            drawSprite(programId, framebufferVAO, framebufferVerticalBlurCoins.framebufferTexture, Vector3f(0f, 0f, 0f), Vector2f(windowWidth.toFloat(), windowHeight.toFloat()))

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            // A detour here...
            glUseProgram(coinProgramId)
            val fTimeLocation = glGetUniformLocation(coinProgramId, "fTime")

            glUniform1f(
                fTimeLocation,
                GLFW.glfwGetTime().toFloat()
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


            // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()

            val finishedAt = System.nanoTime()
            val delta = finishedAt - startedProcessingAt
            lastDeltaTime = delta
            totalElapsedMS += delta
        }
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun createSpriteVAO(): Int {
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

        val quadVAO = GL32.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        return quadVAO
    }

    private fun createFramebufferVAO(): Int {
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

        val quadVAO = GL32.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        return quadVAO
    }

    // VAO = Vertex Array Object
    // VBO = Vertex Buffer Object
    private fun createFontAtlasVAO(): Int {
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

        val quadVAO = GL32.glGenVertexArrays()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(quadVAO)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        return quadVAO
    }

    fun drawSprite(programId: Int, quadVAO: Int, texture: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f) {
        drawSprite(programId, quadVAO, texture.textureId, position, size)
    }

    fun drawSprite(programId: Int, quadVAO: Int, textureId: Int, position: Vector3f, size: Vector2f) {
        glUseProgram(programId)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)

        val model = Matrix4f()

        model.translate(position.x, position.y, 0.0f)
        model.rotateZ(Math.toRadians(position.z.toDouble()).toFloat())
        model.scale(size.x, size.y, 1.0f)
        // model.translate(0.5f * size.x, 0.5f * size.y, 0.0f)

        // glm::rotate(model, glm::radians(rotate), glm::vec3(0.0f, 0.0f, 1.0f));
        // model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f)


        val location = glGetUniformLocation(programId, "model")
        glUniformMatrix4fv(location, false, model.get(FloatArray(16)))

        glBindVertexArray(quadVAO)
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }

    fun drawString(programId: Int, quadVAO: Int, textureId: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f, text: String) {
        val currentPosition = Vector3f(position)
        for (char in text) {
            drawCharacter(programId, quadVAO, textureId, currentPosition, size, charactersUV[char] ?: charactersUV[' ']!!)
            currentPosition.x += size.x
        }
    }

    fun drawCharacter(programId: Int, quadVAO: Int, textureId: ResourceManager.LoadedImage, position: Vector3f, size: Vector2f, texturePos: Vector4f) {
        glUseProgram(programId)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId.textureId)

        val model = Matrix4f()

        model.translate(position.x, position.y, 0.0f)
        model.rotateZ(Math.toRadians(position.z.toDouble()).toFloat())
        model.scale(size.x, size.y, 1.0f)
        // model.translate(0.5f * size.x, 0.5f * size.y, 0.0f)

        // glm::rotate(model, glm::radians(rotate), glm::vec3(0.0f, 0.0f, 1.0f));
        // model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f)


        val location = glGetUniformLocation(programId, "model")
        glUniformMatrix4fv(location, false, model.get(FloatArray(16)))

        val location2 = glGetUniformLocation(programId, "subImageCoordinates")
        glUniform4f(location2, texturePos.x, texturePos.y, texturePos.z, texturePos.w)

        glBindVertexArray(quadVAO)
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }

    /**
     * Creates a framebuffer
     */
    fun createFramebuffer(width: Int, height: Int): FramebufferResult {
        val framebufferObject = GL43.glGenFramebuffers()
        // You NEED to bind the framebuffer due to our future glCheckFramebufferStatus checks
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, framebufferObject)

        val framebufferTexture = GL43.glGenTextures()
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, framebufferTexture)
        GL43.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL)
        GL43.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        GL43.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, GL43.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferTexture, 0)

        if (GL43.glCheckFramebufferStatus(GL43.GL_FRAMEBUFFER) != GL43.GL_FRAMEBUFFER_COMPLETE)
            error("Framebuffer is not complete!")

        // Remove bound FB
        GL43.glBindFramebuffer(GL43.GL_FRAMEBUFFER, 0)

        return FramebufferResult(framebufferObject, framebufferTexture)
    }

    data class FramebufferResult(
        val framebufferObject: Int,
        val framebufferTexture: Int
    )

    fun mapSinToZeroOne(angle: Double): Double {
        return (Math.sin(angle) + 1) / 2
    }
}