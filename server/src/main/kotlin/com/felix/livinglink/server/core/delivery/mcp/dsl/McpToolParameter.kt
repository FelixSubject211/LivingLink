package com.felix.livinglink.server.core.delivery.mcp.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

sealed class McpToolParameter<T>(
    val name: String,
    val description: String,
    val required: Boolean,
    val serializer: KSerializer<*>,
    val schema: JsonElement,
) {
    abstract fun validate(value: Any?)

    class Required<T : Any>(
        name: String,
        description: String,
        serializer: KSerializer<T>,
        schema: JsonElement,
        private val validator: (T) -> Unit = {},
    ) : McpToolParameter<T>(
            name = name,
            description = description,
            required = true,
            serializer = serializer,
            schema = schema,
        ) {
        @Suppress("UNCHECKED_CAST")
        override fun validate(value: Any?) {
            validator(value as T)
        }
    }

    class Optional<T : Any>(
        name: String,
        description: String,
        serializer: KSerializer<T>,
        schema: JsonElement,
        val default: T?,
        private val validator: (T) -> Unit = {},
    ) : McpToolParameter<T?>(
            name = name,
            description = description,
            required = false,
            serializer = serializer.nullable,
            schema = schema,
        ) {
        @Suppress("UNCHECKED_CAST")
        override fun validate(value: Any?) {
            value?.let { presentValue ->
                validator(presentValue as T)
            }
        }
    }

    class OptionalWithDefault<T : Any>(
        name: String,
        description: String,
        serializer: KSerializer<T>,
        schema: JsonElement,
        val default: T,
        private val validator: (T) -> Unit = {},
    ) : McpToolParameter<T>(
            name = name,
            description = description,
            required = false,
            serializer = serializer,
            schema = schema,
        ) {
        @Suppress("UNCHECKED_CAST")
        override fun validate(value: Any?) {
            validator(value as T)
        }
    }

    class Mapped<TRaw : Any, T>(
        name: String,
        description: String,
        rawSerializer: KSerializer<TRaw>,
        schema: JsonElement,
        required: Boolean,
        val rawDefault: TRaw?,
        val map: (TRaw) -> T,
        private val validator: (TRaw) -> Unit = {},
    ) : McpToolParameter<T>(
            name = name,
            description = description,
            required = required,
            serializer = if (required) rawSerializer else rawSerializer.nullable,
            schema = schema,
        ) {
        @Suppress("UNCHECKED_CAST")
        override fun validate(value: Any?) {
            value?.let { validator(it as TRaw) }
        }
    }
}
