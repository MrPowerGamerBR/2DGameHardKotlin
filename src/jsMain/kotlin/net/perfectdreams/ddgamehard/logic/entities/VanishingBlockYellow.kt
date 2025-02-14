package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprites

class VanishingBlockYellow(logic: GameLogic) : Ground(logic) {
    init {
        this.sprite = Sprites.BlockYellow
    }

    var touchedAtTick = -1

    override fun tick() {
        if (this.touchedAtTick == -1)
            return

        val diff = logic.elapsedTicks - this.touchedAtTick

        if (diff >= 60) {
            isAlive = false
        }
    }

    fun markAsTouched() {
        if (this.touchedAtTick != -1)
            return

        this.touchedAtTick = logic.elapsedTicks
    }
}