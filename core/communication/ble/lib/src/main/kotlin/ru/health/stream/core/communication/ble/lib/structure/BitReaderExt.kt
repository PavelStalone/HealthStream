package ru.health.stream.core.communication.ble.lib.structure

/**
 * Creates a BitReader from a single byte
 *
 * @param byte source byte
 * @return BitReader instance
 */
fun bitReaderOf(byte: Byte): BitReader = IntBasedBitReader(byte)

/**
 * Creates a BitReader from multiple bytes
 *
 * @param bytes source bytes
 * @return BitReader instance
 */
fun bitReaderOf(vararg bytes: Byte): BitReader = IntBasedBitReader(bytes)

/**
 * Converts this byte array to a BitReader
 *
 * @return BitReader instance
 */
fun ByteArray.asBitReader(): BitReader = IntBasedBitReader(this)

/**
 * Converts this byte array to a BitReader
 *
 * @return BitReader instance
 */
fun Array<out Byte>.asBitReader(): BitReader = IntBasedBitReader(this.toByteArray())

/**
 * Converts this byte collection to a BitReader
 *
 * @return BitReader instance
 */
fun Collection<Byte>.asBitReader(): BitReader = IntBasedBitReader(this.toByteArray())
