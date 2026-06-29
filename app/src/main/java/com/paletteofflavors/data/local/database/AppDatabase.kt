package com.paletteofflavors.data.local.database

import com.paletteofflavors.data.local.database.converters.Converters
import com.paletteofflavors.data.local.database.dao.SavedRecipeDao
import com.paletteofflavors.data.local.database.dao.CachedRecipeDao
import com.paletteofflavors.data.local.database.model.CachedRecipeEntity
import com.paletteofflavors.data.local.database.model.SavedRecipeEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [CachedRecipeEntity::class, SavedRecipeEntity::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract  class AppDatabase: RoomDatabase(){
    abstract fun cachedRecipeDao(): CachedRecipeDao
    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object{

        // region <Migrations>
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
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
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN complexity INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase){
                // Удаляем старую таблицу пользовательских рецептов
                db.execSQL("DROP TABLE IF EXISTS resipes")

                // Пересоздаём таблицу savedRecipes с новой структурой
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `savedRecipes_new` (
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
                        `ownerId` INTEGER,
                        `mainCategory` TEXT NOT NULL,
                        `secondaryCategory` TEXT NOT NULL,
                        `isPublic` INTEGER NOT NULL DEFAULT 1,
                        `likedListOfUsers` TEXT NOT NULL DEFAULT '[]',
                        `savedListOfUsers` TEXT NOT NULL DEFAULT '[]'
                    )
                """)

                db.execSQL(
                    "INSERT INTO savedRecipes_new SELECT recipeId, title, ingredients," +
                            " instruction, cookTime, complexity, commentsCount, likesCount, " +
                            "imageUrl, dateTime, ownerId, mainCategory, secondaryCategory, " +
                            "1, '[]', '[]' FROM savedRecipes"
                )
                db.execSQL("DROP TABLE savedRecipes")
                db.execSQL("ALTER TABLE savedRecipes_new RENAME TO savedRecipes")

                // Пересоздаём таблицу cashRecipes с новыми полями
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cachedRecipes_new` (
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
                        `ownerId` INTEGER,
                        `mainCategory` TEXT NOT NULL,
                        `secondaryCategory` TEXT NOT NULL,
                        `isPublic` INTEGER NOT NULL DEFAULT 1,
                        `likedListOfUsers` TEXT NOT NULL DEFAULT '[]',
                        `savedListOfUsers` TEXT NOT NULL DEFAULT '[]'
                    )
                """)

                // Переносим данные из старой cashRecipes
                db.execSQL("INSERT OR IGNORE INTO cachedRecipes_new SELECT * FROM cashRecipes")
                db.execSQL("DROP TABLE IF EXISTS cashRecipes")
                db.execSQL("ALTER TABLE cachedRecipes_new RENAME TO cachedRecipes")
            }
        }
        // endregion

        @Volatile var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipes_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
