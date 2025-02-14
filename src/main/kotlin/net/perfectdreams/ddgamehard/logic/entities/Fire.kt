package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites
import org.lwjgl.glfw.GLFW

class Fire(logic: GameLogic) : RenderableEntity(logic) {
    init {
        this.sprite = Sprites.Fire
    }

    override fun tick() {}
}