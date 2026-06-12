package com.example

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ShoppingItem
import com.example.ui.parser.ItalianVocalParser
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShoppingViewModel
import java.util.Locale
import android.net.Uri
import coil.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ShoppingViewModel = viewModel(
                    factory = ShoppingViewModel.Factory(application)
                )
                ShoppingListScreen(viewModel = viewModel)
            }
        }
    }
}

// Category design mapping
data class CategoryUi(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val containerColor: Color
)

val CATEGORIES = listOf(
    CategoryUi("Bevande", "Bevande", "🥤", Color(0xFF3B82F6), Color(0xFF1E293B)),
    CategoryUi("Gastronomia", "Gastronomia", "🥘", Color(0xFFF59E0B), Color(0xFF2D251E)),
    CategoryUi("Pescheria", "Pescheria", "🐟", Color(0xFF06B6D4), Color(0xFF162E35)),
    CategoryUi("Surgelati", "Surgelati", "🍲", Color(0xFF10B981), Color(0xFF142E25)),
    CategoryUi("Macelleria", "Macelleria", "🥩", Color(0xFFEF4444), Color(0xFF3F1B1B)),
    CategoryUi("Salumi", "Salumi", "🍖", Color(0xFFEC4899), Color(0xFF3B1E2E)),
    CategoryUi("Dolci", "Dolci", "🍫", Color(0xFFA855F7), Color(0xFF2E1B3B)),
    CategoryUi("Colazione", "Colazione", "☕", Color(0xFF845D0F), Color(0xFF261D15)),
    CategoryUi("Snack", "Snack", "🍟", Color(0xFFF43F5E), Color(0xFF3B1B25)),
    CategoryUi("Dispensa", "Dispensa", "🍝", Color(0xFF14B8A6), Color(0xFF122E2B)),
    CategoryUi("Fresco", "Fresco", "🥬", Color(0xFF22C55E), Color(0xFF142E1B)),
    CategoryUi("Latticini", "Latticini", "🧀", Color(0xFFEAB308), Color(0xFF2E2B15)),
    CategoryUi("Ortofrutta", "Ortofrutta", "🍎", Color(0xFF10B981), Color(0xFF142E21)),
    CategoryUi("Casa", "Casa", "🧻", Color(0xFF6B7280), Color(0xFF1F2937)),
    CategoryUi("Pulizia", "Pulizia", "🧹", Color(0xFF6366F1), Color(0xFF1F213A)),
    CategoryUi("Igiene", "Igiene", "🧼", Color(0xFF06B6D4), Color(0xFF142A31)),
    CategoryUi("Altro", "Altro", "📦", Color(0xFF94A3B8), Color(0xFF1E293B))
)

fun getCategoryUi(name: String): CategoryUi {
    return CATEGORIES.firstOrNull { it.id == name } ?: CATEGORIES.last()
}

// Autocomplete models and helper logic
data class SuggestionItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: String
)

fun getSuggestions(query: String): List<SuggestionItem> {
    if (query.isBlank()) return emptyList()
    val cleanedQuery = query.lowercase(Locale.ITALIAN).trim()
    val words = cleanedQuery.split("\\s+".toRegex())
    val lastWord = words.lastOrNull() ?: ""
    if (lastWord.length < 2) return emptyList()

    return ItalianVocalParser.catalog.entries
        .filter { it.key.contains(lastWord) }
        .take(5)
        .map { entry ->
            SuggestionItem(
                id = entry.key,
                name = entry.key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() },
                emoji = entry.value.emoji,
                category = entry.value.category
            )
        }
}

fun applySuggestion(currentInput: String, suggestion: String): String {
    val words = currentInput.split("\\s+".toRegex()).toMutableList()
    if (words.isNotEmpty()) {
        val lastIdx = words.size - 1
        words[lastIdx] = suggestion
    } else {
        words.add(suggestion)
    }
    return words.joinToString(" ")
}

