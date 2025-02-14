package net.perfectdreams.harmony.math

data class Vector3f(
    var x: Float,
    var y: Float,
    var z: Float
) {
    constructor(vector3f: Vector3f) : this(vector3f.x, vector3f.y, vector3f.z)
}