import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.exclude

object Libs {

    object Kotlin {
        private const val vKLogging = "2.0.10"
        const val bom = "org.jetbrains.kotlin:kotlin-bom"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
        const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
        const val logging = "io.github.microutils:kotlin-logging-jvm:$vKLogging"
    }

    object Kotest {
        private const val vKotest = "4.6.1"

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

    object ChocoSolver {
        private const val vChocoSolver = "4.10.6"
        const val chocoSolver = "org.choco-solver:choco-solver:$vChocoSolver"

        fun DependencyHandler.implementChocoSolver() {
            addDependencyTo(this, "implementation", chocoSolver, Action<ExternalModuleDependency> {
                exclude(group = "org.knowm.xchart")
                exclude(group = "org.jgrapht")
                exclude(group = "com.github.cp-profiler")
                exclude(group = "dk.brics.automaton")
            })
        }
    }

    object Guava {
        private const val vGuava = "30.1.1-jre"
        const val guava = "com.google.guava:guava:$vGuava"

        fun DependencyHandler.apiGuava() {
            add("api", guava)
        }
    }

    object Djl {
        private const val vDjl = "0.12.0"
        private const val vTensorFlow = "2.4.1"

        const val djlTensorflowZoo = "ai.djl.tensorflow:tensorflow-model-zoo:$vDjl"
        const val djlTensorflowEngine = "ai.djl.tensorflow:tensorflow-engine:$vDjl"
        const val djlTensorflowNative = "ai.djl.tensorflow:tensorflow-native-auto:$vTensorFlow"

        fun DependencyHandler.implementDjl() {
            listOf(djlTensorflowZoo).forEach {
                add("implementation", it)
            }
            listOf(djlTensorflowNative).forEach {
                add("runtimeOnly", it)
            }
        }
    }

    object Nd4j {
        private const val vNd4j = "1.0.0-M1.1"
        private const val vHdf5 = "1.12.1-1.5.6"
        const val nd4j = "org.nd4j:nd4j-native-platform:$vNd4j"
        const val deeplearning4j = "org.deeplearning4j:deeplearning4j-modelimport:$vNd4j"
        const val hdf5 = "org.bytedeco:hdf5-platform:$vHdf5"

        fun DependencyHandler.implementNd4j() {
            addDependencyTo(this, "implementation", nd4j, Action<ExternalModuleDependency> {
                exclude(group = "org.bytedeco", module = "mkl-platform")
            })
            addDependencyTo(this, "implementation", deeplearning4j, Action<ExternalModuleDependency> {
                exclude(group = "org.bytedeco", module = "hdf5-platform")
            })
            add("implementation", hdf5)
        }

    }

    object JavaCv {
        private const val vJavaCv = "1.5.6"
        private const val vOpenCv = "4.5.3-$vJavaCv"
        private const val vFfmpeg = "4.4-$vJavaCv"
        const val javaCv = "org.bytedeco:javacv:$vJavaCv"
        const val openCvPlatform = "org.bytedeco:opencv-platform:$vOpenCv"
        const val ffmpegPlatform = "org.bytedeco:ffmpeg-platform:$vFfmpeg"

        fun DependencyHandler.apiJavaCv() {
            addDependencyTo(this, "api", javaCv, Action<ExternalModuleDependency> {
                listOf(
                    "artoolkitplus", "flandmark", "flycapture", "leptonica", "libdc1394",
                    "libfreenect", "libfreenect2", "librealsense", "librealsense2",
                    "tesseract", "videoinput", "ffmpeg", "opencv", "openblas"
                ).forEach {
                    exclude(group = "org.bytedeco", module = it)
                }
            })
        }

        fun DependencyHandler.apiOpenCv() {
            add("api", openCvPlatform)
        }

        fun DependencyHandler.apiFfmpeg() {
            add("api", ffmpegPlatform)
        }
    }

    object Picocli {
        private const val vPicocli = "4.5.1"
        const val picocliCodegen = "info.picocli:picocli-codegen:$vPicocli"
        const val picocli = "info.picocli:picocli:$vPicocli"

        fun DependencyHandler.implementPicocli() {
            add("kapt", picocliCodegen)
            add("implementation", picocli)
        }
    }

    object AwtColorFactory {
        private const val vAwtColorFactory = "1.0.2"
        const val awtColorFactory = "org.beryx:awt-color-factory:$vAwtColorFactory"

        fun DependencyHandler.implementAwtColorFactory() {
            add("implementation", awtColorFactory)
        }
    }

}