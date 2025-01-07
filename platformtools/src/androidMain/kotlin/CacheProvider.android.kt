package io.github.kdroidfilter.platformtools


import com.kdroid.androidcontextprovider.ContextProvider
import java.io.File

actual fun getCacheDir(): File {
    val context = ContextProvider.getContext()
    return context.cacheDir
}