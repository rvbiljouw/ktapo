# ktapo

Ktapo is a Kotlin library and gateway server for controlling TP-Link Tapo light bulbs. It can automatically discover
bulbs on your network as well as turn them on/off, change the brightness and the colour.

The gateway server is a gRPC service that can be called by 3rd party applications such as our Flutter app, which will be
open-sourced later.

## Usage example

```kotlin
fun main(args: Array<String>) {
    // Log in with your TP-Link account
    val ktapo = Ktapo("tplinkuser", "tplinkpassword")
    // Specify an IP range to scan for devices (currently only supports last octet)
    val discovery = TapoDiscovery(ktapo, "192.168.0.1-254")
    discovery.onDeviceDiscovered { device ->
        if (device is Light) {
            // Turn the light on
            device.turnOn()

            // Set the light to red
            val color = Color(255, 0, 0)
            device.setColor(color)
        }
    }
    // Start auto-discovery (blocking)
    discovery.start()
}
```
