
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;

import java.util.List;

@Dao
public interface StoryCategoryDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategory(StoryCategoryEntity relation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategories(List<StoryCategoryEntity> relations);

    // GET RELATIONS
    @Query("SELECT * FROM story_categories WHERE storyId = :storyId")
    List<StoryCategoryEntity> getRelationsByStoryId(String storyId);

    @Query("SELECT * FROM story_categories WHERE categoryId = :categoryId")
    List<StoryCategoryEntity> getRelationsByCategoryId(String categoryId);

    // GET CATEGORIES
    @Query(
            "SELECT categories.* FROM categories " +
                    "INNER JOIN story_categories " +
                    "ON categories.categoryId = story_categories.categoryId " +
                    "WHERE story_categories.storyId = :storyId " +
                    "ORDER BY categories.name ASC"
    )
    List<CategoryEntity> getCategoriesByStoryId(String storyId);

    @Query(
            "SELECT categories.* FROM categories " +
                    "INNER JOIN story_categories " +
                    "ON categories.categoryId = story_categories.categoryId " +
                    "WHERE story_categories.storyId = :storyId " +
                    "AND categories.categoryId = :categoryId LIMIT 1"
    )
    CategoryEntity getStoryCategory(
            String storyId,
            String categoryId
    );

    // DELETE RELATIONS
    @Query("DELETE FROM story_categories WHERE storyId = :storyId")
    void deleteByStoryId(String storyId);

    @Query("DELETE FROM story_categories WHERE storyId = :storyId")
    void deleteCategoriesByStoryId(String storyId);

    @Query("DELETE FROM story_categories WHERE categoryId = :categoryId")
    void deleteByCategoryId(String categoryId);

    @Query(
            "DELETE FROM story_categories " +
                    "WHERE storyId = :storyId AND categoryId = :categoryId"
    )
    void deleteRelation(String storyId, String categoryId);

    // COUNT
    @Query("SELECT COUNT(*) FROM story_categories WHERE storyId = :storyId")
    int getCategoryCountByStoryId(String storyId);

    // REPLACE RELATIONS
    @Transaction
    default void replaceStoryCategories(
            String storyId,
            List<StoryCategoryEntity> relations
    ) {
        deleteByStoryId(storyId);

        if (relations != null && !relations.isEmpty()) {
            insertStoryCategories(relations);
        }
    }

    // DELETE ALL
    @Query("DELETE FROM story_categories")
    void deleteAllRelations();
}
