package com.felix.livinglink.server.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FractionalIndexOrderKeyProviderTest {
    private val provider = FractionalIndexOrderKeyProvider()

    @Test
    fun `between null and null returns the initial key`() {
        assertEquals("a0", provider.between(null, null))
    }

    @Test
    fun `between produces a key strictly between two neighbours`() {
        val a = provider.between(null, null)
        val b = provider.between(a, null)

        val mid = provider.between(a, b)

        assertTrue(a < mid, "expected $a < $mid")
        assertTrue(mid < b, "expected $mid < $b")
    }

    @Test
    fun `between with null before inserts at the front`() {
        val a = provider.between(null, null)

        val before = provider.between(null, a)

        assertTrue(before < a, "expected $before < $a")
    }

    @Test
    fun `between with null after appends at the end`() {
        val a = provider.between(null, null)

        val after = provider.between(a, null)

        assertTrue(a < after, "expected $a < $after")
    }

    @Test
    fun `between throws when before is not smaller than after`() {
        val a = provider.between(null, null)
        val b = provider.between(a, null)

        assertFailsWith<IllegalArgumentException> {
            provider.between(b, a)
        }
    }

    @Test
    fun `nKeysBetween returns the requested count in ascending order`() {
        val keys = provider.nKeysBetween(before = null, after = null, count = 5)

        assertEquals(5, keys.size)
        assertEquals(keys.sorted(), keys)
        assertEquals(keys.toSet().size, keys.size)
    }

    @Test
    fun `nKeysBetween returns empty list for count zero`() {
        assertEquals(emptyList(), provider.nKeysBetween(before = null, after = null, count = 0))
    }

    @Test
    fun `nKeysBetween rejects a negative count`() {
        assertFailsWith<IllegalArgumentException> {
            provider.nKeysBetween(before = null, after = null, count = -1)
        }
    }

    @Test
    fun `nKeysBetween keeps all keys strictly between the bounds`() {
        val a = provider.between(null, null)
        val b = provider.between(a, null)

        val keys = provider.nKeysBetween(before = a, after = b, count = 3)

        assertEquals(3, keys.size)
        assertEquals(keys.sorted(), keys)
        assertTrue(keys.all { a < it && it < b }, "all keys must be between $a and $b: $keys")
    }

    @Test
    fun `jitter keeps the original key as a prefix`() {
        val key = provider.between(null, null)

        val jittered = provider.jitter(key)

        assertTrue(jittered.startsWith(key), "expected $jittered to start with $key")
    }

    @Test
    fun `jitter appends a fixed length suffix`() {
        val key = provider.between(null, null)

        val jittered = provider.jitter(key)

        assertEquals(key.length + 4, jittered.length)
    }

    @Test
    fun `jitter never ends in the zero digit`() {
        val key = provider.between(null, null)

        repeat(50) {
            val jittered = provider.jitter(key)
            assertTrue(jittered.last() != '0', "jittered key must not end in '0': $jittered")
        }
    }

    @Test
    fun `jittered key is still a valid order key usable by between`() {
        val key = provider.between(null, null)
        val jittered = provider.jitter(key)

        val after = provider.between(jittered, null)

        assertTrue(jittered < after, "expected $jittered < $after")
    }

    @Test
    fun `two jittered keys from the same base can be ordered between`() {
        val base = provider.between(null, null)

        val first = provider.jitter(base)
        val second = provider.jitter(base)

        val lower = minOf(first, second)
        val higher = maxOf(first, second)

        if (lower != higher) {
            val mid = provider.between(lower, higher)
            assertTrue(lower < mid && mid < higher, "expected $lower < $mid < $higher")
        }
    }
}
