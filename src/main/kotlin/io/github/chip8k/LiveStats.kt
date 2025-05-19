package io.github.chip8k

import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.layout.VBox
import javafx.scene.text.Text

class LiveStats {
    var fps = SimpleIntegerProperty(0)
    var frameTime = SimpleFloatProperty(0f)
    var ips = SimpleIntegerProperty(0)

    val fpsText = Text()
    val frameTimeText = Text()
    val ipsText = Text()
    val box = createBox()

    fun update(frameDurationNs: Long) {
        val frameTimeInSeconds = frameDurationNs / 1_000_000_000.0f
        fps.set((1 / frameTimeInSeconds).toInt())
        frameTime.set(frameTimeInSeconds * 1000) // Display frame time in milliseconds for readability
        ips.set((settings.ipf * fps.value))
        println("fps: $fps, frameTime: $frameTime, ips: $ips")
    }
    fun createBox(): VBox {
        val box = VBox()
        fpsText.textProperty().bind(fps.asString("FPS: %d"))
        frameTimeText.textProperty().bind(frameTime.asString("FT: %.2fms"))
        ipsText.textProperty().bind(ips.asString("IPS: %d"))

        fpsText.fill = settings.color1.value
        frameTimeText.fill = settings.color1.value
        ipsText.fill = settings.color1.value

        box.children.addAll(fpsText, frameTimeText, ipsText)

        return box
    }

    fun updateColors() {
        fpsText.fill = settings.color1.value
        frameTimeText.fill = settings.color1.value
        ipsText.fill = settings.color1.value
    }
}