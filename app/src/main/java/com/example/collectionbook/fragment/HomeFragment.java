package com.example.collectionbook.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.collectionbook.R;
import com.example.collectionbook.adapter.BookAdapter;
import com.example.collectionbook.helper.LoadingDialog;
import com.example.collectionbook.model.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewRecommended;
    private RecyclerView recyclerViewNewest;
    private TextView tvRecommended;
    private DatabaseReference userRef;
    private TextView tvNewest;

    private LoadingDialog loadingDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference booksRef;
    private TextView nameUser;
    private RoundedImageView imgView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(requireContext());

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        booksRef = FirebaseDatabase.getInstance().getReference().child("Books");

        recyclerViewRecommended = view.findViewById(R.id.recyclerViewRecomendedBook);
        recyclerViewNewest = view.findViewById(R.id.recyclerViewNewestBook);
        tvRecommended = view.findViewById(R.id.tvRecommended);
        tvNewest = view.findViewById(R.id.tvNewest);
        nameUser = view.findViewById(R.id.nameUser);
        imgView = view.findViewById(R.id.imgView);

        loadUserInfo();

        setupRecyclerViews();
        fetchRecommendedBooks();
        fetchNewestBooks();
    }

    private void setupRecyclerViews() {
        recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewNewest.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void fetchRecommendedBooks() {
        loadingDialog.show();
        List<ModelPdf> recommendedBooks = new ArrayList<>();
        Query recommendedQuery = booksRef.orderByChild("accessCount").limitToLast(10);
        recommendedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                recommendedBooks.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdf book = ds.getValue(ModelPdf.class);
                    if (book != null) {
                        recommendedBooks.add(0, book); // Add to the beginning to maintain the correct order
                    }
                }
                setupRecommendedBooksRecyclerView(recommendedBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
                loadingDialog.dismiss();
            }
        });
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        String uid = firebaseUser.getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                String email = "" + snapshot.child("email").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();

                nameUser.setText(name);
                if (!profileImage.isEmpty()) {
                    Picasso.get().load(profileImage).into(imgView);
                } else {
                    imgView.setImageResource(R.drawable.placeholder_profile); // Fallback image
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void fetchNewestBooks() {
        loadingDialog.show();
        List<ModelPdf> newestBooks = new ArrayList<>();
        Query newestQuery = booksRef.orderByChild("timestamp").limitToLast(5);
        newestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                newestBooks.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdf book = ds.getValue(ModelPdf.class);
                    if (book != null) {
                        newestBooks.add(0,book);
                    }
                }
                setupNewestBooksRecyclerView(newestBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
                loadingDialog.dismiss();
            }
        });
    }

    private void setupRecommendedBooksRecyclerView(List<ModelPdf> recommendedBooks) {
        BookAdapter recommendedAdapter = new BookAdapter(getContext(), recommendedBooks);
        recyclerViewRecommended.setAdapter(recommendedAdapter);
    }

    private void setupNewestBooksRecyclerView(List<ModelPdf> newestBooks) {
        BookAdapter newestAdapter = new BookAdapter(getContext(), newestBooks);
        recyclerViewNewest.setAdapter(newestAdapter);
    }
}
