package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.parser.ItalianVocalParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lista Spesa Vocale", appName)
    }

    @Test
    fun `test speech parser single product`() {
        val result = ItalianVocalParser.parseInput("aggiungi pane")
        assertEquals(1, result.size)
        assertEquals("Pane", result[0].name)
        assertEquals("Gastronomia", result[0].category)
        assertEquals(1, result[0].quantity)
        assertEquals("", result[0].unit)
    }

    @Test
    fun `test speech parser quantities and units`() {
        val result = ItalianVocalParser.parseInput("inserisci 3 litri di latte intero")
        assertEquals(1, result.size)
        assertEquals("Latte intero", result[0].name)
        assertEquals("Latticini", result[0].category)
        assertEquals(3, result[0].quantity)
        assertEquals("litri", result[0].unit)
    }

    @Test
    fun `test speech parser complex multi input`() {
        val result = ItalianVocalParser.parseInput("metti due chili carne e 5 mele e pane")
        assertEquals(3, result.size)

        // Part 1: Carne
        assertEquals("Carne", result[0].name)
        assertEquals("Macelleria", result[0].category)
        assertEquals(2, result[0].quantity)
        assertEquals("chili", result[0].unit)

        // Part 2: Mele
        assertEquals("Mele", result[1].name)
        assertEquals("Ortofrutta", result[1].category)
        assertEquals(5, result[1].quantity)
        assertEquals("", result[1].unit)

        // Part 3: Pane
        assertEquals("Pane", result[2].name)
        assertEquals("Gastronomia", result[2].category)
        assertEquals(1, result[2].quantity)
        assertEquals("", result[2].unit)
    }
}
