package net.perfectdreams.harmony.math

import kotlin.math.sin
import kotlin.math.tan

/**
 * Contains the definition of a 4x4 matrix of floats, and associated functions to transform
 * it. The matrix is column-major to match OpenGL's interpretation, and it looks like this:
 *
 * ```
 * m00  m10  m20  m30
 * m01  m11  m21  m31
 * m02  m12  m22  m32
 * m03  m13  m23  m33
 * ```
 *
 * This is based on JOML's Matrix4f code
 */
class Matrix4f {
    var m00 = 0f
    var m01 = 0f
    var m02 = 0f
    var m03 = 0f
    var m10 = 0f
    var m11 = 0f
    var m12 = 0f
    var m13 = 0f
    var m20 = 0f
    var m21 = 0f
    var m22 = 0f
    var m23 = 0f
    var m30 = 0f
    var m31 = 0f
    var m32 = 0f
    var m33 = 0f


    init {
        this.m00 = 1.0f
        this.m11 = 1.0f
        this.m22 = 1.0f
        this.m33 = 1.0f
    }

    /**
     * Create a new [Matrix4f] and set it to [identity][.identity].
     */
    constructor()

    constructor(source: Matrix4f) {
        this.m00 = source.m00
        this.m01 = source.m01
        this.m02 = source.m02
        this.m03 = source.m03

        this.m10 = source.m10
        this.m11 = source.m11
        this.m12 = source.m12
        this.m13 = source.m13

        this.m20 = source.m20
        this.m21 = source.m21
        this.m22 = source.m22
        this.m23 = source.m23

        this.m30 = source.m30
        this.m31 = source.m31
        this.m32 = source.m32
        this.m33 = source.m33
    }

    fun getAsFloatArray() = get(FloatArray(16), 0)

    fun get(arr: FloatArray, offset: Int): FloatArray {
        arr[offset + 0] = m00
        arr[offset + 1] = m01
        arr[offset + 2] = m02
        arr[offset + 3] = m03

        arr[offset + 4] = m10
        arr[offset + 5] = m11
        arr[offset + 6] = m12
        arr[offset + 7] = m13

        arr[offset + 8] = m20
        arr[offset + 9] = m21
        arr[offset + 10] = m22
        arr[offset + 11] = m23

        arr[offset + 12] = m30
        arr[offset + 13] = m31
        arr[offset + 14] = m32
        arr[offset + 15] = m33

        return arr
    }

    /* fun translate(x: Float, y: Float, z: Float): Matrix4f {
        this.m30 = x
        this.m31 = y
        this.m32 = z

        return this
    } */

    fun translate(x: Float, y: Float, z: Float): Matrix4f {
        this.m30 = HarmonyMath.fma(this.m00, x, HarmonyMath.fma(this.m10, y, HarmonyMath.fma(this.m20, z, this.m30)))
        this.m31 = HarmonyMath.fma(this.m01, x, HarmonyMath.fma(this.m11, y, HarmonyMath.fma(this.m21, z, this.m31)))
        this.m32 = HarmonyMath.fma(this.m02, x, HarmonyMath.fma(this.m12, y, HarmonyMath.fma(this.m22, z, this.m32)))
        this.m33 = HarmonyMath.fma(this.m03, x, HarmonyMath.fma(this.m13, y, HarmonyMath.fma(this.m23, z, this.m33)))

        return this
    }

    fun rotateX(ang: Float) = rotateX(ang, this)

    fun rotateX(ang: Float, dest: Matrix4f): Matrix4f {
        val sin = sin(ang)
        val cos = HarmonyMath.cosFromSin(sin, ang)
        val lm10 = this.m10
        val lm11 = this.m11
        val lm12 = this.m12
        val lm13 = this.m13
        val lm20 = this.m20
        val lm21 = this.m21
        val lm22 = this.m22
        val lm23 =this. m23
        
        return dest.apply {
            this.m20 = HarmonyMath.fma(lm10, -sin, lm20 * cos)
            this.m21 = HarmonyMath.fma(lm11, -sin, lm21 * cos)
            this.m22 = HarmonyMath.fma(lm12, -sin, lm22 * cos)
            this.m23 = HarmonyMath.fma(lm13, -sin, lm23 * cos)
            this.m10 = HarmonyMath.fma(lm10, cos, lm20 * sin)
            this.m11 = HarmonyMath.fma(lm11, cos, lm21 * sin)
            this.m12 = HarmonyMath.fma(lm12, cos, lm22 * sin)
            this.m13 = HarmonyMath.fma(lm13, cos, lm23 * sin)
        }
    }

