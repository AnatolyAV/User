package ru.andreev_av.user.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.andreev_av.user.model.UserModel;

public interface UserApi {

    String BASE_URL = "https://bb-test-server.herokuapp.com";

    @GET("/users.json")
    Call<List<UserModel>> getUsers();

    @POST("users.json")
    Call<UserModel> addUser(@Body UserModel user);

    @PATCH("users/{id}.json")
    Call<UserModel> updateUser(@Path("id") int userId, @Body UserModel user);
}
