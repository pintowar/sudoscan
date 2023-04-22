import net.researchgate.release.ReleaseExtension

plugins {
    base
    id("jacoco-report-aggregation")
    id("net.researchgate.release")
    id("org.sonarqube")
}

allprojects {
    group = "io.github.pintowar"
//    javacppPlatform = "linux-x86_64,macosx-x86_64,windows-x86_64"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    rootProject.subprojects.forEach(::jacocoAggregation)
}

reporting {
    reports {
        val codeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

tasks {
    register("assembleCliApp") {
        dependsOn(":sudoscan-cli:shadowJar")
        group = "build"
        description = "Build cli app"
        doLast {
            copy {
                from(files("${project(":sudoscan-cli").buildDir}/libs/")) {
                    include("*-all.jar")
                }
                into("$rootDir/build/")
            }

            logger.quiet("JAR generated at $rootDir/build/.")
        }
    }
}

sonarqube {
    properties {
        val jacocoReportPath = "${project.buildDir.absolutePath}/reports/jacoco/codeCoverageReport"
        val sonarToken = project.findProperty("sonar.token")?.toString() ?: System.getenv("SONAR_TOKEN")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.organization", "pintowar")
        property("sonar.projectName", "sudoscan")
        property("sonar.projectKey", "pintowar_sudoscan")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", sonarToken)
        property("sonar.verbose", true)
        property("sonar.github.repository", "pintowar/sudoscan")
        property("sonar.exclusions", "sudoscan-cli/**")
        property("sonar.coverage.jacoco.xmlReportPaths", "$jacocoReportPath/codeCoverageReport.xml")
    }
}

configure<ReleaseExtension> {
    tagTemplate.set("v\$version")
    with(git) {
        requireBranch.set("master")
    }
}

tasks.afterReleaseBuild {
    dependsOn(
        ":sudoscan-api:publish", ":sudoscan-solver-choco:publish", ":sudoscan-solver-ojalgo:publish",
        ":sudoscan-recognizer-dl4j:publish", ":sudoscan-recognizer-djl:publish", ":sudoscan-cli:publish"
    )
}