package cz.merton.ktapo

import com.fasterxml.jackson.module.kotlin.readValue
import cz.merton.ktapo.model.DeviceSpec
import cz.merton.ktapo.model.TapoResponse
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

class Light(private val ktapo: Ktapo, private val device: DeviceSpec) : Device(ktapo, device) {
    val nickname: String
        get() = String(Base64.getDecoder().decode(getInfo()?.nickname ?: ""))

    fun turnOn(): Boolean {
        return changePowerState(true)
    }

    fun turnOff(): Boolean {
        return changePowerState(false)
    }

    fun setColor(color: Color): Boolean {
        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val max = arrayOf(r, g, b).maxOrNull() ?: 0f
        val min = arrayOf(r, g, b).minOrNull() ?: 0f

        var h = (max + min) / 2
        val l = (max + min) / 2
        val s: Float


        if (max == min) {
            h = 0f
            s = 0f
        } else {
            val d = max - min
            s = if (l > 0.5) d / (2 - max - min) else d / (max + min)
            when (max) {
                r -> h = (g - b) / d + (if (g < b) 6 else 0)
                g -> h = (b - r) / d + 2
                b -> h = (r - g) / d + 4
            }
            h /= 6
        }

        val finalS = (s * 100).roundToInt()
        val finalL = (l * 100).roundToInt()
        val finalH = (h * 360).roundToInt()

        val request = mapOf(
            "method" to "set_device_info",
            "params" to mapOf(
                "hue" to finalH,
                "saturation" to finalS,
                "brightness" to finalL
            )
        )
        println(request)
        val response = ktapo.passthrough(device, ktapo.mapper.writeValueAsString(request)) ?: return false
        val responseBody = ktapo.mapper.readValue<TapoResponse>(response)
        return responseBody.errorCode == 0
    }

    private fun changePowerState(on: Boolean): Boolean {
        val request = mapOf(
            "method" to "set_device_info",
            "params" to mapOf(
                "device_on" to on
            )
        )
        val response = ktapo.passthrough(device, ktapo.mapper.writeValueAsString(request)) ?: return false
        val responseBody = ktapo.mapper.readValue<TapoResponse>(response)
        return responseBody.errorCode == 0
    }

}
