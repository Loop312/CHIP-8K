package io.github.chip8k
import java.util.ArrayDeque
import kotlin.random.Random

class Cpu {
    //ram
    var memory = ByteArray(4096)

    //registers
    var v = ByteArray(16) // 16 general purpose 8-bit registers
    var i: Short = 0 // 16-bit register for memory address (the index)
    var pc: Short = 0x200.toShort() // 16-bit program counter (starts at 0x200 or 0000 0010 0000 0000)

    //stack
    var stack = ArrayDeque<Short>() // holds 16-bit addresses

    //timers
    var delayTimer: Byte = 0 // 8-bit register (timers are bytes for some reason)
    var soundTimer: Byte = 0 // 8-bit register

    //font
    val font = byteArrayOf(
        0xF0.toByte(), 0x90.toByte(), 0x90.toByte(), 0x90.toByte(), 0xF0.toByte(), // 0
        0x20.toByte(), 0x60.toByte(), 0x20.toByte(), 0x20.toByte(), 0x70.toByte(), // 1
        0xF0.toByte(), 0x10.toByte(), 0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), // 2
        0xF0.toByte(), 0x10.toByte(), 0xF0.toByte(), 0x10.toByte(), 0xF0.toByte(), // 3
        0x90.toByte(), 0x90.toByte(), 0xF0.toByte(), 0x10.toByte(), 0x10.toByte(), // 4
        0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), 0x10.toByte(), 0xF0.toByte(), // 5
        0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), 0x90.toByte(), 0xF0.toByte(), // 6
        0xF0.toByte(), 0x10.toByte(), 0x20.toByte(), 0x40.toByte(), 0x40.toByte(), // 7
        0xF0.toByte(), 0x90.toByte(), 0xF0.toByte(), 0x90.toByte(), 0xF0.toByte(), // 8
        0xF0.toByte(), 0x90.toByte(), 0xF0.toByte(), 0x10.toByte(), 0xF0.toByte(), // 9
        0xF0.toByte(), 0x90.toByte(), 0xF0.toByte(), 0x90.toByte(), 0x90.toByte(), // A
        0xE0.toByte(), 0x90.toByte(), 0xE0.toByte(), 0x90.toByte(), 0xE0.toByte(), // B
        0xF0.toByte(), 0x80.toByte(), 0xE0.toByte(), 0x80.toByte(), 0xF0.toByte(), // C
        0xE0.toByte(), 0x90.toByte(), 0x90.toByte(), 0x90.toByte(), 0xE0.toByte(), // D
        0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), // E
        0xF0.toByte(), 0x80.toByte(), 0xF0.toByte(), 0x80.toByte(), 0x80.toByte()  // F
    )
    var log = ""

    lateinit var gpu: Gpu
    lateinit var keyHandler: KeyHandler

    //loads font into memory
    init {
        font.copyInto(memory, 0x50)
    }

    //loads a program/rom into memory
    fun loadProgram(program: ByteArray) {
        //load program into memory
        program.copyInto(memory, 0x200)
    }

    fun fetch(): Int {
        val byte1 = memory[pc.toInt()].toInt() and 0xFF
        val byte2 = memory[pc.toInt() + 1].toInt() and 0xFF

        //if byte1 = 0000 0001 it becomes 1 0000 0000
        //checks with byte2 to make a combined "or" opcode
        val opcode = (byte1 shl 8) or byte2

        pc = (pc + 2).toShort() //counter goes up by 2 because we fetch 2 bytes
        return opcode
    }

    fun decode(opcode: Int) {
        //checkout opcode table at https://en.wikipedia.org/wiki/CHIP-8#Virtual_machine_description
        /*
        NNN: address
        NN: 8-bit constant
        N: 4-bit constant
        X and Y: 4-bit register identifier
        */
        val nib0 = (opcode and 0xF000) shr 12
        val nib1 = (opcode and 0x0F00) shr 8
        val nib2 = (opcode and 0x00F0) shr 4
        val nib3 = opcode and 0x000F
        when (nib0) { //checks first nibble

            0x0 -> when (opcode and 0x00FF) { //checks last 2 nibbles
                0x00E0 -> {cls(); log(opcode,"clear screen")} //0x00E0 clears the screen
                0x00EE -> {ret(); log(opcode,"return from subroutine")} //0x00EE returns from a subroutine
                else -> println("invalid opcode: " + opcode.toString(16)) //
            }
            //1NNN	Flow	goto NNN;	Jumps to address NNN
            0x1 -> {
                //jump to address corresponding to last 3 nibbles
                jumpTo(opcode and 0x0FFF)
                log(opcode,"jump to address")
            }
            //2NNN	Flow	*(0xNNN)()	Calls subroutine at NNN
            0x2 -> {
                //call subroutine at last 3 nibbles
                call(opcode and 0x0FFF)
                log(opcode,"call subroutine at address")
            }
            //3XNN	Cond	if (Vx == NN)	Skips the next instruction if VX equals NN (usually the next instruction is a jump to skip a code block)
            0x3 -> {
                if (v[nib1] == (opcode and 0x00FF).toByte()) {
                    skip()
                }
                log(opcode, "skip if v[nib1] = last 2 nibbles")
            }
            //4XNN	Cond	if (Vx != NN)	Skips the next instruction if VX does not equal NN (usually the next instruction is a jump to skip a code block).
            0x4 -> {
                if (v[nib1] != (opcode and 0x00FF).toByte()) {
                    skip()
                }
                log(opcode, "skip if v[nib1] != last 2 nibbles")
            }
            //5XY0	Cond	if (Vx == Vy)	Skips the next instruction if VX equals VY (usually the next instruction is a jump to skip a code block)
            0x5 -> {
                if (v[nib1] == v[nib2]) {
                    skip()
                }
                log(opcode, "skip if v[nib1] = v[nib2]")
            }
            //6XNN	Const	Vx = NN	Sets VX to NN.
            0x6 -> {
                //set v[nib1] to last 2 nibbles
                set(nib1, (opcode and 0x00FF))
                log(opcode, "set v[nib1] to last 2 nibbles")
            }
            //7XNN	Const	Vx += NN	Adds NN to VX (carry flag is not changed)
            0x7 -> {
                //add to v[nib1] last 2 nibbles
                add(nib1, (opcode and 0x00FF))
                log(opcode, "v[nib1] += last 2 nibbles")
            }
            //8XY0	Assig	Vx = Vy	Sets VX to the value of VY.
            //8XY1	BitOp	Vx |= Vy	Sets VX to VX or VY. (bitwise OR operation).
            //8XY2	BitOp	Vx &= Vy	Sets VX to VX and VY. (bitwise AND operation).
            //8XY3	BitOp	Vx ^= Vy	Sets VX to VX xor VY.
            //8XY4	Math	Vx += Vy	Adds VY to VX. VF is set to 1 when there's an overflow, and to 0 when there is not.
            //8XY5	Math	Vx -= Vy	VY is subtracted from VX. VF is set to 0 when there's an underflow, and 1 when there is not. (i.e. VF set to 1 if VX >= VY and 0 if not)
            //8XY6	BitOp	Vx >>= 1	Shifts VX to the right by 1, then stores the least significant bit of VX prior to the shift into VF.
            //8XY7	Math	Vx = Vy - Vx	Sets VX to VY minus VX. VF is set to 0 when there's an underflow, and 1 when there is not. (i.e. VF set to 1 if VY >= VX)
            //8XYE	BitOp	Vx <<= 1	Shifts VX to the left by 1, then sets VF to 1 if the most significant bit of VX prior to that shift was set, or to 0 if it was unset.
            0x8 -> {
                //bunch of stuff, add in when statement
                log(opcode, "NOT YET IMPLEMENTED")
            }
            //9XY0	Cond	if (Vx != Vy)	Skips the next instruction if VX does not equal VY. (Usually the next instruction is a jump to skip a code block)
            0x9 -> {
                if (v[nib1] != v[nib2]) {
                    skip()
                }
                log(opcode, "skip if v[nib1] != v[nib2]")
            }
            //ANNN	MEM	I = NNN	Sets I to the address NNN
            0xA -> {
                //set i to last 3 nibbles
                i = (opcode and 0x0FFF).toShort()
                log(opcode, "set i to last 3 nibbles")
            }
            //BNNN	Flow	PC = V0 + NNN	Jumps to the address NNN plus V0.
            0xB -> {
                jumpTo(v[0] + (opcode and 0x0FFF))
                log(opcode, "jump to v[0] + last 3 nibbles")
            }
            //CXNN	Rand	Vx = rand() & NN	Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
            0xC -> {
                //set v[nib1] to random "and" last 2 nibbles
                set(nib1, Random.nextInt(0, 255) and (opcode and 0x00FF))
                log(opcode, "set v[nib1] to random \"and\" last 2 nibbles")
            }
            //DXYN	Display	draw(Vx, Vy, N)
            /*Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
            Each row of 8 pixels is read as bit-coded starting from memory location I; I value does not change
            after the execution of this instruction. As described above, VF is set to 1 if any screen pixels
            are flipped from set to unset when the sprite is drawn, and to 0 if that does not happen*/
            0xD -> {
                draw(v[nib1], v[nib2], nib3)
                log(opcode, "draw sprite")
            }
            //EX9E	KeyOp	if (key() == Vx)	Skips the next instruction if the key stored in VX(only consider the lowest nibble) is pressed (usually the next instruction is a jump to skip a code block)
            //EXA1	KeyOp	if (key() != Vx)	Skips the next instruction if the key stored in VX(only consider the lowest nibble) is not pressed (usually the next instruction is a jump to skip a code block)
            0xE -> {
                //keyhandler stuff
                log(opcode, "NOT YET IMPLEMENTED")
            }
            //FX07	Timer	Vx = get_delay()	Sets VX to the value of the delay timer
            //FX0A	KeyOp	Vx = get_key()	A key press is awaited, and then stored in VX (blocking operation, all instruction halted until next key event, delay and sound timers should continue processing)
            //FX15	Timer	delay_timer(Vx)	Sets the delay timer to VX.
            //FX18	Sound	sound_timer(Vx)	Sets the sound timer to VX.
            //FX1E	MEM	I += Vx	Adds VX to I. VF is not affected.
            //FX29	MEM	I = sprite_addr[Vx]	Sets I to the location of the sprite for the character in VX(only consider the lowest nibble). Characters 0-F (in hexadecimal) are represented by a 4x5 font
            //FX33	BCD
            /*
            set_BCD(Vx)
            *(I+0) = BCD(3);
            *(I+1) = BCD(2);
            *(I+2) = BCD(1);
            */
            //Stores the binary-coded decimal representation of VX, with the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.
            //FX55	MEM	reg_dump(Vx, &I)	Stores from V0 to VX (including VX) in memory, starting at address I. The offset from I is increased by 1 for each value written, but I itself is left unmodified.
            //FX65	MEM	reg_load(Vx, &I)	Fills from V0 to VX (including VX) with values from memory, starting at address I. The offset from I is increased by 1 for each value read, but I itself is left unmodified
            0xF -> {
                //bunch of stuff
                log(opcode, "NOT YET IMPLEMENTED")
            }
            else -> {println("INVALID OPCODE GIVEN: " + opcode.toString(16))}
        }
    }

    fun updateTimers() {

    }

    //runs a single cycle of the cpu
    fun runCycle() {
        //fetch opcode and decode
        //decode also functions as an execute
        decode(fetch())
        //handle inputs
        keyHandler.handleInputs()
    }

    //clears the screen
    fun cls() {
        gpu.display = Array(64) {BooleanArray(32)}
    }

    //returns from a subroutine
    fun ret() {
        pc = stack.pop()
    }

    //jumps to addr
    fun jumpTo(addr: Int) {
        pc = addr.toShort()
    }

    //calls addr/subroutine
    fun call(addr: Int) {
        stack.push(pc)
        pc = addr.toShort()
    }

    //skips the next instruction
    fun skip() {
        pc = (pc + 2).toShort()
    }

    //sets v[nib1] to last byte of opcode
    fun set(register: Int, opcode: Int) {
        v[register] = (opcode and 0x00FF).toByte()
    }

    //
    fun add(register: Int, opcode: Int) {
        v[register] = (v[register] + (opcode and 0x00FF)).toByte()
    }

    //draws a sprite
    fun draw(registerX: Byte, registerY: Byte, height: Int) {

        val x = registerX.toInt() % 64
        val y = registerY.toInt() % 32

        gpu.draw(x, y, height)
    }

    fun reset() {
        //ram
        memory = ByteArray(4096)

        //registers
        v = ByteArray(16) // 16 general purpose 8-bit registers
        i = 0 // 16-bit register for memory address (the index)
        pc = 0x200.toShort() // 16-bit program counter (starts at 0x200 or 0000 0010 0000 0000)

        //stack
        stack = ArrayDeque<Short>() // holds 16-bit addresses

        //timers
        delayTimer = 0 // 8-bit register (timers are bytes for some reason)
        soundTimer = 0 // 8-bit register

        cls()
        gpu.updateDisplay()
    }

    fun log(opcode: Int, description: String) {
        println("opcode: 0x" + opcode.toString(16) + "      description: " + description)
        log = "opcode: 0x" + opcode.toString(16) + "      description: " + description
    }
}