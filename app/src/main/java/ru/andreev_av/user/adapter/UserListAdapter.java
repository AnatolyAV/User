package ru.andreev_av.user.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.andreev_av.user.R;
import ru.andreev_av.user.activities.EditUserActivity;
import ru.andreev_av.user.adapter.holder.UserViewHolder;
import ru.andreev_av.user.model.UserModel;
import ru.andreev_av.user.utils.Constants;

public class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public UserListAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        final UserModel user = userList.get(position);

        holder.cvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditUserActivityOnClick(user);
            }
        });

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.length() > 0) {
            Picasso.with(context)
                    .load(avatarUrl)
                    .fit()
                    .noFade()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher);
        }

        holder.tvEmail.setText(user.getEmail());
        holder.tvName.setText(user.getFirstName() + " " + user.getLastName());

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void refreshList(List<UserModel> list) {
        userList = list;
    }

    private void openEditUserActivityOnClick(final UserModel user){
        Intent intent = new Intent(context, EditUserActivity.class);
        intent.putExtra(Constants.USER_OBJECT, user);
        ((Activity)context).startActivityForResult(intent, Constants.REQUEST_USER_EDIT);
    }
}