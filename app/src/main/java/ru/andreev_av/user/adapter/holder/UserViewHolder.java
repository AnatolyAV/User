package ru.andreev_av.user.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.andreev_av.user.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public final LinearLayout cvUser;
    public final CircleImageView imgAvatar;
    public final TextView tvEmail;
    public final TextView tvName;

    public UserViewHolder(View itemView) {
        super(itemView);

        cvUser = (LinearLayout) itemView.findViewById(R.id.cv_user);
        imgAvatar = (CircleImageView) itemView.findViewById(R.id.img_avatar);
        tvEmail = (TextView) itemView.findViewById(R.id.tv_email);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
    }
}