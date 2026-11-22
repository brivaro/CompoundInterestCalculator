package com.brivaro.compoundinterestcalculator.data

data class CalculatorState(
    val startingAmount: String = "",
    val interestRate: String = "",
    val selectedPayInterval: Int = 12,
    val years: String = "",
    val months: String = "",
    val selectedTaxBracket: Double = 0.19,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val taxBracketBreakdown: List<TaxBracketData> = emptyList()
)

data class TaxBracketData(
    val bracket: Double,
    val netAmount: Double
) 