package com.github.pintowar.sudoscan.api.spi

import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.withSystemProperties
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RecognizerSpec : StringSpec({

    "test provider" {
        Recognizer.provider().shouldBeInstanceOf<MockRecognizer>()
    }

    "test model url" {
        withSystemProperties("sudoscan.recognizer.model.url" to "myModel") {
            Recognizer.provider().modelUrl() shouldBe "myModel"
        }

        Recognizer.provider().modelUrl() shouldBe "myFileModel"
    }
})