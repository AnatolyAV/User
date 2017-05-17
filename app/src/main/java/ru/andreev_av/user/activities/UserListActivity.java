package ru.andreev_av.user.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.andreev_av.user.R;
import ru.andreev_av.user.adapter.UserListAdapter;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.net.ConnectionDetector;
import ru.andreev_av.user.net.IUserHttpRequest;
import ru.andreev_av.user.net.UserHttpRequestRetrofit;
import ru.andreev_av.user.utils.Constants;

public class UserListActivity extends AppCompatActivity implements UserHttpRequestRetrofit.OnActionUserHttpRequestListener {

    private Toolbar toolbar;
    private MenuItem updateItem;
    private ProgressBar progressUpdate;
    private FloatingActionButton fabAddUser;

    private UserListAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();
    private RecyclerView rvUserList;

    // каждый объект хранится отдельно, нужно для быстрого доступа к любому объекту по id (чтобы каждый раз не использовать перебор по всей коллекции userList)
    // предпочтительно при условии что основными действиями будет обновление и добавление данных, а не их получение с сервера (судя по условиям задачи это так)
    // иначе следует убрать и переделать обновление под работу с перебором по всей коллекции userList
    private Map<Integer, UserModel> identityUserMap = new HashMap<>();

    private IUserHttpRequest userHttpRequest;

    private ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        findComponents();

        initToolbar();

        initListeners();

        initAdapter();

        initComponents();

        userHttpRequest = new UserHttpRequestRetrofit(this);

        connectionDetector = new ConnectionDetector(this);

        if (connectionDetector.isNetworkAvailableAndConnected())
            userHttpRequest.getUserList();
        else
            Toast.makeText(this, R.string.connection_not_found, Toast.LENGTH_SHORT).show();

    }

    private void findComponents() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressUpdate = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
        fabAddUser = (FloatingActionButton) findViewById(R.id.fab_add_user);
        rvUserList = (RecyclerView) findViewById(R.id.rv_user_list);
    }

    private void initListeners() {
        fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserListActivity.this, EditUserActivity.class);
                startActivityForResult(intent, Constants.REQUEST_USER_ADD);
            }
        });
    }

    protected void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initAdapter() {
        adapter = new UserListAdapter(this, userList);
    }

    private void initComponents() {
        rvUserList.setAdapter(adapter);
        rvUserList.setHasFixedSize(true);
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        updateItem = menu.findItem(R.id.action_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                if (connectionDetector.isNetworkAvailableAndConnected()) {
                    userHttpRequest.getUserList();
                    setUpdateButtonState(true);
                } else {
                    Toast.makeText(this, R.string.connection_not_found, Toast.LENGTH_SHORT).show();
                    setUpdateButtonState(false);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setUpdateButtonState(boolean isUpdate) {
        if (isUpdate) {
            updateItem.setVisible(false);
            progressUpdate.setVisibility(View.VISIBLE);
        } else {
            progressUpdate.setVisibility(View.GONE);
            updateItem.setVisible(true);
        }
    }

    // сюда возврашается результат после редактирования или добавление элемента
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_USER_EDIT:
                    userHttpRequest.updateUser((UserModel)data.getParcelableExtra(Constants.USER_OBJECT));
                    break;
                case Constants.REQUEST_USER_ADD:
                    userHttpRequest.addUser((UserModel)data.getParcelableExtra(Constants.USER_OBJECT));
                    break;
            }
        }
    }

    @Override
    public void onGetUserList(List<UserModel> userList) {
        if (!userList.isEmpty()) {
            this.userList = userList;
            identityUserMap.clear();
            for (UserModel user : userList)
                identityUserMap.put(user.getId(), user);
            adapter.refreshList(userList);
            adapter.notifyDataSetChanged();
        }
        setUpdateButtonState(false);
    }

    @Override
    public void onAddUser(UserModel user) {
        if (user!=null){
            userList.add(user);
            identityUserMap.put(user.getId(),user);
            adapter.refreshList(userList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onUpdateUser(UserModel user) {
        if (user!=null){
            UserModel oldUser = identityUserMap.remove(user.getId());
            userList.remove(oldUser);
            identityUserMap.put(user.getId(),user);
            userList.add(user);
            adapter.refreshList(userList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(String errorMessage) {
    }
}