package co.uk.bbk.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.uk.bbk.myapplication.databinding.RecipeDialogBinding
import co.uk.bbk.myapplication.R

class RecipePageActivity : AppCompatActivity() {

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var recipe: Recipe
    private lateinit var titleTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var menuButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_page)

        // Setup DAO in ViewModel
        val dao = RecipesDatabase.getInstance(applicationContext).recipesDao()
        viewModel.recipesDao = dao

        // Get passed Recipe
        recipe = intent.getSerializableExtra("recipe") as? Recipe
            ?: return finish()

        // Bind views
        titleTextView = findViewById(R.id.recipeNameTextView)
        ingredientsTextView = findViewById(R.id.ingredientsTextView)
        menuButton = findViewById(R.id.menuButton)

        // Set content
        titleTextView.text = recipe.title
        ingredientsTextView.text = recipe.ingredient

        // Popup menu logic
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, menuButton)
            popupMenu.menuInflater.inflate(R.menu.recipe_settings, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.editRecipe -> {
                        showEditDialog()
                        true
                    }
                    R.id.deleteRecipe -> {
                        confirmDelete()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun showEditDialog() {
        val dialogBinding = RecipeDialogBinding.inflate(layoutInflater)

        // Set title and ingredients text
        dialogBinding.titleEditText.setText(recipe.title)
        dialogBinding.ingredientEditText.setText(recipe.ingredient)

        // Set Spinner selection based on current category
        val categories = resources.getStringArray(R.array.categories) // Define in strings.xml
        val categoryIndex = categories.indexOf(recipe.category).takeIf { it >= 0 } ?: 0
        dialogBinding.categorySpinner.setSelection(categoryIndex)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.saveButton.setOnClickListener {
            val updatedTitle = dialogBinding.titleEditText.text.toString().trim()
            val updatedCategory = dialogBinding.categorySpinner.selectedItem as String
            val updatedIngredient = dialogBinding.ingredientEditText.text.toString().trim()

            if (updatedTitle.isNotBlank() && updatedCategory.isNotBlank() && updatedIngredient.isNotBlank()) {
                recipe = recipe.copy(
                    title = updatedTitle,
                    category = updatedCategory,
                    ingredient = updatedIngredient
                )

                titleTextView.text = recipe.title
                ingredientsTextView.text = recipe.ingredient

                viewModel.editRecipe(recipe)

                Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                if (updatedTitle.isBlank()) dialogBinding.titleEditText.error = "Required"
                // Spinner always has a selection, so usually no error needed here
                if (updatedIngredient.isBlank()) dialogBinding.ingredientEditText.error = "Required"
            }
        }

        dialog.show()
    }


    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecipe(recipe)  // Delete from DB via ViewModel
                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
                finish()  // Close this activity after deletion
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}