package com.brivaro.compoundinterestcalculator

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brivaro.compoundinterestcalculator.ui.theme.CompoundInterestCalculatorTheme
import com.brivaro.compoundinterestcalculator.viewmodel.CalculatorViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.brivaro.compoundinterestcalculator.ui.theme.BackgroundBlue
import com.brivaro.compoundinterestcalculator.ui.theme.GradientStart
import com.brivaro.compoundinterestcalculator.ui.theme.GradientEnd
import com.brivaro.compoundinterestcalculator.ui.theme.LightBlue
//import com.brivaro.compoundinterestcalculator.ui.theme.LightBlueVariant
//import com.brivaro.compoundinterestcalculator.ui.theme.DarkBlue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import com.brivaro.compoundinterestcalculator.data.CalculatorState
import com.brivaro.compoundinterestcalculator.ui.theme.DarkBlue
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompoundInterestCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val state = viewModel.state
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.GERMANY) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(BackgroundBlue)
    ) {
        // Header
        Header()
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Fields
            InputField(
                value = state.startingAmount,
                onValueChange = viewModel::onStartingAmountChange,
                label = "💶 Starting Amount (€)",
                keyboardType = KeyboardType.Decimal
            )

            InputField(
                value = state.interestRate,
                onValueChange = viewModel::onInterestRateChange,
                label = "📈 Interest Rate (%)",
                keyboardType = KeyboardType.Decimal
            )

            // Time Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputField(
                    value = state.years,
                    onValueChange = viewModel::onYearsChange,
                    label = "📆 Years",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                InputField(
                    value = state.months,
                    onValueChange = viewModel::onMonthsChange,
                    label = "🕒 Months",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tax Bracket Selector
            PayIntervalSelector(
                selectedPayInterval = state.selectedPayInterval,
                onPayIntervalChange = viewModel::onPayIntervalChange,
                payIntervals = viewModel.payIntervals,
                years = state.years,
                months = state.months
            )

            // Tax Bracket Selector
            TaxBracketSelector(
                selectedTaxBracket = state.selectedTaxBracket,
                onTaxBracketChange = viewModel::onTaxBracketChange,
                taxBrackets = viewModel.taxBrackets
            )

            // Results Card
            ResultsCard(state = state, formatter = formatter)

            // Savings Graph
            SavingsGraph(state = state, selectedBracket = state.selectedTaxBracket)
            SavingsGraphOverTime(state = state)
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background( // Aseguramos que el fondo se extienda hasta el área superior
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(24.dp) // Padding de los contenidos dentro del Box
    ) {
    Text(
            text = "💰 Interest Calculator",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 35.dp) // Añadido padding superior solo al texto
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),

        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent, // Sin borde cuando está enfocado
            unfocusedBorderColor = Color.Transparent, // Sin borde cuando no está enfocado
            cursorColor = DarkBlue, // Color del cursor
            containerColor = Color.Transparent // Fondo blanco
        )
    )
}


