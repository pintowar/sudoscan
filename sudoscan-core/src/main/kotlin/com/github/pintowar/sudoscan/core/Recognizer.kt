package com.github.pintowar.sudoscan.core

interface Recognizer {
    fun predict(digits: List<Digit>): List<Int>
}