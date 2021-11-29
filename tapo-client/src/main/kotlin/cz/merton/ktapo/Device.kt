package cz.merton.ktapo

import com.fasterxml.jackson.module.kotlin.readValue
import cz.merton.ktapo.model.DeviceInfo
import cz.merton.ktapo.model.DeviceSpec
import cz.merton.ktapo.model.TapoDeviceInfoResponse

open class Device(private val ktapo: Ktapo, private val device: DeviceSpec) {

    fun getInfo(): DeviceInfo? {
        val request = mapOf(
            "method" to "get_device_info"
        )
        val response = ktapo.passthrough(device, ktapo.mapper.writeValueAsString(request)) ?: return null
        val responseBody = ktapo.mapper.readValue<TapoDeviceInfoResponse>(response)
        return if (responseBody.errorCode == 0) responseBody.deviceInfo else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Device

        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }

}