@Composable
private fun PayIntervalSelector(
    selectedPayInterval: Int,
    onPayIntervalChange: (Int) -> Unit,
    payIntervals: List<Int>,
    years: String,
    months: String
) {
    // Convierte el valor de meses a Int. Si no es un número válido, asigna 0.
    val monthsInt = months.toIntOrNull() ?: 0
    // Comprueba si 'years' es una cadena vacía o tiene un valor numérico.
    val isYearsEmpty = years.isEmpty()

    // Lista de intervalos habilitados (que no se desactivan por el número de meses)
    val enabledIntervals = payIntervals.filter { payInterval ->
        // Si 'years' está vacío, desactiva los intervalos de pago que son mayores que 'monthsInt'
        val isMultipleOfPayInterval = monthsInt % (12 / payInterval) == 0
        if (isYearsEmpty) {
            monthsInt >= (12 / payInterval) && isMultipleOfPayInterval // Solo habilitar si months >= payInterval y es múltiplo
        } else {
            true // Si years tiene un valor, habilitar todos los intervalos de pago
        }
    }

    // Si el intervalo seleccionado ya no es válido, seleccionamos el mayor intervalo disponible.
    val newSelectedPayInterval = if (selectedPayInterval !in enabledIntervals) {
        enabledIntervals.maxOrNull() ?: selectedPayInterval // Elige el más grande disponible
    } else {
        selectedPayInterval
    }

    // Actualizar el estado si el intervalo seleccionado ha cambiado
    if (newSelectedPayInterval != selectedPayInterval) {
        onPayIntervalChange(newSelectedPayInterval)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "⌛ Select Payment Interval",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            payIntervals.forEach { payInterval ->
                // Si 'years' está vacío, desactiva los intervalos de pago que son mayores que 'monthsInt' o no son múltiplos
                val isEnabled = if (isYearsEmpty) {
                    monthsInt >= (12 / payInterval) && monthsInt % (12 / payInterval) == 0 // Habilitar solo si months es múltiplo
                } else {
                    true // Si years tiene un valor, habilitar todos los intervalos de pago
                }

                FilterChip(
                    selected = newSelectedPayInterval == payInterval,
                    onClick = { if (isEnabled) onPayIntervalChange(payInterval) },
                    label = {
                        val monthsLabel = 12 / payInterval
                        Text("$monthsLabel ${if (monthsLabel == 1) "month" else "months"}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LightBlue,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = isEnabled // Desactiva el chip si no es habilitado
                )
            }
        }
    }
}



@Composable
private fun TaxBracketSelector(
    selectedTaxBracket: Double,
    onTaxBracketChange: (Double) -> Unit,
    taxBrackets: List<Double>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "💸 Select Tax Bracket",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            taxBrackets.forEach { taxBracket ->
                FilterChip(
                    selected = selectedTaxBracket == taxBracket,
                    onClick = { onTaxBracketChange(taxBracket) },
                    label = { Text("${(taxBracket * 100).toInt()}%") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LightBlue,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ResultsCard(
    state: CalculatorState,
    formatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        BackgroundBlue,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gross Profit:")
                Text(
                    formatter.format(state.grossProfit),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net Profit:")
                Text(
                    formatter.format(state.netProfit),
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            }
        }
    }
}

@Composable
private fun SavingsGraph(
    state: CalculatorState,
    selectedBracket: Double, // Recibimos el bracket seleccionado
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📉 Tax Deduction and Profit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = width / 4 // Ajustamos para que haya 4 partes: Ganancia, Deducción y ejes
                    val maxValue = state.grossProfit

                    // Draw axes
                    drawLine(
                        color = Color.Gray,
                        start = Offset(40f, height - 40f),
                        end = Offset(width - 40f, height - 40f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Gray,
                        start = Offset(40f, 40f),
                        end = Offset(40f, height - 40f),
                        strokeWidth = 2f
                    )

                    // Cálculo de deducción por el bracket seleccionado
                    val taxDeduction = (state.grossProfit * selectedBracket).toFloat()
                    val netAmount = state.grossProfit - taxDeduction // Ganancia neta

                    if (maxValue > 0) {
                        // Calcular la altura de la barra en función del valor máximo
                        val taxDeductionBarHeight = ((taxDeduction / maxValue)).toFloat() * (height - 80f)
                        val netAmountBarHeight = ((netAmount / maxValue)).toFloat() * (height - 80f)

                        // Draw bars for tax deduction and net profit
                        drawRect(
                            color = Color(255, 114, 55, 245).copy(alpha = 0.6f), // Rojo más suave
                            topLeft = Offset(40f, height - 40f - taxDeductionBarHeight),
                            size = Size(barWidth - 20f, taxDeductionBarHeight)
                        )

                        drawRect(
                            color = Color(68, 253, 129, 216).copy(alpha = 0.6f), // Verde más suave
                            topLeft = Offset(40f + barWidth, height - 40f - netAmountBarHeight),
                            size = Size(barWidth - 20f, netAmountBarHeight)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            val labelYOffset = -10f // Ajuste para alejar las etiquetas de la línea del eje X

                            // Etiqueta para deducción de impuestos
                            drawText(
                                "${(selectedBracket * 100).toInt()}% (-${"%.2f".format(taxDeduction)}€)",
                                20 + barWidth / 2, // Centra sobre la barra
                                height - labelYOffset,
                                Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 30f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )

                            // Etiqueta para el valor neto
                            drawText(
                                "Net: ${"%.2f".format(netAmount)}€",
                                20 + barWidth + barWidth / 2, // Centra sobre la siguiente barra
                                height - labelYOffset,
                                Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 30f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }

                    }

                    // Leyenda a la derecha del gráfico
                    val legendX = width - 340f
                    val legendYStart = 40f
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "🔴 Bracket: ${((selectedBracket * 100).toInt())}% (-${"%.2f".format(state.grossProfit * selectedBracket)}€)",
                            legendX,
                            legendYStart,
                            Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 24f
                            }
                        )
                        drawText(
                            "🟢 Net: ${"%.2f".format(netAmount)}€",
                            legendX,
                            legendYStart + 30f,
                            Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 24f
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsGraphOverTime(
    state: CalculatorState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "👛 Savings Growth Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height

                    // Extraemos y convertimos los valores del state
                    val principal = state.startingAmount.toDoubleOrNull() ?: 0.0
                    val annualRate = state.interestRate.toDoubleOrNull() ?: 0.0
                    val monthlyRate = annualRate / 100 / 12

                    val years = state.years.toIntOrNull() ?: 0
                    val months = state.months.toIntOrNull() ?: 0
                    val totalMonths = years * 12 + months

                    // Calculamos el total final para escalar el gráfico
                    // Asumimos que se aplica interés compuesto mensualmente sin aportaciones adicionales
                    val finalTotal = if (totalMonths > 0)
                        principal * (1 + monthlyRate).pow(totalMonths)
                    else principal

                    // Márgenes para el gráfico
                    val leftMargin = 60f
                    val rightMargin = 40f
                    val topMargin = 40f
                    val bottomMargin = 40f

                    val graphWidth = width - (leftMargin + rightMargin)
                    val graphHeight = height - (topMargin + bottomMargin)

                    // Dibujar ejes
                    drawLine(
                        color = Color.Gray,
                        start = Offset(leftMargin, height - bottomMargin),
                        end = Offset(width - rightMargin, height - bottomMargin),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Gray,
                        start = Offset(leftMargin, topMargin),
                        end = Offset(leftMargin, height - bottomMargin),
                        strokeWidth = 2f
                    )

                    // Dibujar barras para cada mes
                    // Calculamos el ancho de cada barra y dejamos un pequeño espacio entre ellas
                    val barSpacing = 4f
                    val barWidth = (graphWidth / totalMonths) - barSpacing

                    for (i in 0 until totalMonths) {
                        // Para el mes i+1 calculamos el total acumulado
                        val monthIndex = i + 1
                        val totalAmount = principal * (1 + monthlyRate).pow(monthIndex)

                        // Altura de la barra escalada
                        val barTotalHeight = (totalAmount / finalTotal * graphHeight).toFloat()

                        // Altura de la parte azul (monto inicial)
                        // Se dibuja siempre en proporción al principal (o monto base) respecto al total de ese mes
                        val blueHeight = (principal / totalAmount * barTotalHeight).toFloat()
                        val orangeHeight = barTotalHeight - blueHeight

                        // Posición X de la barra
                        val x = leftMargin + i * (barWidth + barSpacing)
                        // Posición Y de la barra (se dibuja de abajo hacia arriba)
                        val y = height - bottomMargin - barTotalHeight

                        // Dibujar rectángulo azul (monto inicial)
                        drawRect(
                            color = Color.Blue,
                            topLeft = Offset(x, height - bottomMargin - blueHeight),
                            size = Size(barWidth, blueHeight)
                        )
                        // Dibujar rectángulo naranja (intereses) encima del azul
                        drawRect(
                            color = Color(0xFFFFA500), // Naranja
                            topLeft = Offset(x, y),
                            size = Size(barWidth, orangeHeight)
                        )
                    }

                    // Dibujar month numbers en el eje X
                    val monthNames = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
                    for (i in 0 until totalMonths) {
                        val xPosition = leftMargin + ((i + 0.5f) * graphWidth / totalMonths)
                        drawLine(
                            color = Color.Gray,
                            start = Offset(xPosition, height - bottomMargin),
                            end = Offset(xPosition, height - bottomMargin + 5f),
                            strokeWidth = 2f
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                monthNames[i % 12],
                                xPosition - 10f,
                                height - 10f,
                                Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 24f
                                }
                            )
                        }
                    }

                    // Dibujar leyenda
                    val legendX = width - 200f
                    val legendYStart = 10f
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "🔵 Initial Amount",
                            legendX,
                            legendYStart,
                            Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 24f
                            }
                        )
                        drawText(
                            "🟠 Interest",
                            legendX,
                            legendYStart + 30f,
                            Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 24f
                            }
                        )
                    }
                }
            }
        }
    }
}