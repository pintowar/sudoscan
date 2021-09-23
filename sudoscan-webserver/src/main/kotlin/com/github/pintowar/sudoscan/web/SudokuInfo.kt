package com.github.pintowar.sudoscan.web

import io.micronaut.core.annotation.Introspected
import org.beryx.awt.color.ColorFactory
import java.awt.Color
import java.util.*

@Introspected
data class SudokuInfo(val encodedImage: String, val color: String) {
    private val template = "data:image/(.*);base64,"
    private val groups = Regex("^$template(.*)").find(encodedImage)?.groupValues
    private val type =
        groups?.get(1) ?: throw IllegalArgumentException("No type declared on the encoded image. $encodedImage")
    private val base64Img =
        groups?.get(2) ?: throw IllegalArgumentException("No type declared on the encoded image. $encodedImage")

    fun decode(): ByteArray {
        return Base64.getDecoder().decode(base64Img)
    }

    fun encode(bytes: ByteArray): String {
        val str = Base64.getEncoder().encodeToString(bytes)
        return template.replace("(.*)", type) + str
    }

    fun extension() = when (type) {
        "jpeg", "jpg" -> ".jpg"
        else -> ".$type"
    }

    fun awtColor(): Color = ColorFactory.valueOf(color)
}