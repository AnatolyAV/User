package ru.andreev_av.user.net;

import ru.andreev_av.user.model.UserModel;

public interface IUserHttpRequest {

    void getUserList();

    void addUser(UserModel user);

    void updateUser(UserModel user);
}
