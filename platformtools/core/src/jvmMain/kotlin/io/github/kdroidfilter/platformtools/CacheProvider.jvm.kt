package io.github.kdroidfilter.platformtools


import java.io.File

actual fun getCacheDir(): File = File(System.getProperty("java.io.tmpdir"))
