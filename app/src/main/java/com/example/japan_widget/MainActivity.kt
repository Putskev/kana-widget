package com.example.japan_widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.japan_widget.data.KanaSettings
import com.example.japan_widget.data.Mode
import com.example.japan_widget.data.Order
import com.example.japan_widget.data.SettingsRepository
import com.example.japan_widget.ui.theme.Japan_WidgetTheme
import com.example.japan_widget.work.WidgetScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)

        // Make sure a periodic update job exists even if no widget has been placed yet.
        lifecycleScope.launch {
            WidgetScheduler.schedule(applicationContext, settingsRepository.current().intervalHours)
        }

        setContent {
            Japan_WidgetTheme {
                Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
                    val settings by settingsRepository.settingsFlow.collectAsState(initial = KanaSettings())
                    val scope = rememberCoroutineScope()

                    SettingsScreen(
                        settings = settings,
                        modifier = Modifier.padding(innerPadding),
                        onModeChange = { mode ->
                            scope.launch { settingsRepository.setMode(mode) }
                        },
                        onOrderChange = { order ->
                            scope.launch { settingsRepository.setOrder(order) }
                        },
                        onIntervalChange = { hours ->
                            scope.launch {
                                settingsRepository.setIntervalHours(hours)
                                WidgetScheduler.schedule(applicationContext, hours)
                            }
                        },
                        onDakutenChange = { enabled ->
                            scope.launch { settingsRepository.setIncludeDakuten(enabled) }
                        },
                        onRareChange = { enabled ->
                            scope.launch { settingsRepository.setIncludeRare(enabled) }
                        },
                        onOpenBatterySettings = { openBatteryOptimizationSettings(this@MainActivity) }
                    )
                }
            }
        }
    }
}

private fun openBatteryOptimizationSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)
    if (powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true) return
    val intent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}

@Composable
fun SettingsScreen(
    settings: KanaSettings,
    modifier: Modifier = Modifier,
    onModeChange: (Mode) -> Unit,
    onOrderChange: (Order) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onDakutenChange: (Boolean) -> Unit,
    onRareChange: (Boolean) -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall
        )

        SectionTitle(stringResource(R.string.settings_section_mode))
        ModeOption(Mode.HIRAGANA, settings.mode, stringResource(R.string.mode_hiragana), onModeChange)
        ModeOption(Mode.KATAKANA, settings.mode, stringResource(R.string.mode_katakana), onModeChange)
        ModeOption(Mode.BOTH, settings.mode, stringResource(R.string.mode_both), onModeChange)
        ModeOption(Mode.KANJI, settings.mode, stringResource(R.string.mode_kanji), onModeChange)

        HorizontalDivider()

        SectionTitle(stringResource(R.string.settings_section_order))
        OrderOption(Order.SEQUENTIAL, settings.order, stringResource(R.string.order_sequential), onOrderChange)
        OrderOption(Order.SHUFFLED, settings.order, stringResource(R.string.order_shuffled), onOrderChange)

        HorizontalDivider()

        SectionTitle(stringResource(R.string.settings_section_interval))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 4, 6).forEach { hours ->
                FilterChip(
                    selected = settings.intervalHours == hours,
                    onClick = { onIntervalChange(hours) },
                    label = { Text("${hours}h") }
                )
            }
        }

        HorizontalDivider()

        ToggleRow(
            label = stringResource(R.string.settings_toggle_dakuten),
            checked = settings.includeDakuten,
            onCheckedChange = onDakutenChange
        )
        ToggleRow(
            label = stringResource(R.string.settings_toggle_rare),
            checked = settings.includeRare,
            onCheckedChange = onRareChange
        )

        HorizontalDivider()

        Text(
            text = stringResource(R.string.battery_optimization_hint),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onOpenBatterySettings) {
            Text(stringResource(R.string.battery_optimization_button))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ModeOption(
    value: Mode,
    selected: Mode,
    label: String,
    onSelect: (Mode) -> Unit,
    enabled: Boolean = true
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) }, enabled = enabled)
        Text(text = label, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun OrderOption(
    value: Order,
    selected: Order,
    label: String,
    onSelect: (Order) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) })
        Text(text = label, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, modifier = Modifier.padding(top = 12.dp, end = 8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
