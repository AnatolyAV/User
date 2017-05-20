package ru.andreev_av.user.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.andreev_av.user.R;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.utils.Constants;

public class EditUserFragment extends Fragment {

    private EditText etUserFirstName;
    private EditText etUserLastName;
    private EditText etUserEmail;
    private CircleImageView imgAvatar;

    private UserModel user;

    public EditUserFragment() {
        // Required empty public constructor
    }

    public static EditUserFragment newInstance(int pos, UserModel user) {
        EditUserFragment fragment = new EditUserFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.USER_POSITION, pos);
        args.putParcelable(Constants.USER_OBJECT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_user, container, false);

        findComponents(v);

        initComponents();

        initListeners();

        return v;
    }

    protected void findComponents(View v) {
        etUserFirstName = (EditText) v.findViewById(R.id.et_user_first_name);
        etUserLastName = (EditText) v.findViewById(R.id.et_user_last_name);
        etUserEmail = (EditText) v.findViewById(R.id.et_user_email);
        imgAvatar = (CircleImageView) v.findViewById(R.id.img_avatar);
    }

    private void initComponents() {
        user = getArguments().getParcelable(Constants.USER_OBJECT);
        if (user != null) {
            etUserFirstName.setText(user.getFirstName());
            etUserLastName.setText(user.getLastName());
            etUserEmail.setText(user.getEmail());

            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.length() > 0) {
                Picasso.with(getActivity())
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
        }

        etUserFirstName.setSelection(etUserFirstName.getText().length());
    }

    protected void initListeners() {
        etUserFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (getActivity().getWindow() != null)
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                    Toast.makeText(getActivity(), R.string.updating_avatars_not_implemented, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int getUserPosition() {
        return getArguments().getInt(Constants.USER_POSITION, 0);
    }


}
