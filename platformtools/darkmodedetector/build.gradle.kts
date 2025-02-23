
plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.vannitktech.maven.publish)
}

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
            implementation(libs.jna)
            implementation(libs.jna.platform)
            implementation(libs.jfa)
            implementation(libs.kotlin.logging)
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