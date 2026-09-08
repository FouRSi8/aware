package com.aware.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStore(private val context: Context) {
    private val preferences = context.getSharedPreferences("secure_wrapped_values", Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun key(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val payload = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(payload)
    }

    fun decrypt(value: String): String = runCatching {
        val payload = Base64.getDecoder().decode(value)
        val iv = payload.copyOfRange(0, IV_SIZE)
        val encrypted = payload.copyOfRange(IV_SIZE, payload.size)
        Cipher.getInstance(TRANSFORMATION).run {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(doFinal(encrypted), Charsets.UTF_8)
        }
    }.getOrDefault("")

    fun databasePassphrase(): ByteArray {
        val wrapped = preferences.getString(DB_KEY, null)
        if (wrapped != null) return Base64.getDecoder().decode(decrypt(wrapped))
        val passphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        val encoded = Base64.getEncoder().encodeToString(passphrase)
        preferences.edit().putString(DB_KEY, encrypt(encoded)).apply()
        return passphrase
    }

    fun saveSecret(name: String, value: String) {
        preferences.edit().putString("secret_$name", encrypt(value)).apply()
    }

    fun readSecret(name: String): String? = preferences.getString("secret_$name", null)?.let(::decrypt)

    fun putBoolean(name: String, value: Boolean) { preferences.edit().putBoolean("flag_$name", value).apply() }
    fun getBoolean(name: String, default: Boolean = false): Boolean = preferences.getBoolean("flag_$name", default)

    companion object {
        private const val KEY_ALIAS = "aware_master_key_v1"
        private const val DB_KEY = "wrapped_database_passphrase"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12
    }
}
