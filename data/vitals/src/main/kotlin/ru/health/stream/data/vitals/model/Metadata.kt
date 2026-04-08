package ru.health.stream.data.vitals.model

interface Metadata {

    operator fun <E : Element> get(key: Key<E>): E?
    operator fun plus(metadata: Metadata): Metadata = if (metadata === EmptyMetadata) {
        this
    } else {
        metadata.fold(this) { acc, element ->
            val removed = acc - element.key

            if (removed === EmptyMetadata) {
                element
            } else {
                CombinedMetadata(left = removed, element = element)
            }
        }
    }

    operator fun minus(key: Key<*>): Metadata
    fun <R> fold(initial: R, operation: (R, Element) -> R): R

    interface Key<E : Element>

    interface Element : Metadata {

        val key: Key<*>

        @Suppress("UNCHECKED_CAST")
        override operator fun <E : Element> get(key: Key<E>): E? =
            if (this.key == key) this as E else null

        override fun <R> fold(initial: R, operation: (R, Element) -> R): R =
            operation(initial, this)

        override operator fun minus(key: Key<*>): Metadata =
            if (this.key == key) EmptyMetadata else this
    }
}

data object EmptyMetadata : Metadata {

    override fun <E : Metadata.Element> get(key: Metadata.Key<E>): E? = null
    override fun plus(metadata: Metadata): Metadata = metadata
    override fun minus(key: Metadata.Key<*>): Metadata = this
    override fun <R> fold(initial: R, operation: (R, Metadata.Element) -> R): R = initial
}

internal class CombinedMetadata(
    private val left: Metadata,
    private val element: Metadata.Element,
) : Metadata {

    override fun <E : Metadata.Element> get(
        key: Metadata.Key<E>
    ): E? {
        var current = this

        while (true) {
            current.element[key]?.let { return it }

            val next = current.left
            if (next is CombinedMetadata) {
                current = next
            } else {
                return next[key]
            }
        }
    }

    override fun minus(key: Metadata.Key<*>): Metadata {
        element[key]?.let { return left }

        val newLeft = left - key
        return when {
            newLeft === left -> this
            newLeft === EmptyMetadata -> element
            else -> CombinedMetadata(newLeft, element)
        }
    }

    private fun size(): Int {
        var cur = this
        var size = 2
        while (true) {
            cur = cur.left as? CombinedMetadata ?: return size
            size++
        }
    }

    private fun contains(element: Metadata.Element): Boolean =
        get(element.key) == element

    private fun containsAll(context: CombinedMetadata): Boolean {
        var cur = context
        while (true) {
            if (!contains(cur.element)) return false
            val next = cur.left
            if (next is CombinedMetadata) {
                cur = next
            } else {
                return contains(next as Metadata.Element)
            }
        }
    }

    override fun <R> fold(initial: R, operation: (R, Metadata.Element) -> R): R =
        operation(left.fold(initial, operation), element)

    override fun equals(other: Any?): Boolean =
        this === other || other is CombinedMetadata && other.size() == size() && other.containsAll(
            this
        )

    override fun hashCode(): Int = left.hashCode() + element.hashCode()

    override fun toString(): String =
        "[" + fold("") { acc, element ->
            if (acc.isEmpty()) element.toString() else "$acc, $element"
        } + "]"
}
