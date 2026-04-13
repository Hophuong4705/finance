package com.example.cuoikyltdd.domain.usecase

import com.example.cuoikyltdd.domain.model.Transaction
import javax.inject.Inject

class ParseNotificationUseCase @Inject constructor() {

    operator fun invoke(title: String, content: String): Transaction? {

        val regex = Regex("([+-])\\s*([\\d,.]+)((?:\\s*[Vv][Nn][Dd])?)")
        val matchResult = regex.find(content)

        if (matchResult != null) {
            val sign = matchResult.groupValues[1]
            val amountString = matchResult.groupValues[2].replace(",", "").replace(".", "")

            val amount = amountString.toDoubleOrNull() ?: return null
            val type = if (sign == "+") "INCOME" else "EXPENSE"

            return Transaction(
                amount = amount,
                date = System.currentTimeMillis(),
                source = title,
                type = type,
                note = content
            )
        }
        return null
    }
}