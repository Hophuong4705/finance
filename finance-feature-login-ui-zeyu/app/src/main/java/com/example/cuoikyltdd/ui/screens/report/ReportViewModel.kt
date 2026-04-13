package com.example.cuoikyltdd.ui.screens.report

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: FinanceRepositoryImpl
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Hàm format số tiền để in ra file nhìn chuyên nghiệp hơn
    private fun formatCurrency(amount: Double): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }

    fun exportDataToExcel() {
        // 🔥 ĐÃ FIX: Chuyển sang xuất CSV và dùng luồng IO, dùng Throwable bắt mọi lỗi sập App
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transactions = repository.getAllTransactions().first()

                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, "FinanceMe_Data_${System.currentTimeMillis()}.csv")

                file.bufferedWriter().use { out ->
                    // 🔥 QUAN TRỌNG: Ghi BOM (Byte Order Mark) để Excel không bị lỗi font Tiếng Việt
                    out.write("\uFEFF")
                    out.write("Thời gian,Nội dung,Số tiền (VNĐ),Loại giao dịch,Danh mục\n")

                    transactions.forEach { tx ->
                        val date = dateFormatter.format(Date(tx.date))

                        // Xử lý chống lỗi dấu phẩy (,) trong nội dung ghi chú/danh mục
                        val note = "\"${tx.note.replace("\"", "\"\"").ifBlank { "Không có ghi chú" }}\""
                        val amount = tx.amount.toLong().toString()
                        val type = if (tx.type == "INCOME") "Thu nhập" else "Chi tiêu"
                        val source = "\"${tx.source.replace("\"", "\"\"")}\""

                        out.write("$date,$note,$amount,$type,$source\n")
                    }
                }

                Log.d("EXPORT", "✅ Xuất CSV thành công: ${file.absolutePath}")
            } catch (e: Throwable) {
                // Throwable mạnh hơn Exception, chặn được lỗi NoClassDefFoundError nếu có
                Log.e("EXPORT_ERR", "⚠️ Lỗi xuất CSV: ${e.message}")
            }
        }
    }

    fun exportDataToPDF() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transactions = repository.getAllTransactions().first()
                val path = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "FinanceMe_Report_${System.currentTimeMillis()}.pdf"
                )

                val writer = PdfWriter(path)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                // Tiêu đề báo cáo
                document.add(Paragraph("FINANCEME - BAO CAO TAI CHINH").setBold().setFontSize(18f))
                document.add(Paragraph("Ngay xuat: ${dateFormatter.format(Date())}\n\n"))

                // Tạo bảng với tỷ lệ độ rộng các cột: 2 - 3 - 2 - 2
                val table = Table(floatArrayOf(2f, 3f, 2f, 2f))
                table.addCell(Paragraph("Thoi gian").setBold())
                table.addCell(Paragraph("Noi dung").setBold())
                table.addCell(Paragraph("So tien (VND)").setBold())
                table.addCell(Paragraph("Phan loai").setBold())

                transactions.forEach { tx ->
                    table.addCell(dateFormatter.format(Date(tx.date)))

                    // Xử lý bỏ dấu tiếng Việt để iTextPDF không bị vỡ font
                    val noteStr   = stripAccents(tx.note.ifBlank { "Giao dich" })
                    val sourceStr = stripAccents(tx.source)
                    val typeStr   = if (tx.type == "INCOME") "THU" else "CHI"
                    val sign      = if (tx.type == "INCOME") "+" else "-"

                    table.addCell(noteStr)
                    table.addCell("$sign ${formatCurrency(tx.amount)}")
                    table.addCell("$typeStr - $sourceStr")
                }

                document.add(table)
                document.close()
                Log.d("EXPORT", "✅ Xuất PDF thành công: ${path.absolutePath}")
            } catch (e: Throwable) {
                Log.e("EXPORT_ERR", "⚠️ Lỗi xuất PDF: ${e.message}")
            }
        }
    }

    private fun stripAccents(s: String): String {
        val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
            .replace("đ", "d")
            .replace("Đ", "D")
    }
}