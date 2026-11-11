package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    // =========================================
    // INSERT - THÊM THỂ LOẠI
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategoryIfNotExists(CategoryEntity category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categories);

    // Thêm danh sách nhưng không ghi đè thể loại cũ
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategoriesIfNotExists(
            List<CategoryEntity> categories
    );

    // =========================================
    // UPDATE - CẬP NHẬT THỂ LOẠI
    // =========================================

    @Update
    void updateCategory(CategoryEntity category);

    // Chỉ cập nhật trường có dữ liệu hợp lệ
    @Query(
            "UPDATE categories SET " +
                    "name = CASE " +
                    "WHEN :name IS NOT NULL AND TRIM(:name) != '' " +
                    "THEN :name ELSE name END, " +
                    "description = CASE " +
                    "WHEN :description IS NOT NULL AND TRIM(:description) != '' " +
                    "THEN :description ELSE description END, " +
                    "iconUrl = CASE " +
                    "WHEN :iconUrl IS NOT NULL AND TRIM(:iconUrl) != '' " +
                    "THEN :iconUrl ELSE iconUrl END " +
                    "WHERE categoryId = :categoryId"
    )
    void updateCategoryIfNotEmpty(
            String categoryId,
            String name,
            String description,
            String iconUrl
    );

    // =========================================
    // DELETE - XÓA THỂ LOẠI
    // =========================================

    @Delete
    void deleteCategory(CategoryEntity category);

    @Query(
            "DELETE FROM categories " +
                    "WHERE categoryId = :categoryId"
    )
    void deleteCategoryById(String categoryId);

    @Query("DELETE FROM categories")
    void deleteAllCategories();

    // =========================================
    // SELECT - LẤY THỂ LOẠI
    // =========================================

    @Query(
            "SELECT * FROM categories " +
                    "WHERE categoryId = :categoryId LIMIT 1"
    )
    CategoryEntity getCategoryById(String categoryId);

    @Query(
            "SELECT * FROM categories " +
                    "ORDER BY name COLLATE NOCASE ASC"
    )
    List<CategoryEntity> getAllCategories();

    // =========================================
    // SEARCH - TÌM KIẾM THỂ LOẠI
    // =========================================

    @Query(
            "SELECT * FROM categories " +
                    "WHERE name LIKE '%' || :keyword || '%' " +
                    "ORDER BY name ASC"
    )
    List<CategoryEntity> searchCategories(String keyword);

    // =========================================
    // COUNT - ĐẾM THỂ LOẠI
    // =========================================

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}