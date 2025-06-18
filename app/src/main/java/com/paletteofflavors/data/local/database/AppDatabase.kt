package com.paletteofflavors.data.local.database

import com.paletteofflavors.data.local.database.dao.CashDao
import com.paletteofflavors.data.local.database.converters.Converters
import com.paletteofflavors.data.local.database.dao.RecipeDao
import com.paletteofflavors.data.local.database.dao.SavedRecipeDao
import com.paletteofflavors.data.local.database.model.NetworkRecipe
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.paletteofflavors.data.local.database.model.Recipe
import com.paletteofflavors.data.local.database.model.SavedRecipe

// Singleton-паттерн для жизненного цикла БД

@Database(entities = [Recipe::class, SavedRecipe::class, NetworkRecipe::class], version = 3)
@TypeConverters(Converters::class)
abstract  class AppDatabase: RoomDatabase(){
    abstract fun recipeDao(): RecipeDao
    abstract fun savedRecipeDao(): SavedRecipeDao
    abstract fun cashDao(): CashDao

    companion object{

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE recipes ADD COLUMN complexity INTEGER")
            }
        }


        @Volatile var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipes_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Для перехода на новую версию бд
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}