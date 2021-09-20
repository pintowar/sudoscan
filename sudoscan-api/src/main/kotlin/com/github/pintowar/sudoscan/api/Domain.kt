package com.github.pintowar.sudoscan.api

import org.bytedeco.opencv.opencv_core.Mat

data class Digit(val data: Mat, val empty: Boolean)