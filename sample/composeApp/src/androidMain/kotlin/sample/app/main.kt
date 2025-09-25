package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        io.github.kdroidfilter.platformtools.clipboardmanager.ClipboardMonitorFactory.init(this)

        setContent {
            App()
        }
    }
}


