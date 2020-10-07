package com.github.pintowar.sudoscan.viewer

import org.beryx.awt.color.ColorFactory
import picocli.CommandLine
import picocli.CommandLine.Option
import java.awt.Color
import java.io.File
import java.util.concurrent.Callable

class SudoscanApplication : Callable<Int> {

    @Option(names = ["-c", "--color"], description = ["Solution color"], defaultValue = "BLUE")
    var color: Color = Color.BLUE

    @Option(names = ["-r", "--record"], description = ["In case the solution must be recorded on a video file"],
            defaultValue = "false")
    var record: Boolean = false

    @Option(names = ["-f", "--file"], description = ["File path to record the solution with solution " +
            "(in case -r is provided). Must be a mp4 extension. Ex.: /my_folder/solution.mp4"])
    var file: File = File("${System.getProperty("java.io.tmpdir")}${System.getProperty("file.separator")}sudoku.mp4")

    override fun call(): Int {
        val cam = SudokuCamera(color, record, file.absolutePath)
        val mainThread = Thread.currentThread()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                cam.dispose()
                mainThread.join()
            }
        })

        cam.run()
        return 0
    }
}

fun main(args: Array<String>) {
    CommandLine(SudoscanApplication())
            .registerConverter(Color::class.java) { ColorFactory.valueOf(it) }
            .execute(*args)
}