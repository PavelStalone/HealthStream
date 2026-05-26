package ru.health.stream.source.local.room

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ru.health.stream.source.local.KeyValueSource
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Singleton
internal class SqlCipherKeyManager @Inject constructor(
    private val keyValueSource: KeyValueSource
) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    suspend fun getSupportFactory(): SupportOpenHelperFactory {
        initialize()

        val encryptedKey = keyValueSource.getValue<String>("encrypted_key").orEmpty()
        val iv = keyValueSource.getValue<String>("encryption_iv").orEmpty()
        val decryptedKey = getDecryptedSqlCipherKey("sqlcipher_keystore_key", encryptedKey, iv)
        return SupportOpenHelperFactory(decryptedKey)
    }

    private suspend fun initialize() {
        generateKeystoreKeyIfNeeded()
        if (keyValueSource.getValue<String>("encrypted_key") == null) {
            generateAndEncryptSqlCipherKey()
        }
    }

    private fun generateKeystoreKeyIfNeeded() {
        if (!keyStore.containsAlias("sqlcipher_keystore_key")) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                "sqlcipher_keystore_key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private suspend fun generateAndEncryptSqlCipherKey() {
        val secretKey = getSecretKey("sqlcipher_keystore_key")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val sqlCipherKey = ByteArray(32)
        SecureRandom().nextBytes(sqlCipherKey)

        val encryptedKey = cipher.doFinal(sqlCipherKey)
        val iv = cipher.iv

        keyValueSource.saveValue(
            "encrypted_key",
            Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
        )
        keyValueSource.saveValue("encryption_iv", Base64.encodeToString(iv, Base64.NO_WRAP))

        // Zero out the key in memory
        sqlCipherKey.fill(0)
    }

    private fun getDecryptedSqlCipherKey(keyAlias: String, key: String, iv: String): ByteArray {
        if (key.isEmpty() || iv.isEmpty()) return byteArrayOf()

        val encryptedKey = Base64.decode(key, Base64.NO_WRAP)
        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)

        val secretKey = getSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))

        return cipher.doFinal(encryptedKey)
    }

    private fun getSecretKey(keyAlias: String): SecretKey =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
}
