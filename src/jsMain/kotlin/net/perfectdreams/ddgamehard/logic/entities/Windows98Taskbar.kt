package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class Windows98Taskbar(logic: GameLogic) : Ground(logic) {
    init {
        this.sprite = Sprites.Windows98Taskbar
    }
}