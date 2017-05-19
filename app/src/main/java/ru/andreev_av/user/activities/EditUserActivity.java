package ru.andreev_av.user.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.andreev_av.user.R;
import ru.andreev_av.user.api.AmazonawsApi;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.net.AvatarHttpRequestAmazonaws;
import ru.andreev_av.user.net.ConnectionDetector;
import ru.andreev_av.user.net.IAvatarHttpRequest;
import ru.andreev_av.user.utils.Constants;
import ru.andreev_av.user.utils.EmailValidator;
import ru.andreev_av.user.utils.ImageUtils;

public class EditUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvActionTypeName;
    private ImageView imgSave;
    private ImageView imgClose;

    private EditText etUserFirstName;
    private EditText etUserLastName;
    private EditText etUserEmail;
    private CircleImageView imgAvatar;

    private UserModel user;

    private EmailValidator emailValidator = new EmailValidator();

    private ConnectionDetector connectionDetector;

    private ImageUtils imageUtils;
    private String formattedDate;
    private Bitmap selectedAvatarBitmap = null;
    private IAvatarHttpRequest avatarHttpRequest;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        findComponents();

        initToolbar();

        initListeners();

        user = getIntent().getParcelableExtra(Constants.USER_OBJECT);

        connectionDetector = new ConnectionDetector(this);

        avatarHttpRequest = new AvatarHttpRequestAmazonaws(this);

        imageUtils = new ImageUtils(this);

        initComponents();

    }

    private void findComponents() {
        toolbar = (Toolbar) findViewById(R.id.tlb_edit_actions);
        tvActionTypeName = (TextView) findViewById(R.id.tv_action_type_name);
        imgSave = (ImageView) findViewById(R.id.img_user_save);
        imgClose = (ImageView) findViewById(R.id.img_user_close);

        etUserFirstName = (EditText) findViewById(R.id.et_user_first_name);
        etUserLastName = (EditText) findViewById(R.id.et_user_last_name);
        etUserEmail = (EditText) findViewById(R.id.et_user_email);
        imgAvatar = (CircleImageView) findViewById(R.id.img_avatar);
    }

    protected void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initListeners() {

        etUserFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (getWindow() != null)
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user == null || (user.getId() == null && user.getId() == -1)) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, Constants.REQUEST_AVATAR_SELECT);
                } else {
                    Toast.makeText(EditUserActivity.this, R.string.updating_avatars_not_implemented, Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newUserFirstName = etUserFirstName.getText().toString();
                String newUserLastName = etUserLastName.getText().toString();
                String newUserEmail = etUserEmail.getText().toString();

                if (!validate(newUserFirstName, newUserLastName, newUserEmail)) {
                    return;
                }

                if (user == null) {
                    user = new UserModel();
                    user.setId(-1);
                    if (selectedAvatarBitmap != null) {
                        String s3url = AmazonawsApi.URL_WITH_BUCKET + formattedDate + ".png";
                        user.setAvatarUrl(s3url);
                    }
                }

                // чтобы лишний раз не сохранять - проверяем, были ли изменены данные
                if (user.getId() == -1 || edited(user, newUserFirstName, newUserLastName, newUserEmail)) {

                    user.setFirstName(newUserFirstName);
                    user.setLastName(newUserLastName);
                    user.setEmail(newUserEmail);

                    if (connectionDetector.isNetworkAvailableAndConnected()) {
                        if (user.getId() == -1)
                            avatarHttpRequest.addUserAvatar(formattedDate);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.USER_OBJECT, user);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else
                        Toast.makeText(EditUserActivity.this, R.string.connection_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initComponents() {
        if (user != null) {
            etUserFirstName.setText(user.getFirstName());
            etUserLastName.setText(user.getLastName());
            etUserEmail.setText(user.getEmail());
            tvActionTypeName.setText(R.string.title_edit);

            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.length() > 0) {
                Picasso.with(this)
                        .load(avatarUrl)
                        .fit()
                        .noFade()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.mipmap.ic_launcher);
            }

        } else {
            etUserFirstName.setText("");
            etUserLastName.setText("");
            etUserEmail.setText("");
            tvActionTypeName.setText(R.string.title_add);
        }

        etUserFirstName.setSelection(etUserFirstName.getText().length());
    }

    private boolean validate(String newUserFirstName, String newUserLastName, String newUserEmail) {

        if (newUserFirstName.trim().length() == 0) {
            Toast.makeText(EditUserActivity.this, R.string.enter_first_name, Toast.LENGTH_SHORT).show();
            etUserFirstName.requestFocus();
            return false;
        }
        if (newUserLastName.trim().length() == 0) {
            Toast.makeText(EditUserActivity.this, R.string.enter_last_name, Toast.LENGTH_SHORT).show();
            etUserLastName.requestFocus();
            return false;
        }
        if (newUserEmail.trim().length() == 0) {
            Toast.makeText(EditUserActivity.this, R.string.enter_email, Toast.LENGTH_SHORT).show();
            etUserEmail.requestFocus();
            return false;
        }
        if (!emailValidator.validate(newUserEmail)) {
            Toast.makeText(EditUserActivity.this, R.string.enter_correct_email, Toast.LENGTH_SHORT).show();
            etUserEmail.requestFocus();
            return false;
        }
        return true;
    }

    // для проверки, изменились ли данные пользователя
    private boolean edited(UserModel user, String newUserFirstName, String newUserLastName, String newUserEmail) {
        // описываем условия, при которых будет считаться, что произошло редактирование - чтобы сохранять только при изменении данных
        // если хотя бы одно из условий равно true - значит запись была отредактирована
        return (user.getFirstName() == null && newUserFirstName != null) ||
                (user.getLastName() == null && newUserLastName != null) ||
                (user.getEmail() == null && newUserEmail != null) ||
                (user.getFirstName() != null && newUserFirstName != null && !user.getFirstName().equals(newUserFirstName)) ||
                (user.getLastName() != null && newUserLastName != null && !user.getLastName().equals(newUserLastName)) ||
                (user.getEmail() != null && newUserEmail != null && !user.getEmail().equals(newUserEmail));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {

            if (requestCode == Constants.REQUEST_AVATAR_SELECT) {
                Uri selectedImage = imageReturnedIntent.getData();
                try {
                    selectedAvatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgAvatar.setImageBitmap(selectedAvatarBitmap);
                formattedDate = sdf.format(Calendar.getInstance().getTime());
                imageUtils.saveBitmap(selectedAvatarBitmap, formattedDate);
            }
        }

    }
}
