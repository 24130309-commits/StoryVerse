
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;

import java.util.List;

@Dao
public interface StoryCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategory(StoryCategoryEntity storyCategory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategories(List<StoryCategoryEntity> storyCategories);

    // Lấy các quan hệ Story - Category
    @Query("SELECT * FROM story_categories WHERE storyId = :storyId")
    List<StoryCategoryEntity> getCategoriesByStoryId(String storyId);

    @Query("SELECT * FROM story_categories WHERE categoryId = :categoryId")
    List<StoryCategoryEntity> getStoriesByCategoryId(String categoryId);

    // Lấy thông tin Category thực tế của một truyện
    @Query(
            "SELECT c.* FROM categories AS c " +
                    "INNER JOIN story_categories AS sc " +
                    "ON c.categoryId = sc.categoryId " +
                    "WHERE sc.storyId = :storyId " +
                    "ORDER BY c.name COLLATE NOCASE ASC"
    )
    List<CategoryEntity> getCategoryEntitiesByStoryId(String storyId);

    @Query(
            "SELECT EXISTS(" +
                    "SELECT 1 FROM story_categories " +
                    "WHERE storyId = :storyId AND categoryId = :categoryId)"
    )
    boolean exists(String storyId, String categoryId);

    @Delete
    void deleteStoryCategory(StoryCategoryEntity storyCategory);

    @Query("DELETE FROM story_categories WHERE storyId = :storyId")
    void deleteCategoriesByStoryId(String storyId);

    @Query("DELETE FROM story_categories WHERE categoryId = :categoryId")
    void deleteStoriesByCategoryId(String categoryId);

    @Query("DELETE FROM story_categories")
    void deleteAllStoryCategories();
}
