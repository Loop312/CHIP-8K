module io.github.chip8k.chip8k {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens io.github.chip8k to javafx.fxml;
    exports io.github.chip8k;
}