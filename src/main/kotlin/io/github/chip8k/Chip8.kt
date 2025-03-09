package io.github.chip8k

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class Chip8 : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Chip8::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()
    }
}

val cpu = Cpu()
val graphics = Graphics()
val keyHandler = KeyHandler()


fun main() {
    Application.launch(Chip8::class.java)
}