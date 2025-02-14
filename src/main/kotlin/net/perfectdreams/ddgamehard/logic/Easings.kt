package net.perfectdreams.ddgamehard.logic

object Easings {
    fun easeLinear(start: Float, end: Float, percent: Float): Float {
        return start+(end-start)*percent
    }

    fun easeLinear(start: Double, end: Double, percent: Double): Double {
        return start+(end-start)*percent
    }
}