package com.felix.livinglink.server.core.delivery.mcp.dsl

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import kotlinx.serialization.serializer
import kotlin.time.Instant

class McpToolBuilder {
    private val mutableParameters: MutableList<McpToolParameter<*>> = mutableListOf()

    private var mutableHandler: (suspend McpToolArguments.() -> CallToolResult)? = null

    val parameters: List<McpToolParameter<*>>
        get() = mutableParameters

    val handler: suspend McpToolArguments.() -> CallToolResult
        get() =
            requireNotNull(mutableHandler) {
                "MCP tool handler is required. Use handle { ... } inside the tool block."
            }

    inline fun <reified T : Any> required(
        name: String,
        description: String,
    ): McpToolParameter<T> =
        parameter(
            McpToolParameter.Required(
                name = name,
                description = description,
                serializer = serializer<T>(),
                schema = McpToolSchemaBuilder.schemaFor<T>(),
            ),
        )

    inline fun <reified T : Any> optional(
        name: String,
        description: String,
    ): McpToolParameter<T?> =
        parameter(
            McpToolParameter.Optional(
                name = name,
                description = description,
                serializer = serializer<T>(),
                schema = McpToolSchemaBuilder.schemaFor<T>(),
                default = null,
            ),
        )

    inline fun <reified T : Any> optional(
        name: String,
        description: String,
        default: T,
    ): McpToolParameter<T> =
        parameter(
            McpToolParameter.OptionalWithDefault(
                name = name,
                description = description,
                serializer = serializer<T>(),
                schema = McpToolSchemaBuilder.schemaFor<T>(),
                default = default,
            ),
        )

    fun requiredInstant(
        name: String,
        description: String,
    ): McpToolParameter<Instant> =
        parameter(
            McpToolParameter.Mapped(
                name = name,
                description = description,
                rawSerializer = serializer<String>(),
                schema = McpToolSchemaBuilder.instantSchema(),
                required = true,
                rawDefault = null,
                map = { raw -> parseInstant(name, raw) },
            ),
        )

    fun optionalInstant(
        name: String,
        description: String,
    ): McpToolParameter<Instant?> =
        parameter(
            McpToolParameter.Mapped(
                name = name,
                description = description,
                rawSerializer = serializer<String>(),
                schema = McpToolSchemaBuilder.instantSchema(),
                required = false,
                rawDefault = null,
                map = { raw -> parseInstant(name, raw) },
            ),
        )

    fun requiredInt(
        name: String,
        description: String,
        minimum: Int? = null,
        maximum: Int? = null,
    ): McpToolParameter<Int> =
        parameter(
            McpToolParameter.Required(
                name = name,
                description = description,
                serializer = serializer<Int>(),
                schema =
                    McpToolSchemaBuilder.intSchema(
                        minimum = minimum,
                        maximum = maximum,
                    ),
                validator =
                    intRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                    ),
            ),
        )

    fun optionalInt(
        name: String,
        description: String,
        minimum: Int? = null,
        maximum: Int? = null,
    ): McpToolParameter<Int?> =
        parameter(
            McpToolParameter.Optional(
                name = name,
                description = description,
                serializer = serializer<Int>(),
                schema =
                    McpToolSchemaBuilder.intSchema(
                        minimum = minimum,
                        maximum = maximum,
                    ),
                default = null,
                validator =
                    intRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                    ),
            ),
        )

    fun optionalInt(
        name: String,
        description: String,
        minimum: Int? = null,
        maximum: Int? = null,
        default: Int,
    ): McpToolParameter<Int> =
        parameter(
            McpToolParameter.OptionalWithDefault(
                name = name,
                description = description,
                serializer = serializer<Int>(),
                schema =
                    McpToolSchemaBuilder.intSchema(
                        minimum = minimum,
                        maximum = maximum,
                    ),
                default = default,
                validator =
                    intRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                    ),
            ),
        )

    fun requiredDouble(
        name: String,
        description: String,
        minimum: Double? = null,
        maximum: Double? = null,
        roundedToDecimalPlaces: Int? = null,
    ): McpToolParameter<Double> =
        parameter(
            McpToolParameter.Required(
                name = name,
                description = description,
                serializer = serializer<Double>(),
                schema =
                    McpToolSchemaBuilder.doubleSchema(
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
                validator =
                    doubleRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
            ),
        )

    fun optionalDouble(
        name: String,
        description: String,
        minimum: Double? = null,
        maximum: Double? = null,
        roundedToDecimalPlaces: Int? = null,
    ): McpToolParameter<Double?> =
        parameter(
            McpToolParameter.Optional(
                name = name,
                description = description,
                serializer = serializer<Double>(),
                schema =
                    McpToolSchemaBuilder.doubleSchema(
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
                default = null,
                validator =
                    doubleRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
            ),
        )

    fun optionalDouble(
        name: String,
        description: String,
        minimum: Double? = null,
        maximum: Double? = null,
        roundedToDecimalPlaces: Int? = null,
        default: Double,
    ): McpToolParameter<Double> =
        parameter(
            McpToolParameter.OptionalWithDefault(
                name = name,
                description = description,
                serializer = serializer<Double>(),
                schema =
                    McpToolSchemaBuilder.doubleSchema(
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
                default = default,
                validator =
                    doubleRangeValidator(
                        name = name,
                        minimum = minimum,
                        maximum = maximum,
                        roundedToDecimalPlaces = roundedToDecimalPlaces,
                    ),
            ),
        )

    fun handle(handler: suspend McpToolArguments.() -> CallToolResult) {
        mutableHandler = handler
    }

    fun <T> parameter(parameter: McpToolParameter<T>): McpToolParameter<T> {
        mutableParameters += parameter

        return parameter
    }
}
