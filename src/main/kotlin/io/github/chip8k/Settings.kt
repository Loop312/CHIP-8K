package io.github.chip8k

import javafx.scene.paint.Color

class Settings {
    var scale = 10.0
    var colours = Pair(Color.WHITE, Color.BLACK)
    var fps = 60
    var delayInNs = (1_000_000_000 / fps).toInt()

    fun reset() {
        scale = 10.0
        colours = Pair(Color.WHITE, Color.BLACK)
        fps = 60
        delayInNs = (1_000_000_000 / fps).toInt()
    }


}