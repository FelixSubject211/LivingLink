package com.felix.livinglink.server.core.delivery.mcp.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class McpToolArguments(
    private val values: Map<String, JsonElement>?,
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            classDiscriminator = McpToolSchemaBuilder.SEALED_DISCRIMINATOR
        },
) {
    operator fun <T> McpToolParameter<T>.invoke(): T =
        get(this)

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(parameter: McpToolParameter<T>): T =
        when (parameter) {
            is McpToolParameter.Required<*> -> required(parameter)
            is McpToolParameter.Optional<*> -> optional(parameter)
            is McpToolParameter.OptionalWithDefault<*> -> optionalWithDefault(parameter)
            is McpToolParameter.Mapped<*, *> -> mapped(parameter)
        } as T

    @Suppress("UNCHECKED_CAST")
    private fun <T> required(parameter: McpToolParameter.Required<*>): T {
        val value =
            requireNotNull(values?.get(parameter.name)) {
                "'${parameter.name}' is required."
            }

        val decoded =
            json.decodeFromJsonElement(
                deserializer = parameter.serializer as KSerializer<Any>,
                element = value,
            )

        parameter.validate(decoded)

        return decoded as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> optional(parameter: McpToolParameter.Optional<*>): T {
        val value = values?.get(parameter.name)

        val decoded =
            if (value == null) {
                parameter.default
            } else {
                json.decodeFromJsonElement(
                    deserializer = parameter.serializer as KSerializer<Any>,
                    element = value,
                )
            }

        parameter.validate(decoded)

        return decoded as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> optionalWithDefault(parameter: McpToolParameter.OptionalWithDefault<*>): T {
        val value = values?.get(parameter.name)

        val decoded =
            if (value == null) {
                parameter.default
            } else {
                json.decodeFromJsonElement(
                    deserializer = parameter.serializer as KSerializer<Any>,
                    element = value,
                )
            }

        parameter.validate(decoded)

        return decoded as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <TRaw : Any, T> mapped(parameter: McpToolParameter.Mapped<TRaw, T>): T {
        val value = values?.get(parameter.name)

        val raw: TRaw? =
            if (value == null) {
                parameter.rawDefault
            } else {
                json.decodeFromJsonElement(
                    deserializer = parameter.serializer as KSerializer<TRaw>,
                    element = value,
                )
            }

        if (parameter.required) {
            requireNotNull(raw) { "'${parameter.name}' is required." }
        }

        parameter.validate(raw)

        return if (raw == null) {
            null as T
        } else {
            parameter.map(raw)
        }
    }
}
