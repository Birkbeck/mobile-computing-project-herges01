package co.uk.bbk.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.bbk.myapplication.databinding.RecipeDialogBinding
import co.uk.bbk.myapplication.databinding.RecipeTestPageBinding

class RecipeListPage : AppCompatActivity() {

    private lateinit var binding: RecipeTestPageBinding
    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter
    private var selectedImageUri: Uri? = null
    private var dialogBinding: RecipeDialogBinding? = null

    private val REQUEST_CODE_RECIPE_DETAIL = 1

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                dialogBinding?.recipeImageView?.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeTestPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Show back button
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

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

        val categories = listOf("All", "Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Other")

        val filterAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = filterAdapter

        binding.filterSpinner.setSelection(0) // Default to "All"

        binding.filterSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = categories[position]
                if (selectedCategory == "All") {
                    viewModel.readAllRecipes()
                } else {
                    viewModel.readRecipesByCategory(selectedCategory)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
            }
        })

        viewModel.recipes.observe(this) { recipes ->
            adapter.updateRecipes(recipes)
        }

        viewModel.readAllRecipes()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        val selectedCategory = binding.filterSpinner.selectedItem as? String ?: "All"
        if (selectedCategory == "All") {
            viewModel.readAllRecipes()
        } else {
            viewModel.readRecipesByCategory(selectedCategory)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RECIPE_DETAIL && resultCode == RESULT_OK) {
            val selectedCategory = binding.filterSpinner.selectedItem as? String ?: "All"
            if (selectedCategory == "All") {
                viewModel.readAllRecipes()
            } else {
                viewModel.readRecipesByCategory(selectedCategory)
            }
        }
    }

    // Shows a dialog to add or edit a recipe
    private fun showAddRecipeDialog(recipe: Recipe?) {
        dialogBinding = RecipeDialogBinding.inflate(layoutInflater)
        val binding = dialogBinding!!

        val categories = listOf("Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Other")

        val adapterSpinner = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapterSpinner

        recipe?.let {
            binding.titleEditText.setText(it.title)
            binding.ingredientEditText.setText(it.ingredient)

            val categoryIndex = categories.indexOfFirst { c -> c.equals(it.category, ignoreCase = true) }
            if (categoryIndex >= 0) {
                binding.categorySpinner.setSelection(categoryIndex)
            }

            selectedImageUri = it.image?.let { uri -> Uri.parse(uri) }
            selectedImageUri?.let { binding.recipeImageView.setImageURI(it) }
        }

        binding.selectImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .create()

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val selectedCategory = binding.categorySpinner.selectedItem as String
            val ingredients = binding.ingredientEditText.text.toString().trim()

            var valid = true
            if (title.isBlank()) {
                binding.titleEditText.error = "Required"
                valid = false
            }
            if (ingredients.isBlank()) {
                binding.ingredientEditText.error = "Required"
                valid = false
            }

            if (valid) {
                if (recipe == null) {
                    viewModel.addRecipe(title, selectedCategory, ingredients, selectedImageUri?.toString())
                    Toast.makeText(this, "Recipe added", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedRecipe = recipe.copy(
                        title = title,
                        category = selectedCategory,
                        ingredient = ingredients,
                        image = selectedImageUri?.toString()
                    )
                    viewModel.editRecipe(updatedRecipe)
                    Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Shows a confirmation popup before deleting a recipe
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

    // Opens the RecipePageActivity to view the details of a recipe
    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipePageActivity::class.java)
        intent.putExtra("recipe", recipe)
        startActivityForResult(intent, REQUEST_CODE_RECIPE_DETAIL)
    }
}
