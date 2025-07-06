package co.uk.bbk.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import co.uk.bbk.myapplication.databinding.RecipeDialogBinding

class RecipePageActivity : AppCompatActivity() {

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var recipe: Recipe

    private lateinit var titleTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var menuButton: ImageButton
    private lateinit var recipeImageView: ImageView

    private var image: Uri? = null

    // Register for image picking
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                // keep persistable permission for the selected image
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                image = it
                tempDialogBinding?.recipeImageView?.setImageURI(it)
            } catch (e: SecurityException) {
                Log.e("RecipePageActivity", "Failed to take persistable permission", e)
                Toast.makeText(this, "Cannot access selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var tempDialogBinding: RecipeDialogBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_page)

        // toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dao = RecipesDatabase.getInstance(applicationContext).recipesDao()
        viewModel.recipesDao = dao

        recipe = intent.getSerializableExtra("recipe") as? Recipe ?: return finish()

        titleTextView = findViewById(R.id.recipeNameTextView)
        ingredientsTextView = findViewById(R.id.ingredientsTextView)
        categoryTextView = findViewById(R.id.categoryTextView)
        menuButton = findViewById(R.id.menuButton)
        recipeImageView = findViewById(R.id.recipeImageView)

        titleTextView.text = recipe.title
        ingredientsTextView.text = recipe.ingredient
        categoryTextView.text = recipe.category

        // reacquire permission to view the image
        recipe.image?.let { uriString ->
            val uri = Uri.parse(uriString)
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.e("RecipePageActivity", "Lost permission to URI: $uri", e)
            }
            val loaded = loadImageSafely(recipeImageView, uri)
            if (!loaded) {
                Toast.makeText(this, "Image permission lost, please re-select the image.", Toast.LENGTH_LONG).show()
            }
            image = uri
        } ?: run {
            recipeImageView.setImageResource(R.drawable.ic_placeholder_image)
        }

        // Set up the menu button to show a popup menu
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

    // Handle the toolbar back button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Handle the options menu
    private fun showEditDialog() {
        val dialogBinding = RecipeDialogBinding.inflate(layoutInflater)
        tempDialogBinding = dialogBinding

        dialogBinding.titleEditText.setText(recipe.title)
        dialogBinding.ingredientEditText.setText(recipe.ingredient)

        val categories = resources.getStringArray(R.array.categories)
        val adapter = ArrayAdapter(dialogBinding.root.context, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.categorySpinner.adapter = adapter

        val categoryIndex = categories.indexOf(recipe.category).takeIf { it >= 0 } ?: 0
        dialogBinding.categorySpinner.post {
            dialogBinding.categorySpinner.setSelection(categoryIndex)
        }

        // Load existing image  safely
        val loaded = loadImageSafely(dialogBinding.recipeImageView, image)
        if (!loaded) {
            Toast.makeText(this, "Image permission lost, please re-select the image.", Toast.LENGTH_LONG).show()
        }

        dialogBinding.selectImageButton.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.saveButton.setOnClickListener {
            val updatedTitle = dialogBinding.titleEditText.text.toString().trim()
            val updatedCategory = dialogBinding.categorySpinner.selectedItem as? String ?: ""
            val updatedIngredient = dialogBinding.ingredientEditText.text.toString().trim()

            if (updatedTitle.isNotBlank() && updatedIngredient.isNotBlank()) {
                recipe = recipe.copy(
                    title = updatedTitle,
                    category = updatedCategory,
                    ingredient = updatedIngredient,
                    image = image?.toString()
                )

                titleTextView.text = recipe.title
                ingredientsTextView.text = recipe.ingredient
                categoryTextView.text = recipe.category

                val loadedImage = loadImageSafely(recipeImageView, image)
                if (!loadedImage) {
                    Toast.makeText(this, "Image permission lost, please re-select the image.", Toast.LENGTH_LONG).show()
                }

                viewModel.editRecipe(recipe)
                Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                tempDialogBinding = null
            } else {
                if (updatedTitle.isBlank()) dialogBinding.titleEditText.error = "Required"
                if (updatedIngredient.isBlank()) dialogBinding.ingredientEditText.error = "Required"
            }
        }

        dialog.show()
    }

    //ensures the user confirms before deleting a recipe
    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecipe(recipe)
                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Safely load an image into an ImageView, handling permissions and errors
    private fun loadImageSafely(imageView: ImageView, uri: Uri?): Boolean {
        return if (uri == null) {
            imageView.setImageResource(R.drawable.ic_placeholder_image)
            false
        } else {
            try {
                imageView.setImageURI(uri)
                true
            } catch (e: SecurityException) {
                Log.e("RecipePageActivity", "No permission to load image URI", e)
                imageView.setImageResource(R.drawable.ic_placeholder_image)
                false
            }
        }
    }
}
