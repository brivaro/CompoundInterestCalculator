package com.brivaro.compoundinterestcalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brivaro.compoundinterestcalculator.data.CalculatorState
import kotlin.math.pow
import com.brivaro.compoundinterestcalculator.data.TaxBracketData

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    val taxBrackets = listOf(0.19, 0.21, 0.23, 0.27, 0.28)
    val payIntervals = listOf(12, 4, 2) /*PAGOS: mensuales, trimestrales, semestrales*/

    fun onStartingAmountChange(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            state = state.copy(startingAmount = amount)
            calculateResults()
        }
    }

    fun onInterestRateChange(rate: String) {
        if (rate.isEmpty() || rate.matches(Regex("^\\d*\\.?\\d*$"))) {
            state = state.copy(interestRate = rate)
            calculateResults()
        }
    }

    fun onYearsChange(years: String) {
        if (years.isEmpty() || years.matches(Regex("^\\d*$"))) {
            state = state.copy(years = years)
            calculateResults()
        }
    }

    fun onMonthsChange(months: String) {
        if (months.isEmpty() || months.matches(Regex("^\\d*$"))) {
            state = state.copy(months = months)
            calculateResults()
        }
    }

    fun onPayIntervalChange(payInterval: Int) {
        state = state.copy(selectedPayInterval = payInterval)
        calculateResults()
    }

    fun onTaxBracketChange(taxBracket: Double) {
        state = state.copy(selectedTaxBracket = taxBracket)
        calculateResults()
    }

    private fun calculateResults() {
        val startingAmount = state.startingAmount.toDoubleOrNull() ?: 0.0
        val annualRate = (state.interestRate.toDoubleOrNull() ?: 0.0) / 100
        val years = state.years.toDoubleOrNull() ?: 0.0
        val months = state.months.toDoubleOrNull() ?: 0.0
        val compoundingFrequency = state.selectedPayInterval // Veces por año, por defecto anual

        val totalYears = years + (months / 12)

        // Fórmula general del interés compuesto
        val futureValue = startingAmount * (1 + annualRate / compoundingFrequency)
            .pow(compoundingFrequency * totalYears)
        val grossProfit = futureValue - startingAmount
        val netProfit = grossProfit * (1 - state.selectedTaxBracket)

        // Desglose por tramos fiscales
        val taxBracketBreakdown = taxBrackets.map { bracket ->
            TaxBracketData(
                bracket = bracket,
                netAmount = grossProfit * (1 - bracket)
            )
        }

        state = state.copy(
            grossProfit = grossProfit,
            netProfit = netProfit,
            taxBracketBreakdown = taxBracketBreakdown
        )
    }
} 