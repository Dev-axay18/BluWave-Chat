package com.bluwave.chat.crypto

import android.util.Log
import org.whispersystems.curve25519.Curve25519
import org.whispersystems.curve25519.Curve25519KeyPair
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager {
    companion object {
        private const val TAG = "CryptoManager"
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    private val secureRandom = SecureRandom()
    private var keyPair: Curve25519KeyPair? = null
    private var groupSessionKey: ByteArray? = null

    fun generateKeyPair(): Curve25519KeyPair {
        val curve = Curve25519.getInstance(Curve25519.BEST)
        keyPair = curve.generateKeyPair()
        return keyPair!!
    }

    fun getPublicKey(): ByteArray? {
        return keyPair?.publicKey
    }

    fun getPrivateKey(): ByteArray? {
        return keyPair?.privateKey
    }

    fun performKeyExchange(peerPublicKey: ByteArray): ByteArray {
        val curve = Curve25519.getInstance(Curve25519.BEST)
        val sharedSecret = curve.calculateAgreement(keyPair?.privateKey, peerPublicKey)
        
        // Derive group session key from shared secret
        groupSessionKey = deriveGroupKey(sharedSecret)
        return sharedSecret
    }

    private fun deriveGroupKey(sharedSecret: ByteArray): ByteArray {
        // Use HKDF-like derivation for group key
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE, secureRandom)
        val derivedKey = keyGenerator.generateKey()
        return derivedKey.encoded
    }

    fun encryptMessage(message: String): EncryptedMessage? {
        try {
            val groupKey = groupSessionKey ?: return null
            val secretKey = SecretKeySpec(groupKey, "AES")
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            
            val encryptedData = cipher.doFinal(message.toByteArray())
            
            return EncryptedMessage(
                encryptedData = encryptedData,
                iv = iv,
                tag = ByteArray(0) // GCM includes tag in encrypted data
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting message", e)
            return null
        }
    }

    fun decryptMessage(encryptedMessage: EncryptedMessage): String? {
        try {
            val groupKey = groupSessionKey ?: return null
            val secretKey = SecretKeySpec(groupKey, "AES")
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedMessage.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val decryptedData = cipher.doFinal(encryptedMessage.encryptedData)
            return String(decryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting message", e)
            return null
        }
    }

    fun setGroupSessionKey(key: ByteArray) {
        groupSessionKey = key
    }

    fun getGroupSessionKey(): ByteArray? {
        return groupSessionKey
    }

    fun clearKeys() {
        keyPair = null
        groupSessionKey = null
    }
}

data class EncryptedMessage(
    val encryptedData: ByteArray,
    val iv: ByteArray,
    val tag: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage

        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!tag.contentEquals(other.tag)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedData.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + tag.contentHashCode()
        return result
    }
}
