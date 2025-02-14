package net.perfectdreams.ddgamehard.logic.entities

import net.perfectdreams.ddgamehard.logic.GameLogic
import net.perfectdreams.ddgamehard.logic.Sprite
import net.perfectdreams.ddgamehard.logic.Sprites
import org.joml.Vector2f
import org.joml.Vector3f

abstract class RenderableEntity(logic: GameLogic) : Entity(logic) {
    val renderState = RenderState(
        Vector3f(0.0f, 0.0f, 0.0f),
        Vector3f(0.0f, 0.0f, 0.0f)
    )

    var isVisible = true
    var x = 0.0f
    var y = 0.0f
    var width = 0.0f
    var height = 0.0f
    var sprite: Sprite = Sprites.MissingTexture
    var flipSprite = false
    var rotation = 0f

    data class RenderState(
        var previous: Vector3f,
        var target: Vector3f
    )
}