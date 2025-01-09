package io.github.kdroidfilter.platformtools.appmanager

import com.kdroid.androidcontextprovider.ContextProvider
import io.github.kdroidfilter.platformtools.appmanager.restartappmanager.ProcessRestarter

actual fun restartApplication() {
    val context = ContextProvider.getContext()
    ProcessRestarter.triggerRebirth(context);
}

