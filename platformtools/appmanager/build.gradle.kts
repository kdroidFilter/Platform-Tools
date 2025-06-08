import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
}
val libVersion : String by rootProject.extra

group = "io.github.kdroidfilter.platformtools.appmanager"
version = libVersion

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()


    sourceSets {
        commonMain.dependencies {
            implementation(project(":platformtools:core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmMain.dependencies {
            implementation(libs.jna.jpms)
            implementation(libs.jna.platform.jpms)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidcontextprovider)
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
    namespace = "io.github.kdroidfilter.platformtools.appmanager"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "platformtools.appmanager",
        version = version.toString()
    )

    pom {
        name.set("PlatformTools AppManager")
        description.set("Application manager module for PlatformTools, a Kotlin Library to install and update Desktop and Android Applications")
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

tasks.withType<DokkaTask>().configureEach {
    moduleName.set("Platforms Tools")
    offlineMode.set(true)
}