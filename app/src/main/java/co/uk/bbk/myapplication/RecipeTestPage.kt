package co.uk.bbk.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.bbk.myapplication.databinding.ActivityRecipePageBinding
import co.uk.bbk.myapplication.databinding.RecipeDialogBinding
import co.uk.bbk.myapplication.databinding.RecipeTestPageBinding

// RecipeTestPage: Main list of recipes with add/edit/delete and open detail
class RecipeTestPage : AppCompatActivity() {

    private lateinit var binding: RecipeTestPageBinding
    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

    private val REQUEST_CODE_RECIPE_DETAIL = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeTestPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecipeAdapter(
            onEdit = { recipe -> showAddRecipeDialog(recipe) },
            onDelete = { recipe -> showDeleteConfirmation(recipe) },
            onClick = { recipe -> openRecipeDetail(recipe) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener { showAddRecipeDialog(null) }

        val dao = RecipesDatabase.getInstance(applicationContext).recipesDao()
        viewModel.recipesDao = dao

        viewModel.readAllRecipes()

        viewModel.recipes.observe(this) { recipes ->
            adapter.updateRecipes(recipes)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.readAllRecipes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RECIPE_DETAIL && resultCode == RESULT_OK) {
            viewModel.readAllRecipes()
        }
    }

    private fun showAddRecipeDialog(recipe: Recipe?) {
        val dialogBinding = RecipeDialogBinding.inflate(layoutInflater)

        val categories = listOf("Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Other")

        // Setup Spinner adapter
        val adapterSpinner = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.categorySpinner.adapter = adapterSpinner

        // Set fields if editing
        recipe?.let {
            dialogBinding.titleEditText.setText(it.title)
            dialogBinding.ingredientEditText.setText(it.ingredient)

            val categoryIndex = categories.indexOfFirst { c -> c.equals(it.category, ignoreCase = true) }
            if (categoryIndex >= 0) {
                dialogBinding.categorySpinner.setSelection(categoryIndex)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.saveButton.setOnClickListener {
            val title = dialogBinding.titleEditText.text.toString().trim()
            val selectedCategory = dialogBinding.categorySpinner.selectedItem as String
            val ingredients = dialogBinding.ingredientEditText.text.toString().trim()

            var valid = true
            if (title.isBlank()) {
                dialogBinding.titleEditText.error = "Required"
                valid = false
            }
            if (ingredients.isBlank()) {
                dialogBinding.ingredientEditText.error = "Required"
                valid = false
            }

            if (valid) {
                if (recipe == null) {
                    viewModel.addRecipe(title, selectedCategory, ingredients)
                    Toast.makeText(this, "Recipe added", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedRecipe = recipe.copy(
                        title = title,
                        category = selectedCategory,
                        ingredient = ingredients
                    )
                    viewModel.editRecipe(updatedRecipe)
                    Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(recipe: Recipe) {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete \"${recipe.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecipe(recipe)
                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipePageActivity::class.java)
        intent.putExtra("recipe", recipe)
        startActivityForResult(intent, REQUEST_CODE_RECIPE_DETAIL)
    }
}