    fun rotateY(ang: Float) = rotateY(ang, this)

    fun rotateY(ang: Float, dest: Matrix4f): Matrix4f {
        val sin = sin(ang)
        val cos = HarmonyMath.cosFromSin(sin, ang)

        // add temporaries for dependent values
        val nm00 = HarmonyMath.fma(this.m00, cos, this.m20 * -sin)
        val nm01 = HarmonyMath.fma(this.m01, cos, this.m21 * -sin)
        val nm02 = HarmonyMath.fma(this.m02, cos, this.m22 * -sin)
        val nm03 = HarmonyMath.fma(this.m03, cos, this.m23 * -sin)

        // set non-dependent values directly

        return dest.apply {
            this.m20 = HarmonyMath.fma(this.m00, sin, this.m20 * cos)
            this.m21 = HarmonyMath.fma(this.m01, sin, this.m21 * cos)
            this.m22 = HarmonyMath.fma(this.m02, sin, this.m22 * cos)
            this.m23 = HarmonyMath.fma(this.m03, sin, this.m23 * cos)

            // set other values
            this.m00 = nm00
            this.m01 = nm01
            this.m02 = nm02
            this.m03 = nm03
        }
    }

    fun rotateZ(ang: Float): Matrix4f {
        return rotateZInternal(ang, this)
    }

    private fun rotateZInternal(ang: Float, dest: Matrix4f): Matrix4f {
        val sin: Float = sin(ang)
        val cos: Float = HarmonyMath.cosFromSin(sin, ang)
        return rotateTowardsXY(sin, cos, dest)
    }


    /**
     * Apply rotation about the Z axis to align the local `+X` towards `(dirX, dirY)`.
     *
     *
     * If `M` is `this` matrix and `R` the rotation matrix,
     * then the new matrix will be `M * R`. So when transforming a
     * vector `v` with the new matrix by using `M * R * v`, the
     * rotation will be applied first!
     *
     *
     * The vector `(dirX, dirY)` must be a unit vector.
     *
     * @param dirX
     * the x component of the normalized direction
     * @param dirY
     * the y component of the normalized direction
     * @return this
     */
    fun rotateTowardsXY(dirX: Float, dirY: Float): Matrix4f {
        return rotateTowardsXY(dirX, dirY, this)
    }

    fun rotateTowardsXY(dirX: Float, dirY: Float, dest: Matrix4f): Matrix4f {
        val nm00 = HarmonyMath.fma(this.m00, dirY, this.m10 * dirX)
        val nm01 = HarmonyMath.fma(this.m01, dirY, this.m11 * dirX)
        val nm02 = HarmonyMath.fma(this.m02, dirY, this.m12 * dirX)
        val nm03 = HarmonyMath.fma(this.m03, dirY, this.m13 * dirX)

        return dest.apply {
            this.m10 = HarmonyMath.fma(this.m00, -dirX, this.m10 * dirY)
            this.m11 = HarmonyMath.fma(this.m01, -dirX, this.m11 * dirY)
            this.m12 = HarmonyMath.fma(this.m02, -dirX, this.m12 * dirY)
            this.m13 = HarmonyMath.fma(this.m03, -dirX, this.m13 * dirY)

            this.m00 = nm00
            this.m01 = nm01
            this.m02 = nm02
            this.m03 = nm03
        }
    }

    fun scale(x: Float, y: Float, z: Float): Matrix4f {
        return scale(x, y, z, this)
    }

    fun scale(x: Float, y: Float, z: Float, dest: Matrix4f): Matrix4f {
        return dest.apply {
            this.m00 = this.m00 * x
            this.m01 = this.m01 * x
            this.m02 = this.m02 * x
            this.m03 = this.m03 * x

            this.m10 = this.m10 * y
            this.m11 = this.m11 * y
            this.m12 = this.m12 * y
            this.m13 = this.m13 * y

            this.m20 = this.m20 * z
            this.m21 = this.m21 * z
            this.m22 = this.m22 * z
            this.m23 = this.m23 * z
        }
    }


