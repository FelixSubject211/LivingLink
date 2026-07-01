package com.felix.livinglink.shared.core.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FractionalIndexingTest {
    @Test
    fun `generateFractionalIndexBetween null and null returns the initial key`() {
        assertEquals("a0", FractionalIndexing.generateFractionalIndexBetween(null, null))
    }

    @Test
    fun `generateFractionalIndexBetween produces a key strictly between two neighbours`() {
        val a = FractionalIndexing.generateFractionalIndexBetween(null, null)
        val b = FractionalIndexing.generateFractionalIndexBetween(a, null)

        val mid = FractionalIndexing.generateFractionalIndexBetween(a, b)

        assertTrue(a < mid, "expected $a < $mid")
        assertTrue(mid < b, "expected $mid < $b")
    }

    @Test
    fun `generateFractionalIndexBetween with null before inserts at the front`() {
        val a = FractionalIndexing.generateFractionalIndexBetween(null, null)

        val before = FractionalIndexing.generateFractionalIndexBetween(null, a)

        assertTrue(before < a, "expected $before < $a")
    }

    @Test
    fun `generateFractionalIndexBetween with null after appends at the end`() {
        val a = FractionalIndexing.generateFractionalIndexBetween(null, null)

        val after = FractionalIndexing.generateFractionalIndexBetween(a, null)

        assertTrue(a < after, "expected $a < $after")
    }

    @Test
    fun `generateFractionalIndexBetween throws when before is not smaller than after`() {
        val a = FractionalIndexing.generateFractionalIndexBetween(null, null)
        val b = FractionalIndexing.generateFractionalIndexBetween(a, null)

        assertFailsWith<IllegalArgumentException> {
            FractionalIndexing.generateFractionalIndexBetween(b, a)
        }
    }

    @Test
    fun `generateNFractionalIndicesBetween returns the requested count in ascending order`() {
        val keys = FractionalIndexing.generateNFractionalIndicesBetween(null, null, 5)

        assertEquals(5, keys.size)
        assertEquals(keys.sorted(), keys)
        assertEquals(keys.toSet().size, keys.size)
    }

    @Test
    fun `generateNFractionalIndicesBetween returns empty list for count zero`() {
        assertEquals(emptyList(), FractionalIndexing.generateNFractionalIndicesBetween(null, null, 0))
    }

    @Test
    fun `generateNFractionalIndicesBetween keeps all keys strictly between the bounds`() {
        val a = FractionalIndexing.generateFractionalIndexBetween(null, null)
        val b = FractionalIndexing.generateFractionalIndexBetween(a, null)

        val keys = FractionalIndexing.generateNFractionalIndicesBetween(a, b, 3)

        assertEquals(3, keys.size)
        assertEquals(keys.sorted(), keys)
        assertTrue(keys.all { a < it && it < b }, "all keys must be between $a and $b: $keys")
    }

    @Test
    fun `randomFractionSuffix returns requested length`() {
        val suffix = FractionalIndexing.randomFractionSuffix(4)

        assertEquals(4, suffix.length)
    }

    @Test
    fun `randomFractionSuffix never ends in the zero digit`() {
        repeat(50) {
            val suffix = FractionalIndexing.randomFractionSuffix(4)
            assertTrue(suffix.last() != '0', "suffix must not end in '0': $suffix")
        }
    }

    @Test
    fun `randomFractionSuffix rejects length below 1`() {
        assertFailsWith<IllegalArgumentException> {
            FractionalIndexing.randomFractionSuffix(0)
        }
    }

    @Test
    fun `randomFractionSuffix with seeded random is deterministic`() {
        val a = FractionalIndexing.randomFractionSuffix(4, random = Random(42))
        val b = FractionalIndexing.randomFractionSuffix(4, random = Random(42))

        assertEquals(a, b)
    }

    @Test
    fun `key with appended suffix is still usable by generateFractionalIndexBetween`() {
        val key = FractionalIndexing.generateFractionalIndexBetween(null, null)
        val withSuffix = key + FractionalIndexing.randomFractionSuffix(4)

        val after = FractionalIndexing.generateFractionalIndexBetween(withSuffix, null)

        assertTrue(withSuffix < after, "expected $withSuffix < $after")
    }

    @Test
    fun `two suffixed keys from the same base can be ordered between`() {
        val base = FractionalIndexing.generateFractionalIndexBetween(null, null)

        val first = base + FractionalIndexing.randomFractionSuffix(4)
        val second = base + FractionalIndexing.randomFractionSuffix(4)

        val lower = minOf(first, second)
        val higher = maxOf(first, second)

        if (lower != higher) {
            val mid = FractionalIndexing.generateFractionalIndexBetween(lower, higher)
            assertTrue(lower < mid && mid < higher, "expected $lower < $mid < $higher")
        }
    }
}