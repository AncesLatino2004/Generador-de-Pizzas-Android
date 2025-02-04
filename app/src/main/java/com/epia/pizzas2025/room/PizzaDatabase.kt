
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.epia.pizzas2025.room.Pizza
import com.epia.pizzas2025.room.PizzaDao

@Database(entities = [Pizza::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pizzaDao(): PizzaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pizza_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
