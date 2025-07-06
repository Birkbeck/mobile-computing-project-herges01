package co.uk.bbk.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var buttonBreakfast: ImageButton
    private lateinit var buttonBrunch: ImageButton
    private lateinit var buttonLunch: ImageButton
    private lateinit var buttonDinner: ImageButton
    private lateinit var buttonDessert: ImageButton
    private lateinit var buttonOther: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the image button
        buttonBreakfast = findViewById(R.id.imageButton1)
        buttonBrunch = findViewById(R.id.imageButton2)
        buttonLunch = findViewById(R.id.imageButton3)
        buttonDinner = findViewById(R.id.imageButton4)
        buttonDessert = findViewById(R.id.imageButton5)
        buttonOther = findViewById(R.id.imageButton6)


        buttonBreakfast.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }
        buttonBrunch.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }
        buttonLunch.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }
        buttonDinner.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }
        buttonDessert.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }
        buttonOther.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeListPage::class.java)
            startActivity(intent)
        }


    }
}