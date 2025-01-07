import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
}

group = "io.github.kdroidfilter.platformtools"
version = "0.1.0"

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
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
                implementation(libs.androidcontextprovider)
            }
        }

        androidMain {
            dependsOn(androidJvmMain)
            dependencies {
                implementation(libs.androidcontextprovider)
            }
        }

        val ios = listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        )

        val macos = listOf(
            macosX64(),
            macosArm64()
        )

        val iosMain by creating {
            dependsOn(commonMain.get())
        }

        val macosMain by creating {
            dependsOn(commonMain.get())
        }

        ios.forEach {
            it.compilations["main"].defaultSourceSet {
                dependsOn(iosMain)
            }
        }

        macos.forEach {
            it.compilations["main"].defaultSourceSet {
                dependsOn(macosMain)
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
    namespace = "io.github.kdroidfilter.platformtools"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "platformtools",
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
