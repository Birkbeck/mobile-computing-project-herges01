package co.uk.bbk.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>(listOf())
    val recipes: LiveData<List<Recipe>> = _recipes

    var recipesDao: RecipesDao? = null

    fun readAllRecipes() {
        viewModelScope.launch {
            recipesDao?.let {
                val allRecipes = it.getAllRecipes()
                Log.i("BBK", allRecipes.toString())
                _recipes.value = allRecipes
            }
        }
    }

    fun addRecipe(title: String, category: String, ingredient: String, image: String? = null) {
        viewModelScope.launch {
            recipesDao?.let {
                val recipe = Recipe(
                    title = title,
                    category = category,
                    ingredient = ingredient,
                    image = image
                )
                it.insertRecipe(recipe)
                readAllRecipes()
            }
        }
    }

    fun editRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipesDao?.let {
                it.updateRecipe(recipe)
                readAllRecipes()
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipesDao?.let {
                it.deleteRecipe(recipe)
                readAllRecipes()
            }
        }
    }
}