    /* fun ortho2(
        left: Float,
        right: Float,
        bottom: Float,
        top: Float,
        zNear: Float,
        zFar: Float,
        zZeroToOne: Boolean
    ) {
        this.m00 = 2.0f / (right - left)
        this.m11 = 2.0f / (top - bottom)
        this.m22 = (if (zZeroToOne) 1.0f else 2.0f) / (zNear - zFar)

        this.m30 = (right + left) / (left - right)
        this.m31 = (top + bottom) / (bottom - top)
        this.m32 = (if (zZeroToOne) zNear else (zFar + zNear)) / (zNear - zFar)
    } */

    fun ortho(
        left: Float,
        right: Float,
        bottom: Float,
        top: Float,
        zNear: Float,
        zFar: Float
    ): Matrix4f {
        return ortho(left, right, bottom, top, zNear, zFar, false)
    }

    fun ortho(
        left: Float,
        right: Float,
        bottom: Float,
        top: Float,
        zNear: Float,
        zFar: Float,
        zZeroToOne: Boolean
    ): Matrix4f {
        return ortho(left, right, bottom, top, zNear, zFar, zZeroToOne, this)
    }

    fun ortho(
        left: Float,
        right: Float,
        bottom: Float,
        top: Float,
        zNear: Float,
        zFar: Float,
        zZeroToOne: Boolean,
        dest: Matrix4f
    ): Matrix4f {
        // calculate right matrix elements
        val rm00 = 2.0f / (right - left)
        val rm11 = 2.0f / (top - bottom)
        val rm22 = (if (zZeroToOne) 1.0f else 2.0f) / (zNear - zFar)
        val rm30 = (left + right) / (left - right)
        val rm31 = (top + bottom) / (bottom - top)
        val rm32 = (if (zZeroToOne) zNear else (zFar + zNear)) / (zNear - zFar)

        //         destthis.m30 = m00() * rm30 + m10() * rm31 + m20() * rm32 + m30()
        //            this.m31 = m01() * rm30 + m11() * rm31 + m21() * rm32 + m31()
        //            this.m32 = m02() * rm30 + m12() * rm31 + m22() * rm32 + m32()
        //            this.m33 = m03() * rm30 + m13() * rm31 + m23() * rm32 + m33()
        //            this.m00 = m00() * rm00
        //            this.m01 = m01() * rm00
        //            this.m02 = m02() * rm00
        //            this.m03 = m03() * rm00
        //            this.m10 = m10() * rm11
        //            this.m11 = m11() * rm11
        //            this.m12 = m12() * rm11
        //            this.m13 = m13() * rm11
        //            this.m20 = m20() * rm22
        //            this.m21 = m21() * rm22
        //            this.m22 = m22() * rm22
        //            this.m23 = m23() * rm22

        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        dest.m30 = this.m00 * rm30 + this.m10 * rm31 + this.m20 * rm32 + this.m30
        dest.m31 = this.m01 * rm30 + this.m11 * rm31 + this.m21 * rm32 + this.m31
        dest.m32 = this.m02 * rm30 + this.m12 * rm31 + this.m22 * rm32 + this.m32
        dest.m33 = this.m03 * rm30 + this.m13 * rm31 + this.m23 * rm32 + this.m33

        dest.m00 = dest.m00 * rm00
        dest.m01 = dest.m01 * rm00
        dest.m02 = dest.m02 * rm00
        dest.m03 = dest.m03 * rm00

        dest.m10 = dest.m10 * rm11
        dest.m11 = dest.m11 * rm11
        dest.m12 = dest.m12 * rm11
        dest.m13 = dest.m13 * rm11

        dest.m20 = dest.m20 * rm22
        dest.m21 = dest.m21 * rm22
        dest.m22 = dest.m22 * rm22
        dest.m23 = dest.m23 * rm22

        return dest
    }

    fun perspective(
        fovy: Float,
        aspect: Float,
        zNear: Float,
        zFar: Float,
        zZeroToOne: Boolean
    ): Matrix4f {
        return perspective(fovy, aspect, zNear, zFar, zZeroToOne, this)
    }

