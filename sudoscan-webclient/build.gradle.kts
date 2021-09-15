import com.github.gradle.node.yarn.task.YarnTask

plugins {
    id("com.github.node-gradle.node")
}

project.buildDir = file("dist")

node {
    version.set("12.16.3")
    yarnVersion.set("1.22.4")
    download.set(true)
}

tasks {
    register<YarnTask>("run") {
        dependsOn("yarn")
        group = "application"
        description = "Run the client app"
        args.set(listOf("run", "start"))
    }

    register<YarnTask>("build") {
        dependsOn("yarn")
        group = "build"
        description = "Build the client bundle"
        args.set(listOf("run", "build"))
    }

    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }
}
