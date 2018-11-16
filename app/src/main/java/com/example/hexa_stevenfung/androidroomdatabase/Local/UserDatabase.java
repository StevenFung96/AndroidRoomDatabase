package com.example.hexa_stevenfung.androidroomdatabase.Local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import com.example.hexa_stevenfung.androidroomdatabase.Model.User;

import static com.example.hexa_stevenfung.androidroomdatabase.Local.UserDatabase.DATABASE_VERSION;

@Database(entities = {User.class}, version = DATABASE_VERSION)
public abstract class UserDatabase extends RoomDatabase {
    static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Test-Database-Room";

    public abstract UserDAO userDAO();

    private static UserDatabase mInstance;

    public static UserDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME)

                    .build();
        }
        return mInstance;
    }
}
