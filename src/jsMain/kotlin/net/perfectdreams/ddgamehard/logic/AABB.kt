package net.perfectdreams.ddgamehard.logic

data class AABB(val x: Float, val y: Float, val width: Float, val height: Float) {
    val minX = x
    val minY = y
    val maxX = x + width
    val maxY = y + height

    fun intersects(other: AABB): Boolean {
        return (this.minX <= other.maxX && this.maxX >= other.minX) &&
                (this.minY <= other.maxY && this.maxY >= other.minY)
    }

    fun intersectsIgnoreTouching(other: AABB): Boolean {
        return (this.minX < other.maxX && this.maxX > other.minX) &&
                (this.minY < other.maxY && this.maxY > other.minY)
    }
}