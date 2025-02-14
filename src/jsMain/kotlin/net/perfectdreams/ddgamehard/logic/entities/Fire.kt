package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class Fire(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.Fire
    }

    override fun tick() {}
}