fun formatShoppingListForSharing(items: List<ShoppingItem>): String {
    if (items.isEmpty()) return "La tua lista della spesa è vuota."
    val sb = java.lang.StringBuilder()
    sb.append("🛒 *Lista della Spesa* 🛒\n\n")

    // Group items by category
    val grouped = items.groupBy { it.category }
    grouped.forEach { (catName, catItems) ->
        val catUi = getCategoryUi(catName)
        sb.append("${catUi.emoji} *${catUi.name.uppercase(Locale.ITALIAN)}*\n")
        catItems.forEach { item ->
            val completion = if (item.isCompleted) "✅ " else "⬜ "
            val qtyUnit = if (item.unit.isNotBlank()) " ${item.unit}" else ""
            sb.append("  $completion${item.name} (x${item.quantity}$qtyUnit)")
            if (item.price > 0.0) {
                sb.append(" - €${"%.2f".format(item.price)} cad.")
                val totalItemPrice = item.price * item.quantity
                if (item.quantity > 1) {
                    sb.append(" (Tot: €${"%.2f".format(totalItemPrice)})")
                }
            }
            if (item.notes.isNotBlank()) {
                sb.append(" _[Note: ${item.notes}]_")
            }
            sb.append("\n")
        }
        sb.append("\n")
    }

    val totalCost = items.sumOf { it.price * it.quantity }
    val cartCost = items.filter { it.isCompleted }.sumOf { it.price * it.quantity }

    sb.append("-----------------------------\n")
    sb.append("💰 *Totale stimato:* €${"%.2f".format(totalCost)}\n")
    sb.append("🛒 *Nel carrello:* €${"%.2f".format(cartCost)}")

    return sb.toString()
}

