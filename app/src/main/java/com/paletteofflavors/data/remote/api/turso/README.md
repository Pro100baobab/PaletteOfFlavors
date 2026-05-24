# Актуальная структура серверной базы данных Turso

### Table "users"
Columns:
```
id - INTEGER PRIMARY KEY AUTOINCREMENT
username - TEXT UNIQUE NOT NULL
password - TEXT NOT NULL
fullname - TEXT
email - TEXT
phone_number - TEXT
created_at - NUMERIC DEFAULT CURRENT_TIMESTAMP
```

### Table "Recipes"
Columns:
```
recipe_id - INTEGER PRIMARY KEY AUTOINCREMENT
title - TEXT NOT NULL
instructions - TEXT NOT NULL
cookTime - INTEGER NOT NULL
complexity - INTEGER
image_url - TEXT
publish_dateTime - NUMERIC DEFAULT CURRENT_TIMESTAMP
owner_id - INTEGER
main_category - TEXT
secondary_category - TEXT
isPublic - INTEGER NOT NULL DEFAULT 1
liked_list - TEXT  -- JSON-массив id пользователей, например "[1,2,3]"
saved_list - TEXT  -- JSON-массив id пользователей
```
- Constraints:
    ```
    CONSTRAINT Recipes_check_1 CHECK (complexity BETWEEN 1 AND 5)
    CONSTRAINT Recipes_check_2 CHECK (isPublic IN (0, 1))  
    CONSTRAINT fk_Recipes_owner_id_users_id_fk FOREIGN KEY (owner_id) REFERENCES users(id)
    ```

### Table "Comments"
Columns:
```
comment_id - INTEGER PRIMARY KEY AUTOINCREMENT
recipe_id - INTEGER
user_id - INTEGER
text - TEXT NOT NULL
created_at - NUMERIC DEFAULT CURRENT_TIMESTAMP
```
- Constraints:
    ```
    CONSTRAINT fk_Comments_recipe_id FOREIGN KEY(recipe_id) REFERENCES Recipes(recipe_id) ON DELETE CASCADE
    CONSTRAINT fk_Comments_user_id FOREIGN KEY(user_id) REFERENCES users(id)
    ```

### Table "Followers"
Columns:
```
follower_id - INTEGER
followed_id - INTEGER
```
- Constraints:
    ```
    CONSTRAINT Followers_pk PRIMARY KEY (follower_id, followed_id)
    CONSTRAINT fk_Followers_follower_id FOREIGN KEY(follower_id) REFERENCES users(id)
    CONSTRAINT fk_Followers_followed_id FOREIGN KEY(followed_id) REFERENCES users(id)
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
    CONSTRAINT fk_RecipeIngredients_recipe_id_Recipes_recipe_id_fk FOREIGN KEY(recipe_id) REFERENCES Recipes(recipe_id) ON DELETE CASCADE
    CONSTRAINT RecipeIngredients_pk PRIMARY KEY (recipe_id, ingredient_id)
    ```