     fun perspective(
        fovy: Float,
        aspect: Float,
        zNear: Float,
        zFar: Float,
        zZeroToOne: Boolean,
        dest: Matrix4f
    ): Matrix4f {
        val h: Float = tan(fovy * 0.5f)
        // calculate right matrix elements
        val rm00 = 1.0f / (h * aspect)
        val rm11 = 1.0f / h
        val rm22: Float
        val rm32: Float
        val farInf = zFar > 0 && zFar.isInfinite()
        val nearInf = zNear > 0 && zNear.isInfinite()
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            val e = 1E-6f
            rm22 = e - 1.0f
            rm32 = (e - (if (zZeroToOne) 1.0f else 2.0f)) * zNear
        } else if (nearInf) {
            val e = 1E-6f
            rm22 = (if (zZeroToOne) 0.0f else 1.0f) - e
            rm32 = ((if (zZeroToOne) 1.0f else 2.0f) - e) * zFar
        } else {
            rm22 = (if (zZeroToOne) zFar else zFar + zNear) / (zNear - zFar)
            rm32 = (if (zZeroToOne) zFar else zFar + zFar) * zNear / (zNear - zFar)
        }
        // perform optimized matrix multiplication
        val nm20: Float = this.m20 * rm22 - this.m30
        val nm21: Float = this.m21 * rm22 - this.m31
        val nm22: Float = this.m22 * rm22 - this.m32
        val nm23: Float = this.m23 * rm22 - this.m33

