package com.example.collectionbook.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.collectionbook.EditProfileActivity;
import com.example.collectionbook.InfoActivity;
import com.example.collectionbook.LoginActivity;
import com.example.collectionbook.R;
import com.example.collectionbook.helper.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private TextView nameTv, emailTv;
    private ImageView imgView;
    private ImageButton editBtn;
    private DatabaseReference databaseReference;

    private LoadingDialog loadingDialog;
    private RelativeLayout info, logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgView = view.findViewById(R.id.imgView);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        editBtn = view.findViewById(R.id.editBtn);
        info = view.findViewById(R.id.infoBtn);
        logout = view.findViewById(R.id.logoutBtn);

        loadingDialog = new LoadingDialog(requireContext());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            loadProfile(userId);

            editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            });
        }

        info.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), InfoActivity.class));
        });

        logout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });

        return view;
    }

    private void loadProfile(String userId) {
        loadingDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    nameTv.setText(name);
                    emailTv.setText(email);
                    Picasso.get().load(profileImage).into(imgView);
                    loadingDialog.dismiss();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(ProfileFragment.this.requireContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
                loadingDialog.dismiss();
            }
        });
    }

    private void showLogoutConfirmationDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loadingDialog.show();
                        FirebaseAuth.getInstance().signOut();
                        loadingDialog.dismiss();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
