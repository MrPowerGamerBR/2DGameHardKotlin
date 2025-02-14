package net.perfectdreams.harmony.math

data class Vector4f(
    var x: Float,
    var y: Float,
    var z: Float,
    var w: Float
) {
    fun mul(mat: Matrix4f): Vector4f {
        return mul(mat, this)
    }

    fun mul(mat: Matrix4f, dest: Vector4f): Vector4f {
        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w
        dest.x = HarmonyMath.fma(mat.m00, x, HarmonyMath.fma(mat.m10, y, HarmonyMath.fma(mat.m20, z, mat.m30 * w)))
        dest.y = HarmonyMath.fma(mat.m01, x, HarmonyMath.fma(mat.m11, y, HarmonyMath.fma(mat.m21, z, mat.m31 * w)))
        dest.z = HarmonyMath.fma(mat.m02, x, HarmonyMath.fma(mat.m12, y, HarmonyMath.fma(mat.m22, z, mat.m32 * w)))
        dest.w = HarmonyMath.fma(mat.m03, x, HarmonyMath.fma(mat.m13, y, HarmonyMath.fma(mat.m23, z, mat.m33 * w)))
        return dest
    }

    /**
     * Divide all components of this vector by the given scalar
     * value.
     *
     * @param scalar
     * the scalar to divide by
     * @return this
     */
    fun div(scalar: Float): Vector4f {
        return div(scalar, this)
    }

    fun div(scalar: Float, dest: Vector4f): Vector4f {
        val inv = 1.0f / scalar
        dest.x = x * inv
        dest.y = y * inv
        dest.z = z * inv
        dest.w = w * inv
        return dest
    }
}