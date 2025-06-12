package DataSource.Local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import domain.Recipe
import domain.SavedRecipe

// Singleton-паттерн для жизненного цикла БД

@Database(entities = [Recipe::class, SavedRecipe::class], version = 2)
@TypeConverters(Converters::class)
abstract  class AppDatabase: RoomDatabase(){
    abstract fun recipeDao(): RecipeDao
    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object{

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаем новую таблицу SavedRecipe
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `savedRecipes` (
                `recipeId` INTEGER NOT NULL PRIMARY KEY,
                `title` TEXT NOT NULL,
                `ingredients` TEXT NOT NULL,
                `instruction` TEXT NOT NULL,
                `cookTime` INTEGER NOT NULL,
                `complexity` INTEGER NOT NULL,
                `commentsCount` INTEGER NOT NULL,
                `likesCount` INTEGER NOT NULL,
                `imageUrl` TEXT,
                `dateTime` TEXT NOT NULL,
                `ownerId` INTEGER
            )
        """)
            }
        }


        @Volatile var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipes_db"
                )
                    .addMigrations(MIGRATION_1_2) // Для перехода на новую версию бд
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}