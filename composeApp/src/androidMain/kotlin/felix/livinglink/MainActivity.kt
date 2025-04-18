package felix.livinglink

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContext.applicationContext = this

        setContent {
            App()
        }
    }
}

object AppContext {
    lateinit var applicationContext: Context
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}