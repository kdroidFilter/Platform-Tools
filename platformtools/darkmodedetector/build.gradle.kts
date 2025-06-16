import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.vannitktech.maven.publish)
}

val libVersion : String by rootProject.extra

group = "io.github.kdroidfilter.platformtools.darkmodedetector"
version = libVersion


kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":platformtools:core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
        }

        androidMain.dependencies {
        }

        jvmMain.dependencies {
            implementation(libs.jna.jpms)
            implementation(libs.jna.platform.jpms)
            implementation("de.jangassen:jfa:1.2.0") {
                exclude(group = "net.java.dev.jna", module = "jna")
                exclude(group = "net.java.dev.jna", module = "jna-platform")
                exclude(group = "net.java.dev.jna", module = "jna-jpms")
                exclude(group = "net.java.dev.jna", module = "jna-platform-jpms")
            }
            implementation(libs.kermit)
        }



    }
}

android {
    namespace = "io.github.kdroidfilter.platformtools.darkmodedetector"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "platformtools.darkmodedetector",
        version = version.toString()
    )

    pom {
        name.set("PlatformTools Dark Mode")
        description.set("Dark Mode Detection module for PlatformTools, a Kotlin Multiplatform library for managing platform-specific utilities and tools.")
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

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}
