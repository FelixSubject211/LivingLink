package com.felix.livinglink.server.core.infrastructure.mongo

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import kotlin.time.Instant

class KotlinInstantCodec : Codec<Instant> {
    override fun encode(
        writer: BsonWriter,
        value: Instant,
        encoderContext: EncoderContext,
    ) {
        writer.writeDateTime(value.toEpochMilliseconds())
    }

    override fun decode(
        reader: BsonReader,
        decoderContext: DecoderContext,
    ): Instant =
        when (reader.currentBsonType) {
            BsonType.DATE_TIME -> Instant.fromEpochMilliseconds(reader.readDateTime())
            BsonType.INT64 -> Instant.fromEpochMilliseconds(reader.readInt64())
            BsonType.STRING -> Instant.parse(reader.readString())
            else -> error("Unsupported BSON type for kotlin.time.Instant: ${reader.currentBsonType}")
        }

    override fun getEncoderClass(): Class<Instant> =
        Instant::class.java
}
