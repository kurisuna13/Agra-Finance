package com.agra.finance

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF0B1020)
private val Panel = Color(0xFF151D31)
private val Panel2 = Color(0xFF1D2942)
private val White = Color(0xFFF4F7FF)
private val Muted = Color(0xFF9AA8C3)
private val Green = Color(0xFF47D7A0)
private val Red = Color(0xFFFF6B7A)
private val Blue = Color(0xFF72A7FF)
private val Amber = Color(0xFFFFC857)

private fun rupiah(value: Long): String =
    "Rp" + NumberFormat.getNumberInstance(Locale("id", "ID")).format(value)

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun prettyDate(s: String): String = try {
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s)!!
    )
} catch (_: Exception) { s }

data class Tx(
    val id: Long,
    val type: String, // income or expense
    val amount: Long,
    val category: String,
    val note: String,
    val date: String,
    val source: String = ""
)

data class Budget(val category: String, val amount: Long)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FinanceApp(getSharedPreferences("agra_finance", MODE_PRIVATE)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(prefs: android.content.SharedPreferences) {
    val budgetDefaults = listOf(
        Budget("Makan", 330_000),
        Budget("Kebutuhan sehari-hari", 70_000),
        Budget("Bensin", 175_000),
        Budget("Kuliah / pribadi", 100_000),
        Budget("Hiburan", 50_000),
        Budget("Tabungan", 300_000),
        Budget("Dana darurat", 75_000),
        Budget("Cadangan", 50_000)
    )
    var transactions by remember {
        mutableStateOf(loadTransactions(prefs) ?: listOf(
            Tx(System.currentTimeMillis(), "income", 750_000, "Sangu ayah", "Sangu bulanan dari ayah", today(), "Ayah"),
            Tx(System.currentTimeMillis() + 1, "income", 400_000, "Uang ibu", "Khusus makan dan kebutuhan sehari-hari", today(), "Ibu")
        ).also { saveTransactions(prefs, it) })
    }
    var budgets by remember { mutableStateOf(budgetDefaults) }
    var tab by remember { mutableStateOf("Ringkasan") }
    var showAdd by remember { mutableStateOf(false) }
    var filterStart by remember { mutableStateOf(today().take(7) + "-01") }
    var filterEnd by remember { mutableStateOf(today()) }
    var filterEnabled by remember { mutableStateOf(false) }

    val filtered = transactions.filter { !filterEnabled || (it.date >= filterStart && it.date <= filterEnd) }
    val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
    val expense = filtered.filter { it.type == "expense" }.sumOf { it.amount }
    val allIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val allExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val balance = allIncome - allExpense
    val categorySpent = transactions.filter { it.type == "expense" }.groupBy { it.category }
        .mapValues { e -> e.value.sumOf { it.amount } }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg, surface = Panel, primary = Green,
            onBackground = White, onSurface = White, secondary = Blue
        )
    ) {
        Scaffold(
            containerColor = Bg,
            topBar = {
                Column(Modifier.fillMaxWidth().background(Bg).padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).background(
                                Brush.linearGradient(listOf(Green, Blue)), RoundedCornerShape(12.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) { Text("A", color = Bg, fontSize = 19.sp, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("AGRA FINANCE", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("PERSONAL MONEY MANAGER", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Keuanganmu, lebih teratur.", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("Satu langkah kecil hari ini, lebih tenang di akhir bulan.", color = Muted, fontSize = 12.sp)
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = Green, contentColor = Bg,
                    shape = CircleShape
                ) { Text("+", fontSize = 30.sp, fontWeight = FontWeight.Normal) }
            },
            bottomBar = {
                NavigationBar(containerColor = Panel) {
                    listOf("Ringkasan", "Anggaran", "Riwayat").forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(when (item) { "Ringkasan" -> "◈"; "Anggaran" -> "▤"; else -> "↻" }, fontSize = 18.sp) },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green, selectedTextColor = Green,
                                indicatorColor = Panel2, unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (tab) {
                "Ringkasan" -> SummaryScreen(
                    Modifier.padding(padding), balance, allIncome, allExpense, transactions,
                    budgets, categorySpent, filtered, income, expense, filterEnabled,
                    { filterEnabled = it }, filterStart, { filterStart = it },
                    filterEnd, { filterEnd = it }, showAll = { tab = "Riwayat" }
                )
                "Anggaran" -> BudgetScreen(Modifier.padding(padding), budgets, categorySpent)
                else -> HistoryScreen(
                    Modifier.padding(padding), transactions, filterStart, { filterStart = it },
                    filterEnd, { filterEnd = it }, filterEnabled, { filterEnabled = it },
                    onDelete = { tx ->
                        transactions = transactions.filterNot { it.id == tx.id }
                        saveTransactions(prefs, transactions)
                    }
                )
            }
        }
        if (showAdd) {
            AddTransactionDialog(
                onDismiss = { showAdd = false },
                onSave = { tx ->
                    transactions = listOf(tx) + transactions
                    saveTransactions(prefs, transactions)
                    showAdd = false
                }
            )
        }
    }
}

@Composable
fun SummaryScreen(
    modifier: Modifier, balance: Long, income: Long, expense: Long, transactions: List<Tx>,
    budgets: List<Budget>, spent: Map<String, Long>, filtered: List<Tx>, filteredIncome: Long,
    filteredExpense: Long, filterEnabled: Boolean, setFilterEnabled: (Boolean) -> Unit,
    filterStart: String, setFilterStart: (String) -> Unit, filterEnd: String,
    setFilterEnd: (String) -> Unit, showAll: () -> Unit
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF1B4A47), Color(0xFF17243D), Color(0xFF202A49))),
                RoundedCornerShape(24.dp)
            ).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SALDO TERCATAT", color = Color(0xFFC2D8D9), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    Text("● LIVE", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(rupiah(balance), color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("Sisa uang dari transaksi yang sudah dicatat", color = Color(0xFFC2D8D9), fontSize = 11.sp)
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Pemasukan", income, Green, Modifier.weight(1f))
                    MetricCard("Pengeluaran", expense, Red, Modifier.weight(1f))
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Arus kas", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Pemasukan vs pengeluaran", color = Muted, fontSize = 12.sp)
                    }
                    Box(Modifier.background(Panel2, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("TOTAL", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                CashflowChart(income, expense, Modifier.fillMaxWidth().height(125.dp))
            }
        }
        item {
            val nearLimit = budgets.filter { it.amount > 0 && (spent[it.category] ?: 0L) >= it.amount * 0.8 && it.category !in listOf("Tabungan", "Dana darurat", "Cadangan") }
            if (nearLimit.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().background(Color(0xFF382A1B), RoundedCornerShape(16.dp)).padding(14.dp)) {
                    Text("⚠ Anggaran hampir habis", color = Amber, fontWeight = FontWeight.Bold)
                    nearLimit.forEach { b ->
                        val used = spent[b.category] ?: 0L
                        Text("${b.category}: ${rupiah(used)} dari ${rupiah(b.amount)}", color = White, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(16.dp)) {
                Text("Lihat berdasarkan tanggal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("Dari", filterStart, setFilterStart, Modifier.weight(1f))
                    DateField("Sampai", filterEnd, setFilterEnd, Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = filterEnabled, onCheckedChange = setFilterEnabled)
                    Text("Terapkan filter", color = Muted, fontSize = 13.sp)
                }
                Text("Pemasukan ${rupiah(filteredIncome)}  •  Pengeluaran ${rupiah(filteredExpense)}", color = Blue, fontSize = 13.sp)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Transaksi terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Lihat semua →", color = Green, modifier = Modifier.clickable { showAll() }, fontSize = 12.sp)
            }
        }
        items(filtered.take(4), key = { it.id }) { tx -> TransactionRow(tx) }
        if (filtered.isEmpty()) item { EmptyState("Belum ada transaksi pada rentang tanggal ini.") }
    }
}

@Composable
fun MetricCard(label: String, value: Long, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.background(Color(0xFF111B2D).copy(alpha = .82f), RoundedCornerShape(16.dp)).padding(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color(0xFFBDCADF), fontSize = 12.sp)
        }
        Spacer(Modifier.height(7.dp))
        Text(rupiah(value), color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CashflowChart(income: Long, expense: Long, modifier: Modifier = Modifier) {
    val max = maxOf(income, expense, 1L).toFloat()
    Canvas(modifier) {
        val barW = size.width * 0.24f
        val baseY = size.height - 25f
        val maxH = size.height - 42f
        val x1 = size.width * 0.27f
        val x2 = size.width * 0.70f
        drawLine(Muted.copy(alpha = .4f), Offset(10f, baseY), Offset(size.width - 10f, baseY), 1f)
        val h1 = maxH * income / max
        val h2 = maxH * expense / max
        drawRoundRect(Green, Offset(x1 - barW / 2, baseY - h1), androidx.compose.ui.geometry.Size(barW, h1), androidx.compose.ui.geometry.CornerRadius(14f))
        drawRoundRect(Red, Offset(x2 - barW / 2, baseY - h2), androidx.compose.ui.geometry.Size(barW, h2), androidx.compose.ui.geometry.CornerRadius(14f))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text("Pemasukan", color = Green, fontSize = 12.sp)
        Text("Pengeluaran", color = Red, fontSize = 12.sp)
    }
}

@Composable
fun BudgetScreen(modifier: Modifier, budgets: List<Budget>, spent: Map<String, Long>) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(18.dp)) {
                Text("Rencana bulan ini", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Anggaran awal • Rp1.150.000", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).background(Color(0xFF173D35), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                        Text("♥", color = Green, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Dana dari ibu • Rp400.000", color = Green, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Khusus makan dan kebutuhan sehari-hari", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        items(budgets) { b ->
            val used = spent[b.category] ?: 0L
            val ratio = (used.toFloat() / b.amount.toFloat()).coerceIn(0f, 1f)
            val color = when { used >= b.amount -> Red; ratio >= .8f -> Amber; else -> Green }
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.category, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("${(ratio * 100).toInt()}%", color = color, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = color, trackColor = Panel2
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("Terpakai ${rupiah(used)}", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Batas ${rupiah(b.amount)}", color = White, fontSize = 12.sp)
                }
                if (used >= b.amount) Text("Batas anggaran tercapai atau terlampaui.", color = Red, fontSize = 12.sp)
                else if (ratio >= .8f && b.category !in listOf("Tabungan", "Dana darurat", "Cadangan")) Text("Hati-hati, anggaran hampir habis.", color = Amber, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier, transactions: List<Tx>, start: String, setStart: (String) -> Unit,
    end: String, setEnd: (String) -> Unit, enabled: Boolean, setEnabled: (Boolean) -> Unit,
    onDelete: (Tx) -> Unit
) {
    val filtered = transactions.filter { !enabled || (it.date >= start && it.date <= end) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(16.dp)) {
                Text("Aktivitas keuangan", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Semua pemasukan dan pengeluaranmu", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("Dari", start, setStart, Modifier.weight(1f))
                    DateField("Sampai", end, setEnd, Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = enabled, onCheckedChange = setEnabled)
                    Text("Filter tanggal aktif", color = Muted, fontSize = 13.sp)
                }
                Text("${filtered.size} transaksi ditampilkan", color = Blue, fontSize = 12.sp)
            }
        }
        if (filtered.isEmpty()) item { EmptyState("Tidak ada transaksi pada rentang tanggal ini.") }
        items(filtered, key = { it.id }) { tx ->
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(12.dp)) {
                TransactionRow(tx)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Hapus transaksi", color = Red, fontSize = 12.sp, modifier = Modifier.clickable { onDelete(tx) }.padding(6.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Tx) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(if (tx.type == "income") Color(0xFF163A34) else Color(0xFF3B202C), CircleShape), contentAlignment = Alignment.Center) {
            Text(if (tx.type == "income") "↙" else "↗", color = if (tx.type == "income") Green else Red, fontSize = 20.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.category, color = White, fontWeight = FontWeight.SemiBold)
            Text("${tx.note.ifBlank { tx.source }} • ${prettyDate(tx.date)}", color = Muted, fontSize = 11.sp)
        }
        Text((if (tx.type == "income") "+" else "−") + rupiah(tx.amount), color = if (tx.type == "income") Green else Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun DateField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(modifier) {
        Text(label, color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            prettyDate(value), color = White, fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(10.dp)).clickable {
                val cal = Calendar.getInstance()
                try {
                    cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)!!
                } catch (_: Exception) {}
                DatePickerDialog(context, { _, y, m, d ->
                    onChange(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }.padding(12.dp)
        )
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Muted, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(onDismiss: () -> Unit, onSave: (Tx) -> Unit) {
    var type by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makan") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var source by remember { mutableStateOf("Ayah") }
    var expanded by remember { mutableStateOf(false) }
    val categories = if (type == "income") listOf("Sangu ayah", "Uang ibu", "Pemasukan lain") else listOf("Makan", "Kebutuhan sehari-hari", "Bensin", "Kuliah / pribadi", "Hiburan", "Tabungan", "Dana darurat", "Cadangan", "Lainnya")
    LaunchedEffect(type) { category = categories.first() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text("Tambah transaksi", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "expense", onClick = { type = "expense" }, label = { Text("Pengeluaran") })
                    FilterChip(selected = type == "income", onClick = { type = "income" }, label = { Text("Pemasukan") })
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("Nominal (Rp)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = Green, unfocusedBorderColor = Muted, focusedLabelColor = Green, unfocusedLabelColor = Muted)
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true, label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = Green, unfocusedBorderColor = Muted, focusedLabelColor = Green, unfocusedLabelColor = Muted)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { category = item; expanded = false })
                        }
                    }
                }
                if (type == "income") {
                    Text("Sumber uang", color = Muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = source == "Ayah", onClick = { source = "Ayah" }, label = { Text("Ayah") })
                        FilterChip(selected = source == "Ibu", onClick = { source = "Ibu" }, label = { Text("Ibu") })
                        FilterChip(selected = source == "Lainnya", onClick = { source = "Lainnya" }, label = { Text("Lainnya") })
                    }
                    if (source == "Ibu") Text("Ingat: uang ibu khusus makan dan kebutuhan sehari-hari.", color = Amber, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = note, onValueChange = { note = it }, label = { Text("Catatan (opsional)") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = Green, unfocusedBorderColor = Muted, focusedLabelColor = Green, unfocusedLabelColor = Muted)
                )
                DateField("Tanggal transaksi", date, { date = it }, Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toLongOrNull() ?: 0L
                if (value > 0) onSave(Tx(System.currentTimeMillis(), type, value, category, note, date, if (type == "income") source else ""))
            }) { Text("Simpan", color = Green) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Muted) } }
    )
}

private fun saveTransactions(prefs: android.content.SharedPreferences, txs: List<Tx>) {
    val arr = JSONArray()
    txs.forEach {
        arr.put(JSONObject().apply {
            put("id", it.id); put("type", it.type); put("amount", it.amount)
            put("category", it.category); put("note", it.note); put("date", it.date); put("source", it.source)
        })
    }
    prefs.edit().putString("transactions", arr.toString()).apply()
}

private fun loadTransactions(prefs: android.content.SharedPreferences): List<Tx>? {
    val raw = prefs.getString("transactions", null) ?: return null
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Tx(o.getLong("id"), o.getString("type"), o.getLong("amount"), o.getString("category"),
                o.optString("note", ""), o.getString("date"), o.optString("source", ""))
        }
    } catch (_: Exception) { null }
}
