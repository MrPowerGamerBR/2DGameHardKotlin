package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class BlockRed(logic: GameLogic) : Ground(logic) {
    init {
        this.sprite = Sprites.BlockRed
    }
}