import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.serialization)

}

val libVersion : String by rootProject.extra

group = "io.github.kdroidfilter.platformtools.releasefetcher"
version = libVersion

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()


    sourceSets {
        commonMain.dependencies {
            implementation(project(":platformtools:core"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.cio)
            api(libs.semver)
            implementation(libs.slf4j.simple)
            implementation(libs.kotlin.logging)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }


        jvmMain {
            dependencies {
                implementation(libs.androidcontextprovider)

            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidcontextprovider)
            }
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
        name.set("PlatformTools")
        description.set("A Kotlin Multiplatform library to manage platform-specific utilities and tools.")
        inceptionYear.set("2025") // Change si la création du projet est plus ancienne.
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
                name.set("Elyahou Hadass")
                email.set("elyahou.hadass@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/kdroidFilter/platformtools.git")
            developerConnection.set("scm:git:ssh://git@github.com:kdroidFilter/platformtools.git")
            url.set("https://github.com/kdroidFilter/platformtools")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Platforms Tools")
    offlineMode.set(true)
}