package cz.merton.ktapo.proto

import cz.merton.ktapo.model.DeviceKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMWriter
import java.io.StringWriter
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class TplinkCipher {
    private val keyPair: KeyPair

    init {
        Security.addProvider(BouncyCastleProvider())
        val generator = KeyPairGenerator.getInstance("RSA", "BC")
        generator.initialize(1024)
        keyPair = generator.generateKeyPair()
    }

    fun getPublicKey(): String {
        val stringWriter = StringWriter()
        val pemWriter = PEMWriter(stringWriter)
        pemWriter.writeObject(keyPair.public)
        pemWriter.close()
        return stringWriter.toString()
    }

    fun readDeviceKey(pem: String): ByteArray {
        val cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC")
        val keyBytes = Base64.getDecoder().decode(pem)
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        return cipher.doFinal(keyBytes)
    }

    fun encryptMessage(json: String, key: DeviceKey): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key.key, "AES")
        val ivSpec = IvParameterSpec(key.iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return Base64.getEncoder().encodeToString(cipher.doFinal(json.toByteArray()))
    }

    fun decryptMessage(data: String, key: DeviceKey): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key.key, "AES")
        val ivSpec = IvParameterSpec(key.iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return String(cipher.doFinal(Base64.getDecoder().decode(data.toByteArray())))
    }

}
