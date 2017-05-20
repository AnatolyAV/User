package ru.andreev_av.user.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;

import ru.andreev_av.user.R;
import ru.andreev_av.user.enums.PaneMode;
import ru.andreev_av.user.fragments.EditUserFragment;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.utils.Constants;

public class EditUserActivity extends AbstractUserActivity {


    private TextView tvActionTypeName;
    private ImageView imgClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        paneMode = PaneMode.OnePaneMode;

        findComponents();

        initToolbar();

        initListeners();

        currentUser = getIntent().getParcelableExtra(Constants.USER_OBJECT);

        initTitle();

        initDetector();

        initAvatarHttpRequest();

        initImageUtils();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isLarge()) {
            ActivityCompat.finishAfterTransition(this);
            return;
        }

        initEditUserFragment();

    }

    private boolean isLarge() {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        findComponentsAvatarViewFromFragment();
        initImageAvatar();
    }

    protected void findComponents() {
        super.findComponents();
        toolbar = (Toolbar) findViewById(R.id.tlb_edit_actions);
        tvActionTypeName = (TextView) findViewById(R.id.tv_action_type_name);
        imgClose = (ImageView) findViewById(R.id.img_user_close);
    }

    protected void initListeners() {
        super.initListeners();
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAfterTransition(EditUserActivity.this);
            }
        });
    }

    protected void initTitle() {
        if (currentUser == null) {
            tvActionTypeName.setText(R.string.title_add);
        } else {
            tvActionTypeName.setText(R.string.title_edit);
        }
    }

    private void initEditUserFragment(){
        editUserFragment = EditUserFragment.newInstance(getIntent()
                .getIntExtra(Constants.USER_POSITION, 0), (UserModel) getIntent().getParcelableExtra(Constants.USER_OBJECT));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_content, editUserFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {

//            if (requestCode == Constants.REQUEST_AVATAR_SELECT) {
            Uri selectedImage = imageReturnedIntent.getData();
            try {
                selectedAvatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            findComponentsAvatarViewFromFragment();
            imgAvatar.setImageBitmap(selectedAvatarBitmap);
            avatarFileName = sdf.format(Calendar.getInstance().getTime());
            imageUtils.saveBitmap(selectedAvatarBitmap, avatarFileName);
//            }
        }
    }
}
