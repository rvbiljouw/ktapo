package cz.merton.ktapo

import com.fasterxml.jackson.module.kotlin.readValue
import cz.merton.ktapo.model.DeviceKey
import cz.merton.ktapo.model.DeviceSpec
import cz.merton.ktapo.model.TapoResponse
import cz.merton.webflow.util.execute
import cz.merton.webflow.util.post
import cz.merton.webflow.util.toJSONPayload
import java.util.logging.Logger

class Devices(private val ktapo: Ktapo) {
    private val logger = Logger.getLogger(javaClass.name)

    /**
     * Connects to a light bulb at the specified address.
     * Returns an instance of Light if the connection is successful, otherwise null.
     */
    fun getLight(address: String): Light? {
        val deviceSpec = handshake(address) ?: return null
        if (!authenticate(deviceSpec)) {
            logger.severe("Authentication failed for $address")
            return null
        }

        return Light(ktapo, deviceSpec)
    }

    private fun authenticate(deviceSpec: DeviceSpec): Boolean {
        val loginRequest = ktapo.mapper.writeValueAsString(
            mapOf(
                "method" to "login_device",
                "params" to ktapo.getAuthorization()
            )
        )

        val response = ktapo.passthrough(deviceSpec, loginRequest) ?: return false
        val responseBody = ktapo.mapper.readValue<TapoResponse>(response)
        if (responseBody.errorCode == 0 && responseBody.result != null) {
            deviceSpec.key.token = responseBody.result["token"]
            return true
        }
        return false
    }

    private fun handshake(address: String): DeviceSpec? {
        try {
            val requestBody = ktapo.mapper.writeValueAsString(
                mapOf(
                    "method" to "handshake",
                    "params" to mapOf(
                        "key" to ktapo.cipher.getPublicKey()
                    )
                )
            ).toJSONPayload()

            val request = ktapo.client.post("http://$address/app", requestBody).build()
            val response = ktapo.client.execute(request)
            response.use { r ->
                if (r.isSuccessful) {
                    val bodyAsString = r.body()?.string() ?: "{}"
                    val body: Map<String, *> = ktapo.mapper.readValue(bodyAsString)
                    if (body["error_code"] != 0) {
                        logger.severe("An error occurred. Message: $bodyAsString")
                        return null
                    }

                    val cookie = r.header("Set-Cookie")
                    val result = body["result"] as Map<*, *>
                    val rawKey = ktapo.cipher.readDeviceKey(result["key"] as String)
                    val tapoDeviceKey = DeviceKey(
                        key = rawKey.copyOfRange(0, 16),
                        iv = rawKey.copyOfRange(16, 32),
                        deviceIp = address,
                        sessionCookie = cookie?.substringBefore(';') ?: "none"
                    )
                    return DeviceSpec(ipAddress = address, key = tapoDeviceKey)
                }
            }
            return null
        } catch (t: Throwable) {
            t.printStackTrace()
            return null
        }
    }

}
