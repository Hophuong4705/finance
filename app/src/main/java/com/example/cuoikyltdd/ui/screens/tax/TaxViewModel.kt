package com.example.cuoikyltdd.ui.screens.tax

import androidx.lifecycle.ViewModel
import com.example.cuoikyltdd.domain.model.TaxResult
import com.example.cuoikyltdd.domain.usecase.CalculateTaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TaxViewModel @Inject constructor(
    private val calculateTaxUseCase: CalculateTaxUseCase
) : ViewModel() {

    private val _taxResult = MutableStateFlow<TaxResult?>(null)
    val taxResult: StateFlow<TaxResult?> = _taxResult.asStateFlow()

    fun calculateTax(income: Double, dependents: Int) {
        val result = calculateTaxUseCase(
            monthlyIncome = income,
            numberOfDependents = dependents
        )
        _taxResult.value = result
    }
}