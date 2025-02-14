package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites
import org.lwjgl.glfw.GLFW

class BlockPurple(logic: GameLogic) : Ground(logic) {
    init {
        this.sprite = Sprites.BlockPurple
    }
}