package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class Reversal(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.isVisible = false
    }

    override fun tick() {}
}