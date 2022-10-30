object Libs {

    object Kotlin {
        private const val vKLogging = "2.0.10"
        const val bom = "org.jetbrains.kotlin:kotlin-bom"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
        const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
        const val logging = "io.github.microutils:kotlin-logging-jvm:$vKLogging"
    }

    object Kotest {
        private const val vKotest = "4.6.4"

        const val junit = "io.kotest:kotest-runner-junit5-jvm:$vKotest"
        const val assertionsCore = "io.kotest:kotest-assertions-core-jvm:$vKotest"
        const val assertionsJson = "io.kotest:kotest-assertions-json-jvm:$vKotest"
    }

    object Mockk {
        private const val vMockk = "1.12.0"
        const val mockk = "io.mockk:mockk:$vMockk"
    }

    object LogBack {
        private const val vLogback = "1.2.3"
        const val logback = "ch.qos.logback:logback-classic:$vLogback"
    }
}