@Composable
fun SuggestionChip(
    suggestion: SuggestionItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("suggestion_chip_${suggestion.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(suggestion.emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = suggestion.name,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Tutti") }
    var sortBySelection by remember { mutableStateOf(false) }

    // Dialog state for manual edits
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    // Speech launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.getOrNull(0) ?: ""
            if (spokenText.isNotEmpty()) {
                viewModel.addParsedInput(spokenText)
                Toast.makeText(context, "Aggiunti: $spokenText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Document Creation Launcher for Backup
    val saveBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(viewModel.exportToJson().toByteArray())
                }
                Toast.makeText(context, "Backup salvato con successo!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Errore nel salvataggio: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Document Picker Launcher for Restore
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (!content.isNullOrBlank()) {
                    val success = viewModel.importFromJson(content)
                    if (success) {
                        Toast.makeText(context, "Prodotti importati con successo!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Formato JSON non valido!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Errore nel caricamento: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Dynamic cost calculations
    val totalCost = items.sumOf { it.price * it.quantity }
    val cartCost = items.filter { it.isCompleted }.sumOf { it.price * it.quantity }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Persistent compact input bar at the top (with statusBarsPadding to clear status bar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Red mic voice trigger button on the left
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parla ora...")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Vocal non supportato", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFFE11D48), CircleShape)
                                .testTag("vocal_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Vocale",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Inline fast keyboard add button on the left
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.addParsedInput(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .testTag("add_item_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Aggiungi",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Es. 2 cocacola, 1 pane...", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inputText.isNotBlank()) {
                                        viewModel.addParsedInput(inputText)
                                        inputText = ""
                                    }
                                }
                            ),
                            trailingIcon = {
                                if (inputText.isNotEmpty()) {
                                    IconButton(onClick = { inputText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Pulisci",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // Autocompletion Suggestions Row
                    val suggestions = getSuggestions(inputText)
                    if (suggestions.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                .testTag("autocompletion_list"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(suggestions, key = { it.id }) { sugg ->
                                SuggestionChip(
                                    suggestion = sugg,
                                    onClick = {
                                        val completedInput = applySuggestion(inputText, sugg.name)
                                        if (completedInput.isNotBlank()) {
                                            viewModel.addParsedInput(completedInput)
                                            inputText = ""
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Highly optimized thin layout with zero colossal app labels or large titles.
            // Displays real-time Spesa totals (Conto Totale and Carrello) alongside a square brand icon.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square logo container for optimal sizing appreciation
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.shopping_cart_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Price Metrics Receipts Section
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Totale: ",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "€${"%.2f".format(totalCost)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Carrello: ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "€${"%.2f".format(cartCost)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Toggle Button to Sort/Reorder by Selection state (Pending first, Completed at the bottom)
                IconButton(
                    onClick = { sortBySelection = !sortBySelection },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sort_by_selection_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Riordina per selezione",
                        tint = if (sortBySelection) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Dedicated readable list Share Button for WhatsApp and other chat programs (shows icons, quantities, prices)
                IconButton(
                    onClick = {
                        val readableText = formatShoppingListForSharing(items)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, readableText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Invia la tua Lista della Spesa")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("share_readable_list_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Invia Lista Spesa",
                        tint = MaterialTheme.colorScheme.secondary, // Bright emerald green, perfect match for WhatsApp/sharing!
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Import/Export dialog trigger (for JSON files and folder backups)
                IconButton(
                    onClick = { showImportExportDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("import_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "File Backup JSON",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { showClearConfirmation = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("clear_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Svuota",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Compact horizontal category scrollbar
            CategoryFiltersTabs(
                selectedFilter = selectedCategoryFilter,
                onFilterSelected = { selectedCategoryFilter = it }
            )

            // Main Shopping List Space
            val filteredItems = if (selectedCategoryFilter == "Tutti") {
                items
            } else {
                items.filter { it.category == selectedCategoryFilter }
            }

            if (filteredItems.isEmpty()) {
                EmptyStateView(hasAnyItems = items.isNotEmpty(), activeFilter = selectedCategoryFilter)
            } else {
                val grouped = filteredItems.groupBy { it.category }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (catName, catItems) ->
                        item(key = "hdr_$catName") {
                            val catUi = getCategoryUi(catName)
                            CategoryHeader(catUi = catUi, count = catItems.size)
                        }

                        val sortedCatItems = if (sortBySelection) {
                            catItems.sortedWith(compareBy<ShoppingItem> { it.isCompleted }.thenByDescending { it.createdAt })
                        } else {
                            catItems
                        }

                        items(sortedCatItems, key = { it.id }) { item ->
                            ShoppingItemRow(
                                item = item,
                                onCheckedChange = { viewModel.toggleItemCompletion(item) },
                                onDelete = { viewModel.deleteItem(item) },
                                onQuantityIncrement = { viewModel.updateItem(item.copy(quantity = item.quantity + 1)) },
                                onQuantityDecrement = {
                                    if (item.quantity > 1) {
                                        viewModel.updateItem(item.copy(quantity = item.quantity - 1))
                                    } else {
                                        viewModel.deleteItem(item)
                                    }
                                },
                                onUpdate = { viewModel.updateItem(it) },
                                onClick = { itemToEdit = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for detail adjustments
    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { updatedItem ->
                viewModel.updateItem(updatedItem)
                itemToEdit = null
            }
        )
    }

    // Backup dialogues
    if (showImportExportDialog) {
        ImportExportDialog(
            viewModel = viewModel,
            onSaveBackup = { saveBackupLauncher.launch("lista_spesa_backup.json") },
            onImportBackup = { importBackupLauncher.launch("application/json") },
            onDismiss = { showImportExportDialog = false }
        )
    }

    // Safety clear checks
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Svuotare intera lista?") },
            text = { Text("Questa azione eliminerà permanentemente tutti i prodotti inseriti.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Svuota")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Annulla")
                }
            },
            modifier = Modifier.testTag("clear_all_dialog")
        )
    }
}

@Composable
fun CategoryFiltersTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            val isSelected = selectedFilter == "Tutti"
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onFilterSelected("Tutti") }
            ) {
                Text(
                    text = "Tutti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        items(CATEGORIES) { category ->
            val isSelected = selectedFilter == category.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) category.color else category.containerColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onFilterSelected(category.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.emoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else category.color
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(catUi: CategoryUi, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(catUi.containerColor, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(catUi.emoji, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = catUi.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = catUi.color
            )
        }
        Text(
            text = "$count ${if (count == 1) "prodotto" else "prodotti"}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit,
    onQuantityIncrement: () -> Unit,
    onQuantityDecrement: () -> Unit,
    onUpdate: (ShoppingItem) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val catUi = getCategoryUi(item.category)

    val isComp = item.isCompleted
    val containerBg = if (isComp) Color(0xFF5F151B) else MaterialTheme.colorScheme.surface
    val prNameColor = if (isComp) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isComp) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val actionTint = if (isComp) Color.White else catUi.color

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .testTag("shopping_item_row_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clickable container for pasting/rendering custom image or automatic emoji icon (Enlarged back to 56.dp!)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isComp) Color.White.copy(alpha = 0.2f) else catUi.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imagePath.isNullOrBlank()) {
                    val fallbackPainter = if (item.imagePath.startsWith("http") || item.imagePath.startsWith("content")) {
                        rememberAsyncImagePainter(model = item.imagePath)
                    } else {
                        rememberAsyncImagePainter(model = java.io.File(item.imagePath))
                    }
                    Image(
                        painter = fallbackPainter,
                        contentDescription = "Foto prodotto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = item.emoji.ifEmpty { "📦" },
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details and actions column (clicking toggles completion highlighting)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = prNameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "€${"%.2f".format(item.price)} cad. | Tot: €${"%.2f".format(item.price * item.quantity)}",
                    fontSize = 13.sp,
                    color = subTextColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic utility links: Image Search and Clipboard paste (Larger targets!)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val query = Uri.encode(item.name)
                            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?tbm=isch&q=$query"))
                            context.startActivity(searchIntent)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cerca su Google Immagini",
                            tint = if (isComp) Color(0xFFFED7AA) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            handlePasteImage(context, item) { path ->
                                onUpdate(item.copy(imagePath = path))
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Incolla Immagine da Appunti",
                            tint = if (isComp) Color(0xFFA7F3D0) else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    if (!item.imagePath.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Foto attiva",
                            tint = if (isComp) Color(0xFFA7F3D0) else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Mini quantity selectors (compact dimensions: button 26.dp, icon 13.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                IconButton(
                    onClick = onQuantityDecrement,
                    modifier = Modifier
                        .size(26.dp)
                        .background(if (isComp) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Riduci",
                        tint = if (isComp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isComp) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onQuantityIncrement,
                    modifier = Modifier
                        .size(26.dp)
                        .background(if (isComp) Color.White.copy(alpha = 0.35f) else catUi.color.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumenta",
                        tint = if (isComp) Color.White else actionTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dedicated Pencil Edit Button right next to the delete trigger!
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("edit_item_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Modifica dettagli",
                    tint = if (isComp) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_item_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = if (isComp) Color(0xFFFECACA) else MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun handlePasteImage(context: Context, item: ShoppingItem, onImagePasted: (String) -> Unit) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
        val clipItem = clip.getItemAt(0)
        val uri = clipItem.uri
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = java.io.File(context.filesDir, "item_image_${item.id}_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    onImagePasted(file.absolutePath)
                    Toast.makeText(context, "Immagine salvata e associata al prodotto!", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Errore nella lettura dell'immagine: ${e.message}", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            val text = clipItem.text?.toString()?.trim()
            if (!text.isNullOrBlank() && (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("content://"))) {
                onImagePasted(text)
                Toast.makeText(context, "Collegamento immagine incollato con successo!", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }
    Toast.makeText(context, "Nessun elemento valido negli appunti! Copia prima un'immagine dal browser.", Toast.LENGTH_LONG).show()
}

@Composable
fun EmptyStateView(hasAnyItems: Boolean, activeFilter: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (!hasAnyItems) "🛒" else "🔍",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (!hasAnyItems) "Nessun prodotto in lista" else "Nessuna corrispondenza",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (!hasAnyItems) {
                    "Parla toccando il tasto rosso o digita i prodotti desiderati!"
                } else {
                    "Nessun prodotto trovato per il filtro '$activeFilter'!"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var quantityStr by remember { mutableStateOf(item.quantity.toString()) }
    var priceStr by remember { mutableStateOf(item.price.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    var notes by remember { mutableStateOf(item.notes) }
    var imagePath by remember { mutableStateOf(item.imagePath) }

    var expandedCatMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifica Prodotto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Prodotto") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Row with image preview, search on Google, and paste from clipboard!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imagePath.isNullOrBlank()) {
                            val fallbackPainter = if (imagePath!!.startsWith("http") || imagePath!!.startsWith("content")) {
                                rememberAsyncImagePainter(model = imagePath)
                            } else {
                                rememberAsyncImagePainter(model = java.io.File(imagePath!!))
                            }
                            Image(
                                painter = fallbackPainter,
                                contentDescription = "Anteprima",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val currentCatUi = getCategoryUi(category)
                            Text(
                                text = currentCatUi.emoji.ifEmpty { "📦" },
                                fontSize = 24.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Immagine Prodotto", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Trova sul web o incolla", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val query = Uri.encode(name)
                                val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?tbm=isch&q=$query"))
                                context.startActivity(searchIntent)
                            } else {
                                Toast.makeText(context, "Inserisci prima il nome del prodotto", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cerca su Google",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            handlePasteImage(context, item) { path ->
                                imagePath = path
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Incolla negli appunti",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantità") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Prezzo (€)") },
                        modifier = Modifier.weight(1.2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unità (pz, litri)") },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Category selection drop down
                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    val currentCatUi = getCategoryUi(category)
                    OutlinedTextField(
                        value = "${currentCatUi.emoji} ${currentCatUi.name}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        CATEGORIES.forEach { categoryUi ->
                            DropdownMenuItem(
                                text = { Text("${categoryUi.emoji} ${categoryUi.name}") },
                                onClick = {
                                    category = categoryUi.id
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note aggiuntive") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toIntOrNull() ?: 1
                    val prc = priceStr.toDoubleOrNull() ?: item.price
                    onConfirm(
                        item.copy(
                            name = name,
                            category = category,
                            quantity = qty,
                            price = prc,
                            unit = unit,
                            notes = notes,
                            imagePath = imagePath
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun ImportExportDialog(
    viewModel: ShoppingViewModel,
    onSaveBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputJsonText by remember { mutableStateOf("") }
    var tabIndex by remember { mutableStateOf(0) } // 0 = Export, 1 = Import

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Dati Lista Spesa", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Backup/Esporta") }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Ripristina/Importa") }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (tabIndex == 0) {
                    val json = viewModel.exportToJson()
                    
                    Text(
                        text = "Il codice JSON serve per il backup locale in una cartella a tua scelta o per copiare i dati:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("export_json_field"),
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Lista Spesa JSON", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "JSON copiato negli appunti!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_json_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copia", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onSaveBackup()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("save_backup_file_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salva su File", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dedicated button to send human-readable text list (WhatsApp-style)
                    Button(
                        onClick = {
                            val readableText = formatShoppingListForSharing(viewModel.items.value)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, readableText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Invia Lista Prodotti (WhatsApp)"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_list_text_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Condividi Lista (WhatsApp)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Importa da un file di backup JSON precedentemente salvato sul dispositivo:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            onImportBackup()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_backup_file_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seleziona File di Backup", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "In alternativa, incolla il codice JSON qui sotto:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = inputJsonText,
                        onValueChange = { inputJsonText = it },
                        placeholder = { Text("[{\"name\":\"Pane\",...}]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("import_json_field"),
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (inputJsonText.isNotBlank()) {
                                val success = viewModel.importFromJson(inputJsonText)
                                if (success) {
                                    Toast.makeText(context, "Prodotti importati correttamente!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Formato JSON non valido!", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execute_import_button"),
                        enabled = inputJsonText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importa da Testo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}
