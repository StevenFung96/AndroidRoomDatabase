package com.example.hexa_stevenfung.androidroomdatabase;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.hexa_stevenfung.androidroomdatabase.Database.UserRepository;
import com.example.hexa_stevenfung.androidroomdatabase.Local.UserDataSource;
import com.example.hexa_stevenfung.androidroomdatabase.Local.UserDatabase;
import com.example.hexa_stevenfung.androidroomdatabase.Model.User;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.lstUsers)
    ListView lstUser;

    //Adapter
    List<User> userList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init
        compositeDisposable = new CompositeDisposable();

        //Init View
        ButterKnife.bind(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        registerForContextMenu(lstUser);
        lstUser.setAdapter(adapter);

        //Database
        UserDatabase userDatabase = UserDatabase.getInstance(this);//Create database
        userRepository = UserRepository.getInstance(UserDataSource.getInstance(userDatabase.userDAO()));

        //Load all data from Database
        loadData();

    }

    //Add data into Database
    @OnClick(R.id.fab)
    public void onClickAddData() {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                User user = new User("Stan Lee", UUID.randomUUID().toString() + "@live.com");
                userRepository.insertUser(user);
                emitter.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                               @Override
                               public void accept(Object o) throws Exception {
                                   Toast.makeText(MainActivity.this, "User has been successfully added!", Toast.LENGTH_SHORT).show();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(MainActivity.this, "123" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                );
        compositeDisposable.add(disposable);
    }

    private void loadData() {
        //Use RxJava
        Disposable disposable = userRepository.getAllUsers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<User>>() {
                               @Override
                               public void accept(List<User> users) throws Exception {
                                   onGetAllUserSuccess(users);
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                );
        compositeDisposable.add(disposable);
    }

    private void onGetAllUserSuccess(List<User> users) {
        userList.clear();
        userList.addAll(users);
        adapter.notifyDataSetChanged();
    }

    //Menu Items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                deleteAllUsers();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllUsers() {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                userRepository.deleteAllUsers();
                emitter.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                               @Override
                               public void accept(Object o) throws Exception {
                                   Toast.makeText(MainActivity.this, "All users have been successfully removed!", Toast.LENGTH_SHORT).show();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(MainActivity.this, "123" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                );
        compositeDisposable.add(disposable);
    }

    //Item List's Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle("Select action:");

        menu.add(Menu.NONE, 0, Menu.NONE, "UPDATE");
        menu.add(Menu.NONE, 1, Menu.NONE, "DELETE");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final User user = userList.get(info.position);
        switch (item.getItemId()) {
            case 0: // update
            {
                final EditText edtName = new EditText(MainActivity.this);
                edtName.setText(user.getName());
                edtName.setHint("Enter your name");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Edit")
                        .setMessage("Edit user name")
                        .setView(edtName)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (TextUtils.isEmpty(edtName.getText().toString()))
                                    return;
                                else {
                                    user.setName(edtName.getText().toString());
                                    updateUser(user);
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
            break;

            case 1: //Delete
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you want to delete" + user.toString())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteUser(user);
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
            break;

            default:
                break;
        }
        return true;
    }

    private void deleteUser(final User user) {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                userRepository.deleteUser(user);
                emitter.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                               @Override
                               public void accept(Object o) throws Exception {
                                   Toast.makeText(MainActivity.this, "All users have been successfully removed!", Toast.LENGTH_SHORT).show();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(MainActivity.this, "123" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                );
        compositeDisposable.add(disposable);
    }

    private void updateUser(final User user) {

        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                userRepository.updateUser(user);
                emitter.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                               @Override
                               public void accept(Object o) throws Exception {
                                   Toast.makeText(MainActivity.this, "All users have been successfully removed!", Toast.LENGTH_SHORT).show();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(MainActivity.this, "123" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           }
                );
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}