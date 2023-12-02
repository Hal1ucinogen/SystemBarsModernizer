package com.hal1cinogen.systembarsmodernizer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.hal1cinogen.systembarsmodernizer.bean.AppConfig
import com.hal1cinogen.systembarsmodernizer.bean.PageConfig
import com.hal1cinogen.systembarsmodernizer.tool.PreferenceProvider
import com.hal1cinogen.systembarsmodernizer.ui.theme.SystemBarsModernizerTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SystemBarsModernizerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Greeting("System Bars Modernizer")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            kotlin.runCatching {
                                val packageName = "com.dotamax.app"
                                val prefName = packageName.replace('.', '_')
                                Log.d("Naughty", "onCreate: prefName - $prefName")
                                val pref = PreferenceProvider.get(this@MainActivity)
                                Log.d("Naughty", "onCreate: pref - $pref")
                                pref?.edit {
                                    val mainPage = PageConfig(false, false, Color.WHITE)
                                    val matchPage = PageConfig(false, false, Color.WHITE)
                                    val config =
                                        AppConfig(
                                            false,
                                            1,
                                            mapOf("com.max.app.module.main.MainActivity" to mainPage,
                                                "com.max.app.module.match.match.MatchActivity" to matchPage,
                                                "com.max.app.module.me.PlayerMeActivity" to matchPage)
                                        )
                                    val json = Json.encodeToString(config)
                                    Log.d("Naughty", "onCreate: json - $json")
                                    putString("com.dotamax.app", json)

                                    val jdMain = PageConfig(false, true, Color.WHITE)
                                    val jdConfig =
                                        AppConfig(
                                            false,
                                            1,
                                            mapOf("com.jingdong.app.mall.MainFrameActivity" to jdMain)
                                        )

                                    val jdJson = Json.encodeToString(jdConfig)
                                    putString("com.jingdong.app.mall", jdJson)

                                }

                                Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT)
                                    .show()
                            }.onFailure {
                                it.printStackTrace()
                                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }) {
                            Text(text = "Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SystemBarsModernizerTheme {
        Greeting("Android")
    }
}