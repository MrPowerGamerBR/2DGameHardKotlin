package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class SquishedGoomba(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.GoombaSquished
    }

    override fun tick() {
        if (this.ticksLived >= 60 * 2)
            this.isAlive = false
    }
}