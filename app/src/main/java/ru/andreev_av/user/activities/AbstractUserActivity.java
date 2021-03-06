package ru.andreev_av.user.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.andreev_av.user.R;
import ru.andreev_av.user.api.AmazonawsApi;
import ru.andreev_av.user.enums.PaneMode;
import ru.andreev_av.user.fragments.EditUserFragment;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.net.AvatarHttpRequestAmazonaws;
import ru.andreev_av.user.net.ConnectionDetector;
import ru.andreev_av.user.net.IAvatarHttpRequest;
import ru.andreev_av.user.net.IUserHttpRequest;
import ru.andreev_av.user.utils.Constants;
import ru.andreev_av.user.utils.EmailValidator;
import ru.andreev_av.user.utils.ImageUtils;

import static ru.andreev_av.user.utils.Constants.AVATAR_FILE_NAME;

public abstract class AbstractUserActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    protected EditText etUserFirstName;
    protected EditText etUserLastName;
    protected EditText etUserEmail;
    protected CircleImageView imgAvatar;

    protected ImageView imgSave;

    protected EditUserFragment editUserFragment;

    protected UserModel currentUser;

    protected String avatarFileName;
    protected Bitmap selectedAvatarBitmap = null;

    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    protected IUserHttpRequest userHttpRequest;
    protected IAvatarHttpRequest avatarHttpRequest;

    protected ConnectionDetector connectionDetector;
    protected ImageUtils imageUtils;

    protected PaneMode paneMode;

    private EmailValidator emailValidator = new EmailValidator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void findComponents() {
        imgSave = (ImageView) findViewById(R.id.img_user_save);
    }

    protected void initToolbar() {
        setSupportActionBar(toolbar);
    }

    protected void findComponentsEditTextFromFragment() {
        if (editUserFragment != null && editUserFragment.getView() != null) {
            etUserFirstName = (EditText) editUserFragment.getView().findViewById(R.id.et_user_first_name);
            etUserLastName = (EditText) editUserFragment.getView().findViewById(R.id.et_user_last_name);
            etUserEmail = (EditText) editUserFragment.getView().findViewById(R.id.et_user_email);
        }
    }

    protected void findComponentsAvatarViewFromFragment() {
        if (editUserFragment != null && editUserFragment.getView() != null) {
            imgAvatar = (CircleImageView) editUserFragment.getView().findViewById(R.id.img_avatar);
        }
    }


    protected void initListeners() {
        if (imgSave != null)
            imgSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    findComponentsEditTextFromFragment();

                    String newUserFirstName = etUserFirstName.getText().toString();
                    String newUserLastName = etUserLastName.getText().toString();
                    String newUserEmail = etUserEmail.getText().toString();

                    if (!validate(newUserFirstName, newUserLastName, newUserEmail)) {
                        return;
                    }

                    if (currentUser == null) {
                        currentUser = new UserModel();
                        currentUser.setId(-1);
                        if (selectedAvatarBitmap != null) {
                            String s3url = AmazonawsApi.URL_WITH_BUCKET + avatarFileName + ".png";
                            currentUser.setAvatarUrl(s3url);
                        }
                    }

                    // чтобы лишний раз не сохранять - проверяем, были ли изменены данные
                    if (currentUser.getId() == -1 || edited(currentUser, newUserFirstName, newUserLastName, newUserEmail)) {

                        currentUser.setFirstName(newUserFirstName);
                        currentUser.setLastName(newUserLastName);
                        currentUser.setEmail(newUserEmail);

                        if (connectionDetector.isNetworkAvailableAndConnected()) {
                            if (currentUser.getId() == -1 && currentUser.getAvatarUrl() != null)
                                avatarHttpRequest.addUserAvatar(avatarFileName);
                            Intent intent = new Intent();
                            intent.putExtra(Constants.USER_OBJECT, currentUser);
                            setResult(RESULT_OK, intent);
                            if (paneMode == PaneMode.OnePaneMode)
                                ActivityCompat.finishAfterTransition(AbstractUserActivity.this);
                            else {
                                if (currentUser.getId() == -1)
                                    userHttpRequest.addUser(currentUser);
                                else
                                    userHttpRequest.updateUser(currentUser);
                            }
                        } else
                            Toast.makeText(AbstractUserActivity.this, R.string.connection_not_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    protected void initImageAvatar() {
        if (avatarFileName != null) {
            selectedAvatarBitmap = imageUtils.loadBitmap(avatarFileName);
            if (selectedAvatarBitmap != null) {
                imgAvatar.setImageBitmap(selectedAvatarBitmap);
            }
        }
    }

    protected void initDetector() {
        connectionDetector = new ConnectionDetector(this);
    }

    protected void initAvatarHttpRequest() {
        avatarHttpRequest = new AvatarHttpRequestAmazonaws(this);
    }

    protected void initImageUtils() {
        imageUtils = new ImageUtils(this);
    }

    // для проверки, изменились ли данные пользователя
    public boolean edited(UserModel user, String newUserFirstName, String newUserLastName, String newUserEmail) {
        // описываем условия, при которых будет считаться, что произошло редактирование - чтобы сохранять только при изменении данных
        // если хотя бы одно из условий равно true - значит запись была отредактирована
        return (user.getFirstName() == null && newUserFirstName != null) ||
                (user.getLastName() == null && newUserLastName != null) ||
                (user.getEmail() == null && newUserEmail != null) ||
                (user.getFirstName() != null && newUserFirstName != null && !user.getFirstName().equals(newUserFirstName)) ||
                (user.getLastName() != null && newUserLastName != null && !user.getLastName().equals(newUserLastName)) ||
                (user.getEmail() != null && newUserEmail != null && !user.getEmail().equals(newUserEmail));
    }
    public boolean validate(String newUserFirstName, String newUserLastName, String newUserEmail) {

        if (newUserFirstName.trim().length() == 0) {
            Toast.makeText(this, R.string.enter_first_name, Toast.LENGTH_SHORT).show();
            etUserFirstName.requestFocus();
            return false;
        }
        if (newUserLastName.trim().length() == 0) {
            Toast.makeText(this, R.string.enter_last_name, Toast.LENGTH_SHORT).show();
            etUserLastName.requestFocus();
            return false;
        }
        if (newUserEmail.trim().length() == 0) {
            Toast.makeText(this, R.string.enter_email, Toast.LENGTH_SHORT).show();
            etUserEmail.requestFocus();
            return false;
        }
        if (!emailValidator.validate(newUserEmail)) {
            Toast.makeText(this, R.string.enter_correct_email, Toast.LENGTH_SHORT).show();
            etUserEmail.requestFocus();
            return false;
        }
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AVATAR_FILE_NAME, avatarFileName);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        avatarFileName = savedInstanceState.getString(AVATAR_FILE_NAME);
    }
}
