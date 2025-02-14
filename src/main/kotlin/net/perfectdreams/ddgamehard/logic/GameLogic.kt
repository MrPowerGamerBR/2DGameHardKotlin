package net.perfectdreams.ddgamehard.logic

import kotlinx.serialization.json.Json
import net.perfectdreams.ddgamehard.logic.entities.*
import org.joml.Vector2f
import org.joml.Vector3f
import java.io.File

class GameLogic {
    val keyboard = KeyboardState()
    val cameraCenterPosition = Vector2f()
    val cameraCenterPrevious = Vector2f()
    val cameraCenterTarget   = Vector2f()
    var elapsedTicks = 0
    var deaths = 0

    var room: GameRoom = GameRoom.Level1(this)

    init {
        /* this.entities.add(
            Player(this).apply {
                this.width = 28f
                this.height = 40f

                this.sprite = Sprites.MayWalking1
            }
        )

        repeat(20) {
            this.entities.add(
                BlockYellow(this).apply {
                    this.x = it * 32f
                    this.y = (32f * 16f)

                    this.width = 32f
                    this.height = 32f
                }
            )

            this.entities.add(
                Ground(this).apply {
                    this.x = 256f + (it * 32f)
                    this.y = (32f * 15f)

                    this.width = 32f
                    this.height = 32f
                }
            )

            this.entities.add(
                Ground(this).apply {
                    this.x = it * 32f
                    this.y = (32f * 12f)

                    this.width = 32f
                    this.height = 32f
                }
            )
        }

        this.entities.add(
            Ground(this).apply {
                this.x = 0f
                this.y = (32f * 15f)

                this.width = 32f
                this.height = 32f
            }
        ) */
    }

    fun tick() {
        val room = this.room

        for (entity in room.entities.toList()) {
            entity.tick()

            if (entity is RenderableEntity) {
                if (entity.ticksLived != 0) {
                    entity.renderState.previous = Vector3f(entity.renderState.target)
                } else {
                    entity.renderState.previous = Vector3f(entity.x, entity.y, entity.rotation)
                }

                entity.renderState.target = Vector3f(entity.x, entity.y, entity.rotation)
            }

            entity.ticksLived++
        }

        room.entities.removeAll { !it.isAlive }

        cameraCenterPrevious.set(cameraCenterTarget.x, cameraCenterTarget.y)
        cameraCenterTarget.set(cameraCenterPosition.x, cameraCenterPosition.y)
        elapsedTicks++
    }

    fun switchToRoom(newRoom: GameRoom) {
        this.room = newRoom
        newRoom.start()
    }

    fun goToNextRoom() {
        when (this.room) {
            is GameRoom.Level1 -> switchToRoom(GameRoom.Level2(this))
            else -> {}
        }
    }
}