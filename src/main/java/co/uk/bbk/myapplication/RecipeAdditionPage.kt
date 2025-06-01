package co.uk.bbk.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RecipeAdditionPage : AppCompatActivity() {

    private lateinit var categoryChosen: Spinner
    private val recipeCategories =
        arrayOf("Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_addition_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        categoryChosen = findViewById(R.id.spinner) // Make sure this ID matches your XML

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryChosen.adapter = adapter

        val defaultSelection = "Lunch"
        val position = adapter.getPosition(defaultSelection)
        categoryChosen.setSelection(position)


        categoryChosen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = recipeCategories[position]
                Toast.makeText(
                    this@RecipeAdditionPage,
                    "Selected: $selectedCategory",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }
}