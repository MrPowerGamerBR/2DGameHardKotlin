package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.AABB
import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites
import org.lwjgl.glfw.GLFW

class YellowSpring(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.YellowSpring
    }

    private var lastUsedAtTick = 0

    override fun tick() {
        val diff = logic.elapsedTicks - this.lastUsedAtTick
        if (30 > diff)
            return

        // Are we colliding with a player?
        val selfAABB = AABB(this.x, this.y, width, height)

        val room = logic.room
        val entities = room.entities
        for (entity in entities.toList()) {
            if (entity !is Player)
                continue

            val otherAABB = AABB(entity.x, entity.y, entity.width, entity.height)

            if (selfAABB.intersectsIgnoreTouching(otherAABB)) {
                entity.gravity = -20f
                this.lastUsedAtTick = logic.elapsedTicks
            }
        }
    }
}