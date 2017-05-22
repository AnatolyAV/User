package ru.andreev_av.user.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import ru.andreev_av.user.enums.PaneMode;
import ru.andreev_av.user.fragments.EditUserFragment;
import ru.andreev_av.user.fragments.UserListFragment;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.net.UserHttpRequestRetrofit;
import ru.andreev_av.user.utils.Constants;

public class UserListActivity extends AbstractUserActivity implements UserHttpRequestRetrofit.OnActionUserHttpRequestListener, UserListFragment.onUserItemClickListener {

    private static final String TAG = UserListActivity.class.getName();

    private MenuItem updateItem;
    private ProgressBar progressUpdate;

    private FloatingActionButton fabAddUser;

    private UserListAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();

    // каждый объект хранится отдельно, нужно для быстрого доступа к любому объекту по id (чтобы каждый раз не использовать перебор по всей коллекции userList)
    // предпочтительно при условии что основными действиями будет обновление и добавление данных, а не их получение с сервера (судя по условиям задачи это так)
    // иначе следует убрать и переделать обновление под работу с перебором по всей коллекции userList
    private Map<Integer, UserModel> identityUserMap = new HashMap<>();

    private ProgressDialog progressDialog;

    private UserListFragment userListFragment;
    private boolean userListFragmentAdded;

    private int userPosition = 0;
    private boolean withEditUserFragment= true;

    public UserListActivity() {
        userListFragment = UserListFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        findComponents();

        initToolbar();

        initListeners();

        initAdapter();

        userListFragment.setAdapter(adapter);

        initProgressDialog();

        initDetector();

        initUserHttpRequest();

        initAvatarHttpRequest();

        initImageUtils();

        if (connectionDetector.isNetworkAvailableAndConnected()) {
            if (!progressDialog.isShowing())
                progressDialog.show();
            userHttpRequest.getUserList();
        } else
            Toast.makeText(this, R.string.connection_not_found, Toast.LENGTH_SHORT).show();

        if (savedInstanceState != null)
            userPosition = savedInstanceState.getInt(Constants.USER_POSITION);

        withEditUserFragment = (findViewById(R.id.user_content) != null);
    }

    protected void findComponents() {
        super.findComponents();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressUpdate = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
        fabAddUser = (FloatingActionButton) findViewById(R.id.fab_add_user);
    }

    protected void initListeners() {
        super.initListeners();
        fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserListActivity.this, EditUserActivity.class);
                startActivityForResult(intent, Constants.REQUEST_USER_ADD);
            }
        });
    }

    private void initAdapter() {
        adapter = new UserListAdapter(this, userList);
    }

    protected void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
    }

    protected void initUserHttpRequest() {
        userHttpRequest = new UserHttpRequestRetrofit(this);
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
        if (updateItem != null && progressUpdate != null) {
            if (isUpdate) {
                updateItem.setVisible(false);
                progressUpdate.setVisibility(View.VISIBLE);
            } else {
                progressUpdate.setVisibility(View.GONE);
                updateItem.setVisible(true);
            }
        }
    }

    // сюда возврашается результат после редактирования или добавление элемента
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_USER_EDIT:
                    if (!progressDialog.isShowing())
                        progressDialog.show();
                    userHttpRequest.updateUser((UserModel) data.getParcelableExtra(Constants.USER_OBJECT));
                    break;
                case Constants.REQUEST_USER_ADD:
                    if (!progressDialog.isShowing())
                        progressDialog.show();
                    userHttpRequest.addUser((UserModel) data.getParcelableExtra(Constants.USER_OBJECT));
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
            userListFragment.refreshList(userList);
        }
        Toast.makeText(this, R.string.user_list_updated, Toast.LENGTH_SHORT).show();
        Log.d(TAG, String.valueOf(R.string.user_list_updated));
        setUpdateButtonState(false);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (!userListFragmentAdded)
            initUserListFragment();

        userPosition = 0;
        if (withEditUserFragment && !userList.isEmpty())
            showEditUserFragment(userPosition, userList.get(userPosition));
    }

    private void initUserListFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.users, userListFragment).commit();
        userListFragmentAdded = true;
    }

    @Override
    public void onAddUser(UserModel user) {
        if (user != null) {
            userList.add(user);
            identityUserMap.put(user.getId(), user);
            userListFragment.refreshList(userList);
            Toast.makeText(this, getString(R.string.user_added, user.getFirstName() + " " + user.getLastName()), Toast.LENGTH_SHORT).show();
            Log.d(TAG, getString(R.string.user_added, user.getFirstName() + " " + user.getLastName()));
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onUpdateUser(UserModel user) {
        if (user != null) {
            UserModel oldUser = identityUserMap.remove(user.getId());
            userList.remove(oldUser);
            identityUserMap.put(user.getId(), user);
            userList.add(user);
            userListFragment.refreshList(userList);
            Toast.makeText(this, getString(R.string.user_updated, user.getFirstName() + " " + user.getLastName()), Toast.LENGTH_SHORT).show();
            Log.d(TAG, getString(R.string.user_updated, user.getFirstName() + " " + user.getLastName()));
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, R.string.error_performing_operation, Toast.LENGTH_SHORT).show();
        Log.e(TAG, errorMessage);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        setUpdateButtonState(false);
    }

    @Override
    public void userItemClick(int position, UserModel user) {
        this.userPosition = position;
        currentUser = user;
        showEditUserFragment(position, user);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.USER_POSITION, userPosition);
    }

    void showEditUserFragment(int pos, UserModel user) {
        if (!userList.isEmpty())
            if (withEditUserFragment) {
                editUserFragment = (EditUserFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.user_content);
                if (editUserFragment == null || editUserFragment.getUserPosition() != pos) {
                    editUserFragment = EditUserFragment.newInstance(pos, user);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.user_content, editUserFragment).commit();
                    paneMode = PaneMode.TwoPaneMode;
                }
            } else {
                openEditUserActivityOnClick(pos, user);
            }
    }

    private void openEditUserActivityOnClick(int pos, final UserModel user) {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra(Constants.USER_POSITION, pos);
        intent.putExtra(Constants.USER_OBJECT, user);
        startActivityForResult(intent, Constants.REQUEST_USER_EDIT);
    }
}