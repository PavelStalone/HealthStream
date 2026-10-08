package ru.health.stream.feature.settings.impl.builder

import androidx.compose.material3.Text
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.health.stream.feature.settings.impl.domain.SettingsBuilderImpl

class SettingsBuilderImplTest {

    @Test
    fun `test categories and items sorting and merging`() {
        val builder = SettingsBuilderImpl()

        // Contributor 1
        builder.category(
            key = "integrations",
            header = { Text("Интеграции") },
            priority = 100,
        ) {
            item(key = "google_fit", priority = 10) {}
        }

        // Contributor 2 (adds item to same category "integrations")
        builder.category(
            key = "integrations",
            header = { Text("Интеграции") },
            priority = 100,
        ) {
            item(key = "health_connect", priority = 50) {}
        }

        // Contributor 3 (adds new category "account" with higher category priority)
        builder.category(
            key = "account",
            header = { Text("Аккаунт") },
            priority = 200,
        ) {
            item(key = "profile", priority = 100) {}
        }

        val result = builder.build()

        // 1. Check category order (account priority 200 > integrations priority 100)
        assertEquals(2, result.size)
        assertEquals("account", result[0].key)
        assertEquals("integrations", result[1].key)

        // 2. Check item merging & sorting in "integrations" (health_connect priority 50 > google_fit priority 10)
        val integrationsCategory = result[1]
        assertEquals(2, integrationsCategory.items.size)
        assertEquals("health_connect", integrationsCategory.items[0].key)
        assertEquals("google_fit", integrationsCategory.items[1].key)
    }
}
