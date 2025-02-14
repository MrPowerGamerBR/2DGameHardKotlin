package net.perfectdreams.ddgamehard.logic

import kotlinx.serialization.json.Json
import net.perfectdreams.ddgamehard.logic.entities.*
import net.perfectdreams.ddgamehard.Color
import net.perfectdreams.ddgamehard.VIRTUAL_FILE_SYSTEM

sealed class GameRoom(val logic: GameLogic) {
    val entities = mutableListOf<Entity>()
    abstract val topRoomColor: Color
    abstract val bottomRoomColor: Color

    abstract fun start()
    abstract fun tick()
    abstract fun restartRoom()

    fun loadLevel(levelJsonFileName: String) {
        this.entities.clear()

        // Hacky!
        Json.decodeFromString<Root>(VIRTUAL_FILE_SYSTEM.files[levelJsonFileName]!!.decodeToString())
            .instances
            .instance
            .forEach {
                val convertedEntity = when (it._objName) {
                    "obj_Player1" -> {
                        Player(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 28f
                            this.height = 40f

                            this.sprite = Sprites.MayWalking1
                        }
                    }

                    "obj_BlocoAmarelo" -> {
                        BlockYellow(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_BlocoVermelho" -> {
                        BlockRed(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_BlocoRoxo" -> {
                        BlockPurple(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_BlocoAzul" -> {
                        BlockBlue(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_BlocoVerde" -> {
                        BlockGreen(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Finish" -> {
                        Finish(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Fire" -> {
                        Fire(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Hurt" -> {
                        Hurt(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f * it._scaleX
                            this.height = 32f * it._scaleY
                        }
                    }

                    "obj_Goomba" -> {
                        Goomba(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Reversal" -> {
                        Reversal(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Moedas" -> {
                        Coin(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Mola1" -> {
                        YellowSpring(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_BlocoAmareloQSome" -> {
                        VanishingBlockYellow(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_MinecraftGrass" -> {
                        MinecraftGrass(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_MinecraftDirt" -> {
                        MinecraftDirt(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_MinecraftBrick" -> {
                        MinecraftBrick(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f
                            this.height = 32f
                        }
                    }

                    "obj_Windows98Iniciar" -> {
                        Windows98Start(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 224f
                            this.height = 32f
                        }
                    }

                    "obj_Windows98Barra" -> {
                        Windows98Taskbar(logic).apply {
                            this.x = it._x.toFloat()
                            this.y = it._y.toFloat()

                            this.width = 32f * it._scaleX
                            this.height = 32f * it._scaleY
                        }
                    }

                    "obj_GoogleChrome" -> {
                        // This is a bit bad, it would be better to support proper vertical/horizontal
                        // But oh well
                        if (it._rotation.toFloat() == 90f) {
                            SonicteamPowerBlogVertical(logic).apply {
                                this.x = it._x.toFloat()
                                this.y = it._y.toFloat() - 470f
                                // This is negative because that's how GameMaker: Studio saves the rotation
                                // this.rotation = -it._rotation.toFloat()

                                this.width = 371f
                                this.height = 470f
                            }
                        } else {
                            SonicteamPowerBlog(logic).apply {
                                this.x = it._x.toFloat()
                                this.y = it._y.toFloat()
                                // This is negative because that's how GameMaker: Studio saves the rotation
                                this.rotation = -it._rotation.toFloat()

                                this.width = 470f
                                this.height = 371f
                            }
                        }

                    }

                    else -> null
                }

                if (convertedEntity != null) {
                    this.entities.add(convertedEntity)
                }
            }
    }

    class Level1(logic: GameLogic) : GameRoom(logic) {
        override val topRoomColor = Color(200, 0, 0)
        override val bottomRoomColor = Color(0, 0, 0)

        override fun start() {
            loadLevel("level1.json")
        }

        override fun tick() {}

        override fun restartRoom() {
            loadLevel("level1.json")
        }
    }

    class Level2(logic: GameLogic) : GameRoom(logic) {
        override val topRoomColor = Color(0, 20, 170)
        override val bottomRoomColor = Color(0, 0, 0)

        override fun start() {
            loadLevel("level2.json")
        }

        override fun tick() {}

        override fun restartRoom() {
            loadLevel("level2.json")
        }
    }
}