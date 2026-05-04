package ru.health.stream.core.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

object KClassSerializer : KSerializer<KClass<*>> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("KClass", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: KClass<*>) {
        val qualifiedName = value.qualifiedName
            ?: error("Unable to serialize KClass without a qualified name: $value")
        encoder.encodeString(qualifiedName)
    }

    override fun deserialize(decoder: Decoder): KClass<*> {
        val qualifiedName = decoder.decodeString()
        return Class.forName(qualifiedName).kotlin
    }
}
