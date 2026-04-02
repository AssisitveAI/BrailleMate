package kr.ac.kaist.aailab.braillemate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.kaist.aailab.braillemate.android.ui.BrailleMateMainScreen
import kr.ac.kaist.aailab.braillemate.android.ui.theme.BrailleMateTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrailleMateTheme {
                BrailleMateMainScreen()
            }
        }
    }
}
