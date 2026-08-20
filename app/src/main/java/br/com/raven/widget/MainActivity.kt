package br.com.raven.widget

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent { RavenApp() }
    }

    @Composable
    fun RavenApp() {
        val prefs = getSharedPreferences("raven", MODE_PRIVATE)
        var token by remember { mutableStateOf(prefs.getString("token", "") ?: "") }
        var enabled by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
        var sound by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
        var vibration by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
        var showValue by remember { mutableStateOf(prefs.getBoolean("showValue", true)) }
        var status by remember { mutableStateOf("Não testado") }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF85EA61),
                background = Color(0xFF0A0A0A),
                surface = Color(0xFF101010)
            )
        ) {
            Surface(Modifier.fillMaxSize(), color = Color(0xFF0A0A0A)) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("● RAVEN", color = Color(0xFF85EA61), style = MaterialTheme.typography.headlineMedium)
                    Text("Notificações de vendas", color = Color.White)

                    SettingSwitch("🔔 Notificações", enabled) {
                        enabled = it
                        prefs.edit().putBoolean("notifications", it).apply()
                    }
                    SettingSwitch("🔊 Som", sound) {
                        sound = it
                        prefs.edit().putBoolean("sound", it).apply()
                    }
                    SettingSwitch("📳 Vibração", vibration) {
                        vibration = it
                        prefs.edit().putBoolean("vibration", it).apply()
                    }
                    SettingSwitch("💰 Mostrar valor da venda", showValue) {
                        showValue = it
                        prefs.edit().putBoolean("showValue", it).apply()
                    }

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token Raven") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            prefs.edit().putString("token", token.trim()).apply()
                            status = "Token salvo"
                            RavenScheduler.schedule(this@MainActivity)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("SALVAR TOKEN") }

                    OutlinedButton(
                        onClick = {
                            RavenScheduler.runNow(this@MainActivity)
                            status = "Atualização solicitada"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🔄 ATUALIZAR DADOS") }

                    Text("🟢 Status: $status", color = Color(0xFF85EA61))
                    Text(
                        "Para adicionar o widget: pressione a tela inicial → Widgets → Raven.",
                        color = Color.Gray
                    )
                    Text(
                        "A tela de bloqueio segue as configurações de notificações do Android.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    @Composable
    fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
