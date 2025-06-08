import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vannitktech.maven.publish)
}
val libVersion : String by rootProject.extra

group = "io.github.kdroidfilter.platformtools.rtlwindows"
version = libVersion

kotlin {
    jvmToolchain(17)

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
            implementation("net.java.dev.jna:jna:5.17.0")
            implementation("net.java.dev.jna:jna-platform:5.17.0")

            implementation(compose.foundation)
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


mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "platformtools.rtlwindows",
        version = version.toString()
    )

    pom {
        name.set("PlatformTools Rtl Windows Fix")
        description.set("Fix in Windows OS bug that the Title bar not display correctly in rtl mode")
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