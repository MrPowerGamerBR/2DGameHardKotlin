package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.AABB
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.GameRoom
import net.perfectdreams.ddgamehard.logic.Sprites
import org.lwjgl.glfw.GLFW

class Finish(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.Finish
    }

    override fun tick() {
        // Are we colliding with a player?
        val selfAABB = AABB(this.x, this.y, width, height)

        val room = logic.room
        val entities = room.entities
        for (entity in entities.toList()) {
            if (entity !is Player)
                continue

            val otherAABB = AABB(entity.x, entity.y, entity.width, entity.height)

            if (selfAABB.intersectsIgnoreTouching(otherAABB)) {
                logic.goToNextRoom()
            }
        }
    }
}