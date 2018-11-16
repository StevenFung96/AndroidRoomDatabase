package com.example.hexa_stevenfung.androidroomdatabase.Database;

import com.example.hexa_stevenfung.androidroomdatabase.Model.User;
import io.reactivex.Flowable;

import java.util.List;

public interface IUserDataSource {

    Flowable<User> getUserById(int userId);

    Flowable<List<User>> getAllUsers();

    void insertUser(User... users);

    void updateUser(User... users);

    void deleteUser(User user);

    void deleteAllUsers();
}
