package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.SudokuCell
import com.github.pintowar.sudoscan.api.spi.Recognizer.Companion.provider
import java.util.*

/**
 * This interface represents the main contract (function and properties) of a Recognizer.
 * Recognizers are responsible to classify a number found on an image.
 *
 * An implementation of this Recognizer can be found using the [provider] function. This function will load an
 * implementation of this interface (found on classpath) via SPI.
 */
interface Recognizer {

    companion object {
        /**
         * This function will load an implementation of this interface (found on classpath) via SPI.
         */
        fun provider(): Recognizer {
            val loader = ServiceLoader.load(Recognizer::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Recognizer found in classpath.")
        }
    }

    /**
     * Recognizers are usually separated modules that use a Machine Learning model to recognize numbers from images.
     * These models are provided by an external and can be accessed by an url. This url is provided by the property
     * "sudoscan.recognizer.model.url" and by default, can be found on the sudoscan-recognizer.properties file.
     *
     * This function restores the url configured on this property file.
     */
    fun modelUrl(): String = Thread.currentThread().contextClassLoader.let { cl ->
        val urlProperty = "sudoscan.recognizer.model.url"
        Properties().also {
            cl.getResourceAsStream("sudoscan-recognizer.properties")?.let { res -> it.load(res) }
        }.getProperty(urlProperty, System.getProperty(urlProperty))!!
    }

    /**
     * Name of Recognizer implementation.
     */
    val name: String

    /**
     * Predict the number provided by a list images of sudoku cells.
     *
     * @param cells list of sudoku cells to have a number recognized.
     * @return list of integer equivalent to it sudoku cell. If an empty sudoku cell is provided,
     * a number 0 (zero) is returned.
     */
    fun predict(cells: List<SudokuCell>): List<Int>
}