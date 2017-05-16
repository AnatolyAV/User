package ru.andreev_av.user.net;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.andreev_av.user.App;
import ru.andreev_av.user.model.UserModel;

public class UserHttpRequestRetrofit implements IUserHttpRequest {

    private OnActionUserHttpRequestListener listener;

    public UserHttpRequestRetrofit(OnActionUserHttpRequestListener listener) {
        this.listener = listener;
    }

    @Override
    public void getUserList() {
        App.getApi().getUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()) {
                    listener.onGetUserList(response.body());
                } else {
                    try {
                        listener.onError(response.errorBody().string());
                    } catch (IOException e) {
                        listener.onError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    @Override
    public void addUser(UserModel user) {
        App.getApi().addUser(user).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    listener.onAddUser(response.body());
                } else {
                    try {
                        listener.onError(response.errorBody().string());
                    } catch (IOException e) {
                        listener.onError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    @Override
    public void updateUser(UserModel user) {
        App.getApi().updateUser(user.getId(), user).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    listener.onUpdateUser(response.body());
                } else {
                    try {
                        listener.onError(response.errorBody().string());
                    } catch (IOException e) {
                        listener.onError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    public interface OnActionUserHttpRequestListener {
        void onGetUserList(List<UserModel> userList);

        void onAddUser(UserModel user);

        void onUpdateUser(UserModel user);

        void onError(String errorMessage);
    }
}
