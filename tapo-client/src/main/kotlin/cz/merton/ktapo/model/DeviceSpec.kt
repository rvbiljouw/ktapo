package cz.merton.ktapo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceKey(
    val key: ByteArray,
    val iv: ByteArray,
    val deviceIp: String,
    val sessionCookie: String,
    var token: String? = null
)

data class DeviceSpec(
    val ipAddress: String,
    val key: DeviceKey,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceSpec

        if (ipAddress != other.ipAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return ipAddress.hashCode()
    }
}

data class DeviceInfo(
    @JsonProperty("device_id") val deviceId: String,
    @JsonProperty("fw_ver") val fwVer: String,
    @JsonProperty("hw_ver") val hwVer: String,
    val type: String,
    val model: String,
    val mac: String,
    @JsonProperty("hw_id") val hwId: String,
    @JsonProperty("fw_id") val fwId: String,
    @JsonProperty("oem_id") val oemId: String,
    val specs: String,
    val lang: String,
    @JsonProperty("device_on") val deviceOn: Boolean,
    @JsonProperty("on_time") val onTime: Int,
    val overheated: Boolean,
    val nickname: String,
    val avatar: String,
    val brightness: Int,
    val hue: Int,
    val saturation: Int,
    @JsonProperty("color_temp") val colorTemp: Int
)
