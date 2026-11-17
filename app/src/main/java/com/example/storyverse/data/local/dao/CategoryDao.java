
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategoryIfNotExists(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategoriesIfNotExists(List<CategoryEntity> categories);

    @Query("UPDATE categories SET name = CASE WHEN :name IS NOT NULL AND :name != '' THEN :name ELSE name END, description = CASE WHEN :description IS NOT NULL AND :description != '' THEN :description ELSE description END, iconUrl = CASE WHEN :iconUrl IS NOT NULL AND :iconUrl != '' THEN :iconUrl ELSE iconUrl END WHERE categoryId = :categoryId")
    void updateCategoryIfNotEmpty(
            String categoryId,
            String name,
            String description,
            String iconUrl
    );

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategories();

    @Delete
    void deleteCategory(CategoryEntity category);

    @Query("DELETE FROM categories WHERE categoryId = :categoryId")
    void deleteCategoryById(String categoryId);

    @Query("DELETE FROM categories")
    void deleteAllCategories();
}
