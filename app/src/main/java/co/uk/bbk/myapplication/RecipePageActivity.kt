package co.uk.bbk.myapplication

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RecipePageActivity : AppCompatActivity() {

    private lateinit var popUpButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        popUpButton = findViewById(R.id.menuButton)
        popUpButton.setOnClickListener {
            val popupMenu = PopupMenu(this@RecipePageActivity, popUpButton)
            popupMenu.menuInflater.inflate(R.menu.recipe_settings, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                Toast.makeText(
                    this@RecipePageActivity,
                    "You Clicked " + menuItem.title,
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            popupMenu.show()
        }
    }
}



