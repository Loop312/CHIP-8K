package io.github.chip8k

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.stage.Stage
import java.io.InputStream

class Chip8 : Application() {
    private val cpu = Cpu()
    private val gpu = Gpu()
    private val keyHandler = KeyHandler()
    private var running = false

    override fun start(stage: Stage) {
        cpu.gpu = gpu
        cpu.keyHandler = keyHandler
        gpu.cpu = cpu

        stage.title = "CHIP-8K"
        stage.scene = gpu.scene
        stage.show()

        val inputStream: InputStream? = javaClass.classLoader.getResourceAsStream("roms/IBM Logo.ch8")
        if (inputStream != null) {
            val rom = inputStream.readBytes()
            cpu.loadProgram(rom)
            running = true
            val timer = object : AnimationTimer() {
                private var lastUpdateTime: Long = 0

                override fun handle(now: Long) {
                    if (now - lastUpdateTime >= 16_666_666) {
                        if (running) {
                            cpu.runCycle()
                            cpu.updateTimers()
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