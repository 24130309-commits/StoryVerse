package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    List<UserEntity> getAllUsers();

    @Delete
    void deleteUser(UserEntity user);

    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUserById(String userId);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}