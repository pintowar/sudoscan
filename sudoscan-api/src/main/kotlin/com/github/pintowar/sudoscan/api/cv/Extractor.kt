package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.PuzzleCells

interface Extractor<T : ImageMatrix> {

    fun preProcessPhases(img: T): PreProcessPhases<T>

    fun extractPuzzleCells(img: T): PuzzleCells
}