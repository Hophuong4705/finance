package com.example.cuoikyltdd.domain.usecase

import com.example.cuoikyltdd.domain.model.TaxResult
import javax.inject.Inject

class CalculateTaxUseCase @Inject constructor() {
    operator fun invoke(monthlyIncome: Double, numberOfDependents: Int): TaxResult {
        // Định mức theo luật VN hiện hành
        val personalDeduction = 11_000_000.0
        val dependentDeduction = numberOfDependents * 4_400_000.0
        val insurance = monthlyIncome * 0.105 // Bảo hiểm bắt buộc 10.5%

        val totalDeduction = personalDeduction + dependentDeduction + insurance
        val taxableIncome = (monthlyIncome - totalDeduction).coerceAtLeast(0.0)

        // Biểu thuế lũy tiến từng phần
        val tax = when {
            taxableIncome <= 5_000_000 -> taxableIncome * 0.05
            taxableIncome <= 10_000_000 -> taxableIncome * 0.1 - 250_000
            taxableIncome <= 18_000_000 -> taxableIncome * 0.15 - 750_000
            taxableIncome <= 32_000_000 -> taxableIncome * 0.2 - 1_650_000
            taxableIncome <= 52_000_000 -> taxableIncome * 0.25 - 3_250_000
            taxableIncome <= 80_000_000 -> taxableIncome * 0.3 - 5_850_000
            else -> taxableIncome * 0.35 - 9_850_000
        }

        return TaxResult(monthlyIncome, totalDeduction, taxableIncome, tax)
    }
}