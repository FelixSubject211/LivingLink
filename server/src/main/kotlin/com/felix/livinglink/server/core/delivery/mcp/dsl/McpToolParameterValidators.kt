package com.felix.livinglink.server.core.delivery.mcp.dsl

import kotlin.math.pow
import kotlin.math.roundToLong

fun intRangeValidator(
    name: String,
    minimum: Int?,
    maximum: Int?,
): (Int) -> Unit =
    { value ->
        minimum?.let { minimumValue ->
            require(value >= minimumValue) {
                "'$name' must be greater than or equal to $minimumValue."
            }
        }

        maximum?.let { maximumValue ->
            require(value <= maximumValue) {
                "'$name' must be less than or equal to $maximumValue."
            }
        }
    }

fun doubleRangeValidator(
    name: String,
    minimum: Double?,
    maximum: Double?,
    roundedToDecimalPlaces: Int?,
): (Double) -> Unit =
    { value ->
        minimum?.let { minimumValue ->
            require(value >= minimumValue) {
                "'$name' must be greater than or equal to $minimumValue."
            }
        }

        maximum?.let { maximumValue ->
            require(value <= maximumValue) {
                "'$name' must be less than or equal to $maximumValue."
            }
        }

        roundedToDecimalPlaces?.let { decimalPlaces ->
            require(decimalPlaces >= 0) {
                "roundedToDecimalPlaces must be greater than or equal to 0."
            }

            val scale = 10.0.pow(decimalPlaces)
            val scaledValue = value * scale

            require(scaledValue.roundToLong().toDouble() == scaledValue) {
                "'$name' must be rounded to $decimalPlaces decimal places."
            }
        }
    }
