package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class Hurt(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.Fire
        this.isVisible = false
    }

    override fun tick() {}
}