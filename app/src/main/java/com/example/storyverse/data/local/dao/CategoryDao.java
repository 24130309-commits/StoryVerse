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

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategories();

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId LIMIT 1")
    CategoryEntity getCategoryById(String categoryId);

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<CategoryEntity> searchCategories(String keyword);

    @Delete
    void deleteCategory(CategoryEntity category);

    @Query("DELETE FROM categories WHERE categoryId = :categoryId")
    void deleteCategoryById(String categoryId);

    @Query("DELETE FROM categories")
    void deleteAllCategories();
}