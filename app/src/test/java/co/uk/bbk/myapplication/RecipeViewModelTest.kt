import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.uk.bbk.myapplication.Recipe
import co.uk.bbk.myapplication.RecipeViewModel
import co.uk.bbk.myapplication.RecipesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RecipeViewModel
    private lateinit var fakeDao: RecipesDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = mock(RecipesDao::class.java)
        viewModel = RecipeViewModel()
        viewModel.recipesDao = fakeDao
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testEditRecipe_callsDaoUpdate() = runTest {
        val recipe = Recipe(1, "Title", "Category", "Ingredients", null)
        viewModel.editRecipe(recipe)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(fakeDao).updateRecipe(recipe)
    }

    @Test
    fun testDeleteRecipe_callsDaoDelete() = runTest {
        val recipe = Recipe(2, "Title2", "Category2", "Ingredients2", null)
        viewModel.deleteRecipe(recipe)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(fakeDao).deleteRecipe(recipe)
    }
}