# Актуальная структура серверной базы данных Turso

### Table "users"
Columns:
```
id - INTEGER PRIMARY KEY AUTOINCREMENT
username - TEXT UNIQUE NOT NULL
password - TEXT NOT NULL
fullname - TEXT
email - TEXT
phone_number -TEXT
created_at - NUMERIC
```

### Table "Recipes"
```
recipe_id - INTEGER PRIMARY KEY AUTOINCREMENT
title -TEXT NOT NULL
instructions - TEXT NOT NULL
cookTime -INTEGER NOT NULL
complexity - INTEGER
comments_count - INTEGER DEFAULT 0
likes_count - INTEGER DEFAULT 0
image_url - TEXT
publish_dateTime - NUMERIC DEFAULTCURRENT_TIMESTAMP
owner_id - INTEGER
main_category - TEXT
secondary_category - TEXT
isPublic - INTEGER NOT NULL DEFAULT0
liked_list - TEXT
saved_list - TEXT
```
- Constraints:
    ```
    CONSTRAINT Recipes_check_1 CHECK (complexity BETWEEN 1 AND 5)
    CONSTRAINT Recipes_check_2 CHECK (isPublic IN (0, 1))  
    CONSTRAINT fk_Recipes_owner_id_users_id_fk FOREIGN KEY (owner_id) REFERENCES users(id)
    ```

### Table "IngredientDictionary"
```
ingredient_id INTEGER PRIMARY KEY AUTOINCREMENT
name TEXT UNIQUE NOT NULL
```

### Table "RecipeIngredients"
```
recipe_id INTEGER
ingredient_id INTEGER
```
- Constraints:
    ```
    CONSTRAINT fk_RecipeIngredients_ingredient_id_IngredientDictionary_ingredient_id_fk FOREIGN KEY(ingredient_id) REFERENCES IngredientDictionary(ingredient_id)
    CONSTRAINT fk_RecipeIngredients_recipe_id_Recipes_recipe_id_fk FOREIGN KEY(recipe_id) REFERENCES Recipes(recipe_id) ON DELETECASCADE
    CONSTRAINT RecipeIngredients_pk PRIMARY KEY (recipe_id, ingredient_id)
    ```