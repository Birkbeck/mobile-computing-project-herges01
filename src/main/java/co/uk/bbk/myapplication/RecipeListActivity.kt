package co.uk.bbk.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.uk.bbk.myapplication.MainActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class RecipeListActivity : AppCompatActivity() {

    private lateinit var list: ListView
    private lateinit var fabButton : ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fabButton = findViewById(R.id.floatingActionButton)
        list = findViewById(R.id.list_item)
        val listItems = arrayOf("Recipe 1", "Recipe 2")

        val listAdapter = ArrayAdapter(this, R.layout.item_list, R.id.item_text, listItems)
        list.adapter = listAdapter

        list.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@RecipeListActivity, RecipePageActivity::class.java)
            startActivity(intent)
        }

        fabButton.setOnClickListener {
            val intent = Intent(this@RecipeListActivity, RecipeAdditionPage::class.java)
            startActivity(intent)
        }
    }
}

