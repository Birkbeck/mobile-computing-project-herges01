package co.uk.bbk.myapplication

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.uk.bbk.myapplication.databinding.RecipeTestPageBinding
import co.uk.bbk.myapplication.databinding.RecipeDialogBinding
import android.content.Intent

class RecipeTestPage : AppCompatActivity() {

    private lateinit var binding: RecipeTestPageBinding
    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeTestPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RecipeAdapter(
            onEdit = { recipe -> showAddRecipeDialog(recipe) },
            onDelete = { recipe -> showDeleteConfirmation(recipe) },
            onClick = { recipe -> openRecipeDetail(recipe) }
        )

        binding.recyclerView.adapter = adapter
        binding.addButton.setOnClickListener { showAddRecipeDialog(null) }

        val dao = RecipesDatabase.getInstance(applicationContext).recipesDao()
        viewModel.recipesDao = dao
        viewModel.readAllRecipes()

        viewModel.recipes.observe(this) { recipes ->
            adapter.updateRecipes(recipes)
        }
    }

    private fun showAddRecipeDialog(recipe: Recipe?) {
        val dialogBinding = RecipeDialogBinding.inflate(layoutInflater)

        recipe?.let {
            dialogBinding.titleEditText.setText(it.title)
            dialogBinding.categoryEditText.setText(it.category)
            dialogBinding.ingredientEditText.setText(it.ingredient)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.saveButton.setOnClickListener {
            val title = dialogBinding.titleEditText.text.toString().trim()
            val category = dialogBinding.categoryEditText.text.toString().trim()
            val ingredients = dialogBinding.ingredientEditText.text.toString().trim()

            if (title.isNotBlank() && category.isNotBlank() && ingredients.isNotBlank()) {
                if (recipe == null) {
                    // Add new recipe
                    viewModel.addRecipe(title, category, ingredients)
                } else {
                    // Edit existing recipe
                    val updatedRecipe = recipe.copy(
                        title = title,
                        category = category,
                        ingredient = ingredients
                    )
                    viewModel.editRecipe(updatedRecipe)
                }
                dialog.dismiss()
            } else {
                if (title.isBlank()) dialogBinding.titleEditText.error = "Required"
                if (category.isBlank()) dialogBinding.categoryEditText.error = "Required"
                if (ingredients.isBlank()) dialogBinding.ingredientEditText.error = "Required"
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
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipePageActivity::class.java)
        intent.putExtra("recipe", recipe)
        startActivity(intent)
    }
}