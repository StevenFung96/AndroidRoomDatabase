package com.example.hexa_stevenfung.androidroomdatabase.Local;

import android.arch.persistence.room.*;
import com.example.hexa_stevenfung.androidroomdatabase.Model.User;
import io.reactivex.Flowable;

import java.util.List;

@Dao
public interface UserDAO {

    @Query("SELECT * FROM users WHERE id=:userId")
    Flowable<User> getUserById(int userId);

    @Query("SELECT * FROM users")
    Flowable<List<User>> getAllUsers();

    @Insert
    void insertUser(User... users);

    @Update
    void updateUser(User... users);

    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}