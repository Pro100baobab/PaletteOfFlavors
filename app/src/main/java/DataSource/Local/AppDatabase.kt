package DataSource.Local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import domain.Recipe
import domain.savedRecipe

// Singleton-паттерн для жизненного цикла БД

@Database(entities = [Recipe::class, savedRecipe::class], version = 1)
@TypeConverters(Converters::class)
abstract  class AppDatabase: RoomDatabase(){
    abstract fun recipeDao(): RecipeDao

    companion object{
        @Volatile var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipes_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}