        dest.apply {
            this.m00 = this.m00 * rm00
            this.m01 = this.m01 * rm00
            this.m02 = this.m02 * rm00
            this.m03 = this.m03 * rm00

            this.m10 = this.m10 * rm11
            this.m11 = this.m11 * rm11
            this.m12 = this.m12 * rm11
            this.m13 = this.m13 * rm11

            this.m30 = this.m20 * rm32
            this.m31 = this.m21 * rm32
            this.m32 = this.m22 * rm32
            this.m33 = this.m23 * rm32

            this.m20 = nm20
            this.m21 = nm21
            this.m22 = nm22
            this.m23 = nm23
        }
        return dest
    }

    fun lookAtGeneric(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upX: Float, upY: Float, upZ: Float, dest: Matrix4f
    ): Matrix4f {
        // Compute direction from position to lookAt
        var dirX = eyeX - centerX
        var dirY = eyeY - centerY
        var dirZ = eyeZ - centerZ
        // Normalize direction
        val invDirLength = HarmonyMath.invsqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        dirX *= invDirLength
        dirY *= invDirLength
        dirZ *= invDirLength
        // left = up x direction
        var leftX = upY * dirZ - upZ * dirY
        var leftY = upZ * dirX - upX * dirZ
        var leftZ = upX * dirY - upY * dirX
        // normalize left
        val invLeftLength = HarmonyMath.invsqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)
        leftX *= invLeftLength
        leftY *= invLeftLength
        leftZ *= invLeftLength
        // up = direction x left
        val upnX = dirY * leftZ - dirZ * leftY
        val upnY = dirZ * leftX - dirX * leftZ
        val upnZ = dirX * leftY - dirY * leftX

        // calculate right matrix elements
        val rm30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ)
        val rm31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ)
        val rm32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ)
        // introduce temporaries for dependent results
        val nm00: Float = this.m00 * leftX + this.m10 * upnX + this.m20 * dirX
        val nm01: Float = this.m01 * leftX + this.m11 * upnX + this.m21 * dirX
        val nm02: Float = this.m02 * leftX + this.m12 * upnX + this.m22 * dirX
        val nm03: Float = this.m03 * leftX + this.m13 * upnX + this.m23 * dirX
        val nm10: Float = this.m00 * leftY + this.m10 * upnY + this.m20 * dirY
        val nm11: Float = this.m01 * leftY + this.m11 * upnY + this.m21 * dirY
        val nm12: Float = this.m02 * leftY + this.m12 * upnY + this.m22 * dirY
        val nm13: Float = this.m03 * leftY + this.m13 * upnY + this.m23 * dirY

        // perform optimized matrix multiplication
        // compute last column first, because others do not depend on it
        return dest.apply {
            this.m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30
            this.m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31
            this.m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32
            this.m33 = m03 * rm30 + m13 * rm31 + m23 * rm32 + m33

            this.m20 = m00 * leftZ + m10 * upnZ + m20 * dirZ
            this.m21 = m01 * leftZ + m11 * upnZ + m21 * dirZ
            this.m22 = m02 * leftZ + m12 * upnZ + m22 * dirZ
            this.m23 = m03 * leftZ + m13 * upnZ + m23 * dirZ

            this.m00 = nm00
            this.m01 = nm01
            this.m02 = nm02
            this.m03 = nm03

            this.m10 = nm10
            this.m11 = nm11
            this.m12 = nm12
            this.m13 = nm13
        }
    }

    fun invert(): Matrix4f {
        val a: Float = this.m00 * this.m11 - this.m01 * this.m10
        val b: Float = this.m00 * this.m12 - this.m02 * this.m10
        val c: Float = this.m00 * this.m13 - this.m03 * this.m10
        val d: Float = this.m01 * this.m12 - this.m02 * this.m11
        val e: Float = this.m01 * this.m13 - this.m03 * this.m11
        val f: Float = this.m02 * this.m13 - this.m03 * this.m12
        val g: Float = this.m20 * this.m31 - this.m21 * this.m30
        val h: Float = this.m20 * this.m32 - this.m22 * this.m30
        val i: Float = this.m20 * this.m33 - this.m23 * this.m30
        val j: Float = this.m21 * this.m32 - this.m22 * this.m31
        val k: Float = this.m21 * this.m33 - this.m23 * this.m31
        val l: Float = this.m22 * this.m33 - this.m23 * this.m32
        var det = a * l - b * k + c * j + d * i - e * h + f * g
        det = 1.0f / det
        val nm00: Float = HarmonyMath.fma(m11, l, HarmonyMath.fma(-m12, k, m13 * j)) * det
        val nm01: Float = HarmonyMath.fma(-m01, l, HarmonyMath.fma(m02, k, -m03 * j)) * det
        val nm02: Float = HarmonyMath.fma(m31, f, HarmonyMath.fma(-m32, e, m33 * d)) * det
        val nm03: Float = HarmonyMath.fma(-m21, f, HarmonyMath.fma(m22, e, -m23 * d)) * det
        val nm10: Float = HarmonyMath.fma(-m10, l, HarmonyMath.fma(m12, i, -m13 * h)) * det
        val nm11: Float = HarmonyMath.fma(m00, l, HarmonyMath.fma(-m02, i, m03 * h)) * det
        val nm12: Float = HarmonyMath.fma(-m30, f, HarmonyMath.fma(m32, c, -m33 * b)) * det
        val nm13: Float = HarmonyMath.fma(m20, f, HarmonyMath.fma(-m22, c, m23 * b)) * det
        val nm20: Float = HarmonyMath.fma(m10, k, HarmonyMath.fma(-m11, i, m13 * g)) * det
        val nm21: Float = HarmonyMath.fma(-m00, k, HarmonyMath.fma(m01, i, -m03 * g)) * det
        val nm22: Float = HarmonyMath.fma(m30, e, HarmonyMath.fma(-m31, c, m33 * a)) * det
        val nm23: Float = HarmonyMath.fma(-m20, e, HarmonyMath.fma(m21, c, -m23 * a)) * det
        val nm30: Float = HarmonyMath.fma(-m10, j, HarmonyMath.fma(m11, h, -m12 * g)) * det
        val nm31: Float = HarmonyMath.fma(m00, j, HarmonyMath.fma(-m01, h, m02 * g)) * det
        val nm32: Float = HarmonyMath.fma(-m30, d, HarmonyMath.fma(m31, b, -m32 * a)) * det
        val nm33: Float = HarmonyMath.fma(m20, d, HarmonyMath.fma(-m21, b, m22 * a)) * det

        return this.apply {
            this.m00 = nm00
            this.m01 = nm01
            this.m02 = nm02
            this.m03 = nm03
            this.m10 = nm10
            this.m11 = nm11
            this.m12 = nm12
            this.m13 = nm13
            this.m20 = nm20
            this.m21 = nm21
            this.m22 = nm22
            this.m23 = nm23
            this.m30 = nm30
            this.m31 = nm31
            this.m32 = nm32
            this.m33 = nm33
        }
    }
}