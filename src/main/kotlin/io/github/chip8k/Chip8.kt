package io.github.chip8k

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.stage.Stage
import java.io.InputStream


var running = false
lateinit var loadedRom: ByteArray
val settings = Settings()

class Chip8 : Application() {
    private val cpu = Cpu()
    private val gpu = Gpu()
    private val keyHandler = KeyHandler()

    override fun start(stage: Stage) {
        cpu.gpu = gpu
        cpu.keyHandler = keyHandler
        gpu.cpu = cpu

        stage.title = "CHIP-8K"
        stage.scene = gpu.scene
        stage.show()

        println("loading roms/ibm.ch8")
        val inputStream: InputStream? = javaClass.classLoader.getResourceAsStream("ibm.ch8")
        if (inputStream == null) { println("rom not found") }

        if (inputStream != null) {
            loadedRom = inputStream.readBytes()
            cpu.loadProgram(loadedRom)
            running = true
            val timer = object : AnimationTimer() {
                private var lastUpdateTime: Long = 0

                override fun handle(now: Long) {
                    if (now - lastUpdateTime >= settings.delayInNs) {
                        if (running) {
                            gpu.handleLogs()
                            gpu.updateDisplay()
                            cpu.runCycle()
                            cpu.updateTimers()
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