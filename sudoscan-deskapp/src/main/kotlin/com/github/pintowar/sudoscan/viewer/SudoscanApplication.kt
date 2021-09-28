package com.github.pintowar.sudoscan.viewer

import io.micronaut.configuration.picocli.PicocliRunner
import org.beryx.awt.color.ColorFactory
import picocli.CommandLine.*
import java.awt.Color
import java.io.File

@Command(name = "sudoscan-cli", version = ["Versioned Command 1.0"], mixinStandardHelpOptions = true)
class SudoscanApplication : Runnable {

    internal class ColorConverter : ITypeConverter<Color> {
        override fun convert(value: String): Color = ColorFactory.valueOf(value)
    }

    @Option(
        names = ["-c", "--color"], description = ["Solution color"],
        defaultValue = "BLUE", converter = [ColorConverter::class]
    )
    var color: Color = Color.BLUE

    @Option(
        names = ["-r", "--record"], description = ["In case the solution must be recorded on a video file"],
        defaultValue = "false"
    )
    var record: Boolean = false

    @Option(
        names = ["-f", "--file"],
        description = [
            "File path to record the solution with solution " +
                "(in case -r is provided). Must be a mp4 extension. Ex.: /my_folder/solution.mp4"
        ]
    )
    var file: File = File("${System.getProperty("java.io.tmpdir")}${System.getProperty("file.separator")}sudoku.mp4")

    override fun run() {
        val camera = SudokuCamera(color, record, file.absolutePath)
        camera.startCapture()
    }
}

fun main(args: Array<String>) {
    PicocliRunner.run(SudoscanApplication::class.java, *args)
}