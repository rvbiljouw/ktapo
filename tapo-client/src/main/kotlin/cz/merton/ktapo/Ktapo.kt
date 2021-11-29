package cz.merton.ktapo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import cz.merton.ktapo.model.DeviceSpec
import cz.merton.ktapo.proto.TapoDiscovery
import cz.merton.ktapo.proto.TplinkCipher
import cz.merton.webflow.util.execute
import cz.merton.webflow.util.post
import cz.merton.webflow.util.toJSONPayload
import okhttp3.OkHttpClient
import org.apache.commons.codec.digest.DigestUtils
import java.awt.Color
import java.util.*
import java.util.concurrent.TimeUnit

data class DeviceKey(
    val key: ByteArray,
    val iv: ByteArray,
    val deviceIp: String,
    val sessionCookie: String,
    val token: String? = null
)


class Ktapo(private val username: String, private val password: String) {
    internal val mapper = ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
    internal val client = OkHttpClient.Builder()
        .callTimeout(200, TimeUnit.MILLISECONDS)
        .build()

    val cipher = TplinkCipher()
    val devices = Devices(this)

    fun passthrough(device: DeviceSpec, requestJson: String): String? {
        val encryptedRequest = cipher.encryptMessage(requestJson, device.key)
        val body = mapper.writeValueAsString(
            mapOf(
                "method" to "securePassthrough",
                "params" to mapOf(
                    "request" to encryptedRequest
                )
            )
        ).toJSONPayload()

        val request = client.post("http://${device.ipAddress}/app?token=${device.key.token}", body)
            .header("Cookie", device.key.sessionCookie)
            .build()
        val result = client.execute(request)
        if (result.isSuccessful) {
            val data: Map<String, Any> = mapper.readValue(result.body()?.string() ?: "{}")
            val encryptedResponse = (data["result"] as Map<*, *>)["response"] as String
            result.close()
            return cipher.decryptMessage(encryptedResponse, device.key)
        }
        return null
    }

    fun getAuthorization(): Map<String, String> {
        return mapOf(
            "username" to Base64.getEncoder().encodeToString(DigestUtils.sha1Hex(username.toByteArray()).toByteArray()),
            "password" to Base64.getEncoder().encodeToString(password.toByteArray())
        )
    }

}
