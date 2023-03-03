plugins {
    id("sudoscan.kotlin-publish")
    id("java-library")
}

description = "Sudoscan Recognizer Dl4j"

dependencies {
    api(projects.sudoscanApi)
    implementation(libs.recognizer.dl4j.nd4j) {
        exclude(group = "org.bytedeco", module = "mkl-platform")
    }
    implementation(libs.recognizer.dl4j.deeplearning4j)
    testImplementation(projects.sudoscanCvOpencv)
    testImplementation(testFixtures(projects.sudoscanApi))
}