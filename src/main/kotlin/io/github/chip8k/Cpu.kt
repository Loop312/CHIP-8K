package io.github.chip8k

class Cpu {
    //ram
    var memory = ByteArray(4096)

    //registers
    var v = ByteArray(16) // 16 general purpose 8-bit registers
    var i: Short = 0 // 16-bit register for memory address (the index)
    var pc: Short = 0x200.toShort() // 16-bit program counter
    var sp: Byte = 0 // 8-bit stack pointer

    //stack
    var stack = ShortArray(16) // holds 16-bit addresses

    //timers
    var delayTimer: Byte = 0 // 8-bit register (timers are bytes for some reason)
    var soundTimer: Byte = 0 // 8-bit register

    //loads a program/rom into memory
    fun loadProgram(program: ByteArray) {
        program.forEachIndexed { index, byte ->
            memory[index + 0x200] = byte // program starts at 0x200
        }
    }

    fun fetch() {

    }

    fun decode() {

    }

    fun execute() {

    }

    fun updateTimers() {

    }

    fun handleInputs() {

    }

    fun updateDisplay() {

    }


    //
    fun run() {
        while (true) {
            //fetch
            //decode
            //execute
            //update timers
            //handle inputs
            //update display
            //increment program counter
            break //get rid of later
        }
    }
}

/*
useful notes:
- Byte = 8 bits
- Short = 16 bits
 */