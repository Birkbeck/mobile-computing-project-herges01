package co.uk.bbk.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.uk.bbk.myapplication.databinding.RecipeItemBinding
import android.widget.PopupMenu
import android.content.Intent

class RecipeAdapter(
    private var recipes: List<Recipe> = listOf(),
    private val onEdit: (Recipe) -> Unit,
    private val onDelete: (Recipe) -> Unit,
    private var onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    inner class RecipeViewHolder(private val binding: RecipeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipe = recipe
            binding.executePendingBindings()

            // allows items to be clickable
            binding.root.setOnClickListener {
                onClick(recipe)
            }

            //for the menu button
            binding.menuButton.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.recipe_settings, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.editRecipe -> {
                            onEdit(recipe)
                            true
                        }
                        R.id.deleteRecipe -> {
                            onDelete(recipe)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
        }
    }
