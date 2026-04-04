package ru.health.stream.feature.personal.data.model

@JvmInline
value class Email(val value: String) : Comparable<Email> {

    init {
        require(EMAIL_REGEX.matches(value)) { "Invalid email address: $value" }
    }

    val domain: String get() = value.substringAfter('@')
    val localPart: String get() = value.substringBefore('@')

    override fun compareTo(other: Email): Int = value.compareTo(other.value)

    companion object {

        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
}
