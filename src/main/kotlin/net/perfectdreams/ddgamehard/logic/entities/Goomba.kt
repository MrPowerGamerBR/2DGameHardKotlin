package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.AABB
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class Goomba(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.Goomba1
    }

    var horizontalSpeed = 0.0f
    var isFlipped = false

    override fun tick() {
        if (isFlipped) {
            this.horizontalSpeed = 3f
        } else {
            this.horizontalSpeed = -3f
        }

        val newX = this.x + this.horizontalSpeed

        val selfHorizontalAABB = AABB(newX, this.y, width, height)

        val room = logic.room
        val entities = room.entities
        for (entity in entities.toList()) {
            if (entity !is RenderableEntity)
                continue

            val otherAABB = AABB(entity.x, entity.y, entity.width, entity.height)

            if (selfHorizontalAABB.intersectsIgnoreTouching(otherAABB)) {
                if (entity is Reversal) {
                    this.isFlipped = !this.isFlipped
                }
            }
        }

        this.sprite = Sprites.Goomba[(this.ticksLived / 16) % 3]

        this.x = newX
    }
}