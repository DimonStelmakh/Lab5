package com.example.lab5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
//import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenu(
                onCalculator1Click = { navController.navigate("calculator1") },
                onCalculator2Click = { navController.navigate("calculator2") }
            )
        }
        composable("calculator1") {
            ReliabilityCalculator1()
        }
        composable("calculator2") {
            ReliabilityCalculator2()
        }
    }
}

@Composable
fun MainMenu(
    onCalculator1Click: () -> Unit,
    onCalculator2Click: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onCalculator1Click,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Порівняння надійності одноколової та двоколової систем",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCalculator2Click,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Розрахунок збитків від перерв електропостачання",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

data class PowerLineElement(
    val name: String,
    val omega: Double,
    val tv: Double,
    val mu: Double,
    val tp: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityCalculator1() {
    var result by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var powerLineLength by remember { mutableStateOf("") }
    var numberOfConnections by remember { mutableStateOf("") }

    // типи ліній електропередачі
    val powerLines = remember {
        listOf(
            PowerLineElement("ПЛ-110 кВ", 0.007, 10.0, 0.167, 35.0),
            PowerLineElement("ПЛ-35 кВ", 0.02, 8.0, 0.167, 35.0),
            PowerLineElement("ПЛ-10 кВ", 0.02, 10.0, 0.167, 35.0),
            PowerLineElement("КЛ-10 кВ (траншея)", 0.03, 44.0, 1.0, 9.0),
            PowerLineElement("КЛ-10 кВ (кабельний канал)", 0.005, 17.5, 1.0, 9.0)
        )
    }
    var selectedPowerLine by remember { mutableStateOf(powerLines[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Порівняння надійності одноколової та двоколової систем",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Параметри системи:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        // селектор
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            TextField(
                value = selectedPowerLine.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип лінії електропередачі") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                powerLines.forEach { line ->
                    DropdownMenuItem(
                        text = { Text(line.name) },
                        onClick = {
                            selectedPowerLine = line
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = powerLineLength,
            onValueChange = { powerLineLength = it },
            label = { Text("Довжина лінії (км)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = numberOfConnections,
            onValueChange = { numberOfConnections = it },
            label = { Text("Кількість приєднань 10 кВ") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                try {
                    val lineLength = powerLineLength.toDouble()
                    val connections = numberOfConnections.toInt()

                    // одноколова система
                    val omegaSwitch110 = 0.01
                    val omegaLine110 = selectedPowerLine.omega * lineLength
                    val omegaTransformer = 0.015
                    val omegaSwitch10 = 0.02
                    val omegaConnections = 0.03 * connections

                    val omegaOc = omegaSwitch110 + omegaLine110 + omegaTransformer +
                            omegaSwitch10 + omegaConnections

                    val tvOc = (30.0 * omegaSwitch110 + selectedPowerLine.tv * omegaLine110 +
                            100.0 * omegaTransformer + 15.0 * omegaSwitch10 +
                            2.0 * omegaConnections) / omegaOc

                    val kaOc = omegaOc * tvOc / 8760

                    val kpOc = 1.2 * (43.0 / 8760)

                    // двоколова система
                    val omegaDk = 2 * omegaOc * (kaOc + kpOc)

                    val omegaDs = omegaDk + 0.02

                    // формуємо висновок
                    val conclusion = if (omegaDs < omegaOc) {
                        "двоколова система має вищу надійність, ніж одноколова"
                    } else if (omegaDs > omegaOc) {
                        "одноколова система має вищу надійність, ніж двоколова"
                    } else {
                        "одноколова та двоколова системи мають однакову надійність"
                    }

                    result = """
                        Одноколова система:
                        Сумарна частота відмов (ωос): ${String.format("%.6f", omegaOc)} рік⁻¹
                        Середня тривалість відновлення (tв.ос): ${String.format("%.2f", tvOc)} год
                        Коефіцієнт аварійного простою (ka.ос): ${String.format("%.6f", kaOc)}
                        Коефіцієнт планового простою (kп.ос): ${String.format("%.6f", kpOc)}
                        
                        Двоколова система:
                        Частота відмов без секційного вимикача (ωдк): ${String.format("%.6f", omegaDk)} рік⁻¹
                        Частота відмов з секційним вимикачем (ωдс): ${String.format("%.6f", omegaDs)} рік⁻¹
                        
                        Висновок: $conclusion
                    """.trimIndent()

                } catch (e: NumberFormatException) {
                    result = "Помилка: Перевірте правильність введених даних"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Розрахувати")
        }

        Text(
            text = result,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

data class TransformerParameters(
    val voltage: Int,
    val omega: Double,
    val tv: Double,
    val kp: Double,
    val Pm: Double,
    val Tm: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityCalculator2() {
    var result by remember { mutableStateOf("") }

    val transformer35kV = remember {
        TransformerParameters(
            voltage = 35,
            omega = 0.01,
            tv = 45.0/1000,
            kp = 4.0/1000,
            Pm = 5.12e3,
            Tm = 6451
        )
    }

    var emergencyLoss by remember { mutableStateOf("") }
    var plannedLoss by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Розрахунок збитків від перерв електропостачання",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Параметри трансформатора 35 кВ:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text("Частота відмов (ω): ${transformer35kV.omega} рік⁻¹")
        Text("Середній час відновлення (tв): ${transformer35kV.tv * 1000} · 10⁻³ року")
        Text("Середній час планового простою (kп): ${transformer35kV.kp * 1000} · 10⁻³")
        Text("Потужність (Pм): ${String.format("%.2f", transformer35kV.Pm)} кВт")
        Text("Час використання максимального навантаження (Tм): ${transformer35kV.Tm} год/рік")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emergencyLoss,
            onValueChange = { emergencyLoss = it },
            label = { Text("Питомі збитки від аварійних вимкнень (грн/кВт·год)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = plannedLoss,
            onValueChange = { plannedLoss = it },
            label = { Text("Питомі збитки від планових вимкнень (грн/кВт·год)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                try {
                    val emergencyLossVal = emergencyLoss.toDouble()
                    val plannedLossVal = plannedLoss.toDouble()

                    val emergencyShortage = transformer35kV.omega *
                            transformer35kV.tv *
                            transformer35kV.Pm *
                            transformer35kV.Tm

                    val plannedShortage = transformer35kV.kp *
                            transformer35kV.Pm *
                            transformer35kV.Tm

                    val totalLosses = emergencyLossVal * emergencyShortage +
                            plannedLossVal * plannedShortage

                    result = """
                        Математичне сподівання аварійного недовідпущення електроенергії:
                        M(Wнед.а) = ${String.format("%.2f", emergencyShortage)} кВт·год
                        
                        Математичне сподівання планового недовідпущення електроенергії:
                        M(Wнед.п) = ${String.format("%.2f", plannedShortage)} кВт·год
                        
                        Математичне сподівання збитків від переривання електропостачання:
                        M(Зпер) = ${String.format("%.2f", totalLosses)} грн
                    """.trimIndent()

                } catch (e: NumberFormatException) {
                    result = "Помилка: Перевірте правильність введених даних"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Розрахувати")
        }

        Text(
            text = result,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
