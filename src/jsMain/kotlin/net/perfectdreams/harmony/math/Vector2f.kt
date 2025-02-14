package net.perfectdreams.harmony.math

data class Vector2f(
    var x: Float,
    var y: Float
) {
    constructor() : this(0f, 0f)

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}