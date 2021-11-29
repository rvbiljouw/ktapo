package cz.merton.ktapo.proto

import cz.merton.ktapo.Device
import cz.merton.ktapo.Ktapo
import cz.merton.ktapo.Light
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.logging.Logger

typealias OnDeviceDiscovered = (device: Device) -> Unit

class TapoDiscovery(private val ktapo: Ktapo, private val range: String) {
    private val logger = Logger.getLogger(javaClass.name)
    private val devices = mutableSetOf<Device>()
    private var discoveryJob: Job? = null

    private val deviceDiscoveryListeners = mutableListOf<OnDeviceDiscovered>()


    fun start() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        discoveryJob = CoroutineScope(dispatcher).launch { discoverContinuously() }
    }

    fun stop() {
        discoveryJob?.cancel()
    }

    private suspend fun discoverContinuously() {
        val addressTokens = range.split(".")
        val rangeToken = addressTokens.last().split("-")
        val ipBase = addressTokens.take(3).joinToString(".")
        val startAddress = rangeToken.first().toInt()
        val endAddress = rangeToken.last().toInt()
        while (!Thread.interrupted()) {
            (startAddress until endAddress).map { i ->
                CoroutineScope(Dispatchers.Default).launch {
                    discover("$ipBase.$i")
                }
            }.forEach { it.join() }
            delay(5000)
        }
    }

    private fun discover(address: String) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, 80), 100)
                if (!socket.isConnected) {
                    return
                }

                val light = ktapo.devices.getLight(address) ?: return
                if (!devices.contains(light)) {
                    logger.info("Discovered ${light.nickname}")
                    devices.add(light)
                    deviceDiscoveryListeners.forEach {
                        it(light)
                    }
                }
            }
        } catch (ignored: Throwable) {
        }
    }

    fun getLights(): List<Light> {
        return devices.filterIsInstance<Light>()
    }

    fun onDeviceDiscovered(listener: OnDeviceDiscovered) {
        deviceDiscoveryListeners.add(listener)
    }

}
