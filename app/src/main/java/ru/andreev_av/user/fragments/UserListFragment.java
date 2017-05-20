package ru.andreev_av.user.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.andreev_av.user.R;
import ru.andreev_av.user.adapter.UserListAdapter;
import ru.andreev_av.user.model.UserModel;

public class UserListFragment extends Fragment {

    private onUserItemClickListener listener;
    private UserListAdapter adapter;
    private RecyclerView rvUserList;

    public UserListFragment() {
    }

    @SuppressWarnings("unused")
    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            rvUserList = (RecyclerView) view;
            rvUserList.setLayoutManager(new LinearLayoutManager(context));
            rvUserList.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onUserItemClickListener) {
            listener = (onUserItemClickListener) context;
            if (adapter != null)
                adapter.setListener(listener);
//            adapter.setContext((Activity) getContext());
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setAdapter(UserListAdapter adapter) {
        this.adapter = adapter;
        adapter.setListener(listener);
    }

    public void refreshList(List<UserModel> userList){
        adapter.refreshList(userList);
    }

    public interface onUserItemClickListener {
        void userItemClick(int position, UserModel user);
    }
}
