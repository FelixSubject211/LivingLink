package com.felix.livinglink.server.core.domain

import org.koin.core.annotation.Single
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

@Single(binds = [OrderKeyProvider::class])
class FractionalIndexOrderKeyProvider : OrderKeyProvider {
    override fun between(before: String?, after: String?): String =
        FractionalIndexing.generateFractionalIndexBetween(before, after)

    override fun nKeysBetween(before: String?, after: String?, count: Int): List<String> {
        require(count >= 0) { "count must be >= 0" }
        if (count == 0) return emptyList()
        return FractionalIndexing.generateNFractionalIndicesBetween(before, after, count)
    }

    override fun jitter(key: String): String =
        key + FractionalIndexing.randomFractionSuffix(JITTER_LENGTH)

    companion object {
        private const val JITTER_LENGTH = 4
    }
}

private object FractionalIndexing {
    private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    fun randomFractionSuffix(length: Int, digits: String = DIGITS, random: Random = Random): String {
        require(length >= 1) { "length must be >= 1" }
        val nonZero = digits.drop(1)
        return buildString {
            repeat(length - 1) { append(digits[random.nextInt(digits.length)]) }
            append(nonZero[random.nextInt(nonZero.length)])
        }
    }

    private fun midpoint(a: String, b: String?, digits: String): String {
        val zero = digits[0]
        if (b != null && a >= b) {
            throw IllegalArgumentException("$a >= $b")
        }
        if (a.lastOrNull() == zero || (b != null && b.lastOrNull() == zero)) {
            throw IllegalArgumentException("trailing zero")
        }
        if (b != null) {
            var shared = 0
            while (true) {
                val aChar = if (shared >= a.length) zero else a[shared]
                val bChar = if (shared >= b.length) zero else b[shared]
                if (aChar == bChar) {
                    shared += 1
                } else {
                    break
                }
            }
            if (shared > 0) {
                return b.substring(0 until shared) + midpoint(a.drop(shared), b.drop(shared), digits)
            }
        }
        val lo = if (a.isNotEmpty()) digits.indexOf(a[0]) else 0
        val hi = if (!b.isNullOrEmpty()) digits.indexOf(b[0]) else digits.length
        if (hi - lo > 1) {
            val pick = (0.5f * (lo + hi)).roundToInt()
            return digits[pick].toString()
        } else {
            return if (b != null && b.length > 1) {
                b.take(1)
            } else {
                digits[lo] + midpoint(a.drop(1), null, digits)
            }
        }
    }

    private fun checkHeadBody(key: String) {
        if (key.length != headBodyLength(key[0].toString())) {
            throw IllegalArgumentException("invalid integer part of order key: $key")
        }
    }

    private fun headBodyLength(head: String): Int =
        if (head >= "a" && head <= "z") {
            head[0].code - 'a'.code + 2
        } else if (head >= "A" && head <= "Z") {
            'Z'.code - head[0].code + 2
        } else {
            throw IllegalArgumentException("invalid order key head: $head")
        }

    private fun headBody(key: String): String {
        val length = headBodyLength(key[0].toString())
        if (length > key.length) {
            throw IllegalArgumentException("invalid order key: $key")
        }
        return key.take(length)
    }

    private fun checkKey(key: String, digits: String) {
        if (key == "A" + digits[0].toString().repeat(26)) {
            throw IllegalArgumentException("invalid order key: $key")
        }
        val head = headBody(key)
        val frac = key.drop(head.length)
        if (frac.lastOrNull() == digits[0]) {
            throw IllegalArgumentException("invalid order key: $key")
        }
    }

