package com.felix.livinglink.server.core.delivery.mcp.dsl

import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.serializer
import kotlin.math.pow

object McpToolSchemaBuilder {
    const val SEALED_DISCRIMINATOR = "type"

    fun build(parameters: List<McpToolParameter<*>>): ToolSchema =
        ToolSchema(
            properties =
                buildJsonObject {
                    parameters.forEach { parameter ->
                        put(parameter.name, parameter.schemaWithDescription())
                    }
                },
            required =
                parameters
                    .filter { parameter ->
                        parameter.required
                    }.map { parameter ->
                        parameter.name
                    },
        )

    inline fun <reified T> schemaFor(): JsonElement =
        schemaFor(serializer<T>().descriptor)

    fun instantSchema(): JsonElement =
        buildJsonObject {
            put("type", "string")
            put(
                "description",
                "ISO 8601 date or instant. Accepted: '2026-05-24', " +
                    "'2026-05-24T10:00:00+02:00', '2026-05-24T10:00:00Z'.",
            )
        }

    fun intSchema(
        minimum: Int? = null,
        maximum: Int? = null,
    ): JsonElement =
        buildJsonObject {
            put("type", "integer")

            minimum?.let { value ->
                put("minimum", value)
            }

            maximum?.let { value ->
                put("maximum", value)
            }
        }

    fun doubleSchema(
        minimum: Double? = null,
        maximum: Double? = null,
        roundedToDecimalPlaces: Int? = null,
    ): JsonElement =
        buildJsonObject {
            put("type", "number")

            minimum?.let { value ->
                put("minimum", value)
            }

            maximum?.let { value ->
                put("maximum", value)
            }

            roundedToDecimalPlaces?.let { decimalPlaces ->
                require(decimalPlaces >= 0) {
                    "roundedToDecimalPlaces must be greater than or equal to 0."
                }

                put(
                    "multipleOf",
                    10.0.pow(-decimalPlaces),
                )
            }
        }

    private fun McpToolParameter<*>.schemaWithDescription(): JsonObject =
        buildJsonObject {
            schema.jsonObject.forEach { entry ->
                put(entry.key, entry.value)
            }

            put("description", description)
        }

    @OptIn(ExperimentalSerializationApi::class)
    fun schemaFor(descriptor: SerialDescriptor): JsonElement {
        if (descriptor.isNullable) {
            return schemaFor(descriptor.getElementDescriptor(0))
        }

        return when (descriptor.kind) {
            PrimitiveKind.STRING ->
                buildJsonObject {
                    put("type", "string")
                }

            PrimitiveKind.INT ->
                buildJsonObject {
                    put("type", "integer")
                }

            PrimitiveKind.LONG ->
                buildJsonObject {
                    put("type", "integer")
                }

            PrimitiveKind.FLOAT ->
                buildJsonObject {
                    put("type", "number")
                }

            PrimitiveKind.DOUBLE ->
                buildJsonObject {
                    put("type", "number")
                }

            PrimitiveKind.BOOLEAN ->
                buildJsonObject {
                    put("type", "boolean")
                }

            StructureKind.LIST ->
                buildJsonObject {
                    put("type", "array")
                    put("items", schemaFor(descriptor.getElementDescriptor(0)))
                }

            StructureKind.MAP ->
                buildJsonObject {
                    put("type", "object")
                    put("additionalProperties", schemaFor(descriptor.getElementDescriptor(1)))
                }

            StructureKind.CLASS ->
                buildJsonObject {
                    put("type", "object")

                    putJsonObject("properties") {
                        repeat(descriptor.elementsCount) { index ->
                            val elementName = descriptor.getElementName(index)
                            val elementDescriptor = descriptor.getElementDescriptor(index)

                            put(elementName, schemaFor(elementDescriptor))
                        }
                    }

                    putJsonArray("required") {
                        repeat(descriptor.elementsCount) { index ->
                            if (!descriptor.isElementOptional(index)) {
                                add(descriptor.getElementName(index))
                            }
                        }
                    }
                }

            StructureKind.OBJECT ->
                buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {}
                    putJsonArray("required") {}
                }

            SerialKind.ENUM ->
                buildJsonObject {
                    put("type", "string")

                    putJsonArray("enum") {
                        repeat(descriptor.elementsCount) { index ->
                            add(descriptor.getElementName(index))
                        }
                    }
                }

            PolymorphicKind.SEALED -> sealedSchemaFor(descriptor)

            else ->
                error("Unsupported MCP parameter type: ${descriptor.serialName}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun sealedSchemaFor(descriptor: SerialDescriptor): JsonElement {
        val valueIndex =
            (0 until descriptor.elementsCount)
                .firstOrNull { index ->
                    descriptor.getElementName(index) == "value"
                } ?: 1
        val subclassesContainer = descriptor.getElementDescriptor(valueIndex)

        return buildJsonObject {
            putJsonArray("oneOf") {
                repeat(subclassesContainer.elementsCount) { index ->
                    val subclassDescriptor = subclassesContainer.getElementDescriptor(index)
                    val serialName = subclassDescriptor.serialName

                    val branchSchema = schemaFor(subclassDescriptor).jsonObject

                    add(
                        buildJsonObject {
                            branchSchema.forEach { entry ->
                                if (entry.key != "properties" && entry.key != "required") {
                                    put(entry.key, entry.value)
                                }
                            }

                            putJsonObject("properties") {
                                put(
                                    SEALED_DISCRIMINATOR,
                                    buildJsonObject {
                                        put("type", "string")
                                        put("const", serialName)
                                    },
                                )

                                branchSchema["properties"]?.jsonObject?.forEach { entry ->
                                    put(entry.key, entry.value)
                                }
                            }

                            putJsonArray("required") {
                                add(SEALED_DISCRIMINATOR)

                                val branchRequired = branchSchema["required"]
                                if (branchRequired is JsonArray) {
                                    branchRequired.forEach { element ->
                                        add(element)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
