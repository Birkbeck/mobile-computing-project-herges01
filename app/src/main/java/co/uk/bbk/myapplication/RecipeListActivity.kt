package co.uk.bbk.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.ArrayList

class RecipeListActivity : AppCompatActivity() {

    private lateinit var list : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        list = findViewById(R.id.list_item)
        val listItems = arrayOf("Recipe 1", "Recipe 2")

        val listAdapter = ArrayAdapter(this, R.layout.list_item, R.id.item_text,listItems)
        list.adapter = listAdapter

    }
}