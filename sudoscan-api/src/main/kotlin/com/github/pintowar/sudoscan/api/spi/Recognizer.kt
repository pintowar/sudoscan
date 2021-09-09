package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Digit
import java.util.*

interface Recognizer {

    companion object {
        fun provider(): Recognizer {
            val loader = ServiceLoader.load(Recognizer::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Recognizer found in classpath.")
        }
    }

    fun modelUrl(): String = Thread.currentThread().contextClassLoader.let { cl ->
        Properties().also {
            it.load(cl.getResourceAsStream("sudoscan-recognizer.properties"))
        }.getProperty("sudoscan.recognizer.model.url")!!
    }

    val name: String

    fun predict(digits: List<Digit>): List<Int>

}