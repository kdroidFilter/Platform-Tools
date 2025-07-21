import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
    alias(libs.plugins.kotlinx.serialization)
}

val libVersion : String by rootProject.extra

group = "io.github.kdroidfilter.platformtools.releasefetcher"
version = libVersion

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":platformtools:core"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            compileOnly(libs.ktor.client.core)
            compileOnly(libs.ktor.client.content.negotiation)
            compileOnly(libs.ktor.client.serialization)
            compileOnly(libs.ktor.client.logging)
            compileOnly(libs.ktor.client.cio)
            api(libs.semver)
            implementation(libs.kermit)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        val androidJvmMain by creating {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(androidJvmMain)
            dependencies {
                implementation(libs.slf4j.simple)
            }
        }

        androidMain {
            dependsOn(androidJvmMain)
            dependencies {
                implementation(libs.androidcontextprovider)
            }
        }

        wasmJsMain.dependencies {
            compileOnly(libs.ktor.client.js)
            api(libs.ktor.client.js)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.serialization)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.cio)
        }


    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

}

android {
    namespace = "io.github.kdroidfilter.platformtools.releasefetcher"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "platformtools.releasefetcher",
        version = version.toString()
    )

    pom {
        name.set("PlatformTools ReleaseFetcher")
        description.set("A module for Platform Tools library to manage and fetch releases from many sources (Only Github for now).")
        inceptionYear.set("2025")
        url.set("https://github.com/kdroidFilter/")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("kdroidfilter")
                name.set("Elie Gambache")
                email.set("elyahou.hadass@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/kdroidFilter/platformtools.git")
            developerConnection.set("scm:git:ssh://git@github.com:kdroidFilter/platformtools.git")
            url.set("https://github.com/kdroidFilter/platformtools")
        }
    }

    publishToMavenCentral()

    signAllPublications()
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Platforms Tools")
    offlineMode.set(true)
}
