package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.GLFW
import net.perfectdreams.ddgamehard.logic.AABB
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites
import kotlin.math.absoluteValue

class Player(logic: GameLogic) : RenderableEntity(logic) {
    var gravity = 0.0f
    var horizontalSpeed = 0.0f
    var isOnGround = false
    var isGodMode = false

    override fun tick() {
        if (!isGodMode)
            this.gravity += 0.7f

        var newX = this.x
        var newY = this.y

        if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_G)) {
            this.isGodMode = !this.isGodMode
        }

        if (!this.isGodMode) {
            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_D)) {
                this.horizontalSpeed += 0.7f
            }

            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_A)) {
                this.horizontalSpeed -= 0.7f
            }

            if (this.isOnGround && logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_SPACE)) {
                this.gravity -= 16f
            }
        } else {
            logic.cameraCenterPosition.x = this.x
            logic.cameraCenterPosition.y = this.y

            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_UP)) {
                this.y -= 16f
            }

            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_DOWN)) {
                this.y += 16f
            }

            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_LEFT)) {
                this.x -= 16f
            }

            if (logic.keyboard.pressedKeys.contains(GLFW.GLFW_KEY_RIGHT)) {
                this.x += 16f
            }
            return
        }

        // Add some drag
        this.horizontalSpeed = this.horizontalSpeed * 0.95f

        this.horizontalSpeed = this.horizontalSpeed.coerceIn(-15f, 15f)
        if (0.1f >= this.horizontalSpeed.absoluteValue) {
            this.horizontalSpeed = 0.0f
        }

        newX += horizontalSpeed
        newY += this.gravity

        // If we are on ground, this will be reset
        this.isOnGround = false

        // Are we intersecting any ground?
        val selfVerticalAABB = AABB(this.x, newY, width, height)
        val selfHorizontalAABB = AABB(newX, this.y, width, height)

        val room = logic.room
        val entities = room.entities
        for (entity in entities.toList()) {
            if (entity !is RenderableEntity)
                continue

            // Rotation Hack
            val otherAABB = AABB(entity.x, entity.y, entity.width, entity.height)

            if (selfVerticalAABB.intersectsIgnoreTouching(otherAABB)) {
                if (entity is Ground) {
                    if (entity is VanishingBlockYellow)
                        entity.markAsTouched()

                    val yDirection = newY - this.y

                    // // println("yDirection: $yDirection")
                    if (0 > yDirection) {
                        newY = entity.y + entity.height
                        this.gravity = 0.0f
                    }

                    if (yDirection > 0) {
                        // // println("direction is $yDirection")
                        // Reset!
                        newY = entity.y - this.height
                        this.gravity = 0.0f
                        this.isOnGround = true
                    }
                } else if (entity is Hurt) {
                    // Bye!
                    room.restartRoom()
                    logic.deaths++
                    return
                }
            }

            if (selfHorizontalAABB.intersectsIgnoreTouching(otherAABB)) {
                if (entity is Ground) {
                    if (entity is VanishingBlockYellow)
                        entity.markAsTouched()

                    val xDirection = newX - this.x

                    if (xDirection > 0) {
                        newX = entity.x - this.width
                        this.horizontalSpeed = 0.0f
                    }

                    if (0 > xDirection) {
                        newX = entity.x + entity.width
                        this.horizontalSpeed = 0.0f
                    }
                } else if (entity is Hurt) {
                    // Bye!
                    room.restartRoom()
                    logic.deaths++
                    return
                } else if (entity is Goomba) {
                    if (this.gravity > 0f) {
                        // Kill teh goomba!!
                        entity.isAlive = false
                        this.gravity = -12f // Bounce for the fans!

                        // And spawn a new entity
                        room.entities.add(
                            SquishedGoomba(this.logic).apply {
                                this.x = entity.x
                                this.y = entity.y + 20f

                                this.width = 32f
                                this.height = 10f
                            }
                        )
                    } else {
                        // Bye!
                        room.restartRoom()
                        logic.deaths++
                        return
                    }
                }
            }
        }

        this.x = newX
        this.y = newY

        if (this.horizontalSpeed > 0f) {
            this.flipSprite = false
        }

        if (0f > this.horizontalSpeed) {
            this.flipSprite = true
        }

        if (this.horizontalSpeed == 0f) {
            this.sprite = Sprites.MayWalking2
        } else {
            this.sprite = Sprites.MayWalking[(this.ticksLived / 16) % 3]
        }

        logic.cameraCenterPosition.x = this.x
        logic.cameraCenterPosition.y = this.y
    }
}