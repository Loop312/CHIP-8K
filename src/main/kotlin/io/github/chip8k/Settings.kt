package io.github.chip8k

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class Settings {
    var scale = 10.0
    var colours = Pair(Color.WHITE, Color.BLACK)
    var fps = 60
    var delayInNs = (1_000_000_000 / fps)
    var ipf = 11

    // Create a list of Stop objects for the gradient
    var stops = mutableListOf(
        Stop(0.0, colours.first),
        Stop(0.5, colours.second)
    )

    fun flipStops() {
        stops = mutableListOf(
            Stop(0.0, colours.first),
            Stop(0.5, colours.second)
        )

        gradient = RadialGradient(
            0.0, 0.0, // focusAngle, focusDistance
            0.5, 0.5, // centerX, centerY
            1.25, // radius
            true, // proportional
            null, // cycleMethod
            stops
        )
    }

    // Create a RadialGradient
    var gradient = RadialGradient(
        0.0, 0.0, // focusAngle, focusDistance
        0.5, 0.5, // centerX, centerY
        1.25, // radius
        true, // proportional
        null, // cycleMethod
        stops
    )

    var popup = createPopupPane()

    fun reset() {
        scale = 10.0
        colours = Pair(Color.WHITE, Color.BLACK)
        fps = 60
        delayInNs = (1_000_000_000 / fps)
    }

    private fun createPopupPane(): GridPane {
        val popupWidth = 200.0
        //val popupHeight = scene.height
        val popup = GridPane()
        popup.prefWidth = popupWidth
        //popup.prefHeight = popupHeight
        popup.background = Background(BackgroundFill(colours.second, null, null))

        // Add some content to the popup
        val content = VBox(10.0)
        content.alignment = Pos.CENTER
        val rectangle = Rectangle(50.0, 50.0, Color.BLUE)
        val closeButton = Button("Close")
        closeButton.onAction = EventHandler {
            hidePopup()
        }
        val greyBackground = Pane()
        greyBackground.background = Background(BackgroundFill(Color.GRAY.deriveColor(0.0, 0.0, 0.0, 1.0), null, null))

        content.children.addAll(rectangle, closeButton)

        popup.add(content, 0, 0)
        popup.add(greyBackground, 1, 0)
        //popup.alignment = Pos.CENTER_LEFT
        slideOut(popup)
        return popup
    }

    fun showPopup() {
        cpu.log(0, "settings opened, emulation paused")
        gpu.handleLogs()
        paused = true
        slideIn(popup)
    }

    fun hidePopup() {
        cpu.log(0, "settings closed, emulation resumed")
        gpu.handleLogs()
        paused = false
        slideOut(popup)
    }

    private fun slideIn(pane: Pane, targetX: Double = 0.0) {
        val transition = TranslateTransition(Duration.millis(500.0), pane)
        transition.toX = targetX
        transition.play()
    }

    private fun slideOut(pane: Pane, targetX: Double = 1000.0) {
        val transition = TranslateTransition(Duration.millis(500.0), pane)
        transition.toX = targetX
        transition.play()
    }

}