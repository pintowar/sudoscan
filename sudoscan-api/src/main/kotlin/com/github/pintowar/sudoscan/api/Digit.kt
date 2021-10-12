package com.github.pintowar.sudoscan.api

/**
 * The returning value of a Recognizer.
 * A Digit can have 2 kinds of types. A Valid Digit and an Unknown Digit.
 *
 * An Unknown Digit is a digit that wasn't correctly identified by the Recognizer. It assumes a default value of zero.
 * A Valid Digit is a digit that was identified by the Recognizer. It contains data about the value of the digit and the
 * confidence of the model.
 */
sealed class Digit(open val value: Int) {
    data class Valid(override val value: Int, val confidence: Double) : Digit(value)
    object Unknown : Digit(0)
}