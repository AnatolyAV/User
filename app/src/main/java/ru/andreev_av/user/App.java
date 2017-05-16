package ru.andreev_av.user;

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.andreev_av.user.api.UserApi;

public class App extends Application {

    private static UserApi userApi;
    private Retrofit retrofit;

    public static UserApi getApi() {
        return userApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        retrofit = new Retrofit.Builder()
                .baseUrl(UserApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userApi = retrofit.create(UserApi.class);
    }
}
