import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()


    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
            implementation(project(":platformtools:core"))
            implementation(project(":platformtools:appmanager"))
            implementation(project(":platformtools:releasefetcher"))
            implementation(project(":platformtools:darkmodedetector"))
            implementation(project(":platformtools:rtlwindows"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

    }
}

android {
    namespace = "sample.app"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        applicationId = "sample.app.androidApp"
        versionCode = 1
        versionName = "1.0.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "sample"
            packageVersion = "1.0.1"
            macOS {
                jvmArgs(
                    "-Dapple.awt.application.appearance=system"
                )
            }
            jvmArgs(
                "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug"
            )
        }

    }
}
