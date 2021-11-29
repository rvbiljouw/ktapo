package cz.merton.ktapo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TapoResponse(
    @JsonProperty("error_code") val errorCode: Int,
    val result: Map<String, String>?
)


data class TapoDeviceInfoResponse(
    @JsonProperty("error_code") val errorCode: Int,
    @JsonProperty("result") val deviceInfo: DeviceInfo?
)

