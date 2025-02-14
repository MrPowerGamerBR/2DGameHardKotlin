package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic

abstract class Entity(val logic: GameLogic) {
    var ticksLived = 0
    var isAlive = true

    abstract fun tick()
}