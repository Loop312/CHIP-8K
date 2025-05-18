package io.github.chip8k

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.stage.Stage
import java.io.InputStream


var running = false
var paused = false
lateinit var loadedRom: ByteArray
val cpu = Cpu()
val settings = Settings()
val gpu = Gpu()
val keyHandler = KeyHandler()


class Chip8 : Application() {


    @OptIn(ExperimentalUnsignedTypes::class)
    override fun start(stage: Stage) {
        stage.title = "CHIP-8K"
        stage.scene = gpu.scene
        stage.show()
        keyHandler.handleInputs()

        println("loading roms/6-keypad.ch8")
        val inputStream: InputStream? = javaClass.classLoader.getResourceAsStream("6-keypad.ch8")
        if (inputStream == null) { println("rom not found") }

        if (inputStream != null) {
            loadedRom = inputStream.readBytes()
            cpu.loadProgram(loadedRom.toUByteArray())
            running = true
            val timer = object : AnimationTimer() {
                private var lastUpdateTime: Long = 0

                override fun handle(now: Long) {
                    if (now - lastUpdateTime >= settings.delayInNs) {
                        if (running && !paused) {
                            gpu.handleLogs()
                            cpu.runCycle()
                            gpu.updateDisplay()
                            lastUpdateTime = now
                        }
                    }
                }
            }
            timer.start()
        }
    }
}


fun main() {
    Application.launch(Chip8::class.java)
}