    private fun stepUp(x: String, digits: String): String? {
        checkHeadBody(x)
        val parts = x.split("").drop(1).dropLast(1)
        val head = parts.first()
        val body = parts.drop(1).toMutableList()
        var carry = true
        for (i in body.size - 1 downTo 0) {
            if (!carry) {
                break
            }
            val d = digits.indexOf(body[i]) + 1
            if (d == digits.length) {
                body[i] = digits[0].toString()
            } else {
                body[i] = digits[d].toString()
                carry = false
            }
        }
        if (carry) {
            if (head == "Z") {
                return "a" + digits[0]
            }
            if (head == "z") {
                return null
            }
            val h = (head[0].code + 1).toChar().toString()
            if (h > "a") {
                body.add(digits[0].toString())
            } else {
                body.removeLast()
            }
            return h + body.joinToString("")
        } else {
            return head + body.joinToString("")
        }
    }

    private fun stepDown(x: String, digits: String): String? {
        checkHeadBody(x)
        val parts = x.split("").drop(1).dropLast(1)
        val head = parts.first()
        val body = parts.drop(1).toMutableList()
        var borrow = true
        for (i in body.size - 1 downTo 0) {
            if (!borrow) {
                break
            }
            val d = digits.indexOf(body[i]) - 1
            if (d == -1) {
                body[i] = digits.last().toString()
            } else {
                body[i] = digits[d].toString()
                borrow = false
            }
        }
        if (borrow) {
            if (head == "a") {
                return "Z" + digits.last().toString()
            }
            if (head == "A") {
                return null
            }
            val h = (head[0].code - 1).toChar().toString()
            if (h < "Z") {
                body.add(digits.last().toString())
            } else {
                body.removeLast()
            }
            return h + body.joinToString("")
        } else {
            return head + body.joinToString("")
        }
    }

    fun generateFractionalIndexBetween(a: String?, b: String?, digits: String = DIGITS): String {
        if (a != null) {
            checkKey(a, digits)
        }
        if (b != null) {
            checkKey(b, digits)
        }
        if (a != null && b != null && a >= b) {
            throw IllegalArgumentException("$a >= $b")
        }
        if (a == null) {
            if (b == null) {
                return "a" + digits[0]
            }

            val hb = headBody(b)
            val fb = b.drop(hb.length)
            if (hb == "A" + digits[0].toString().repeat(26)) {
                return hb + midpoint("", fb, digits)
            }
            if (hb < b) {
                return hb
            }
            return stepDown(hb, digits) ?: throw IllegalStateException("cannot decrement any more")
        }

        if (b == null) {
            val ha = headBody(a)
            val fa = a.drop(ha.length)
            return stepUp(ha, digits) ?: (ha + midpoint(fa, null, digits))
        }

        val ha = headBody(a)
        val fa = a.drop(ha.length)
        val hb = headBody(b)
        val fb = b.drop(hb.length)
        if (ha == hb) {
            return ha + midpoint(fa, fb, digits)
        }
        val next = stepUp(ha, digits) ?: throw IllegalStateException("cannot increment any more")
        if (next < b) {
            return next
        }
        return ha + midpoint(fa, null, digits)
    }

    fun generateNFractionalIndicesBetween(a: String?, b: String?, n: Int, digits: String = DIGITS): List<String> {
        if (n == 0) {
            return emptyList()
        }
        if (n == 1) {
            return listOf(generateFractionalIndexBetween(a, b, digits))
        }
        if (b == null) {
            var c = generateFractionalIndexBetween(a, b, digits)
            val result = mutableListOf(c)
            repeat(n - 1) {
                c = generateFractionalIndexBetween(c, b, digits)
                result.add(c)
            }
            return result
        }
        if (a == null) {
            var c = generateFractionalIndexBetween(a, b, digits)
            val result = mutableListOf(c)
            repeat(n - 1) {
                c = generateFractionalIndexBetween(a, c, digits)
                result.add(c)
            }
            result.reverse()
            return result
        }
        val mid = floor(n / 2f).toInt()
        val c = generateFractionalIndexBetween(a, b, digits)
        val result = mutableListOf<String>()
        result.addAll(generateNFractionalIndicesBetween(a, c, mid, digits))
        result.add(c)
        result.addAll(generateNFractionalIndicesBetween(c, b, n - mid - 1, digits))
        return result
    }
}
