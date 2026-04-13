package com.example.cuoikyltdd.ui.screens.report

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: FinanceRepositoryImpl
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun exportDataToExcel() {
        viewModelScope.launch {
            try {
                // SỬA: Đảm bảo repository có hàm này, nếu repository dùng tên khác (vd: getTransactions) thì phải đổi tại đây
                val transactions = repository.getAllTransactions().first()

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("GiaoDich")

                // Header
                val header = sheet.createRow(0)
                val titles = listOf("Ngày", "Nội dung", "Số tiền", "Loại", "Nguồn")
                titles.forEachIndexed { i, title -> header.createCell(i).setCellValue(title) }

                // Data
                transactions.forEachIndexed { i, tx ->
                    val row = sheet.createRow(i + 1)
                    row.createCell(0).setCellValue(dateFormatter.format(Date(tx.date))) // tx.date là Long
                    row.createCell(1).setCellValue(tx.note)
                    row.createCell(2).setCellValue(tx.amount)
                    row.createCell(3).setCellValue(tx.type)
                    row.createCell(4).setCellValue(tx.source)
                }

                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, "BaoCao_Excel_${System.currentTimeMillis()}.xlsx")
                FileOutputStream(file).use { workbook.write(it) }
                workbook.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exportDataToPDF() {
        viewModelScope.launch {
            try {
                val transactions = repository.getAllTransactions().first()
                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BaoCao_PDF_${System.currentTimeMillis()}.pdf")

                val writer = PdfWriter(FileOutputStream(path))
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                document.add(Paragraph("BAO CAO TAI CHINH").setBold().setFontSize(18f))

                val table = Table(floatArrayOf(2f, 4f, 2f, 2f))
                table.addCell("Ngay"); table.addCell("Noi dung"); table.addCell("Tien"); table.addCell("Nguon")

                transactions.forEach { tx ->
                    table.addCell(dateFormatter.format(Date(tx.date)))
                    table.addCell(tx.note)
                    table.addCell(tx.amount.toString())
                    table.addCell(tx.source)
                }

                document.add(table)
                document.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}