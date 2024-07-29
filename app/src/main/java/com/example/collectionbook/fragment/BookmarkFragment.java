package com.example.collectionbook.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.collectionbook.R;
import com.example.collectionbook.adapter.BookmarkAdapter;
import com.example.collectionbook.helper.LoadingDialog;
import com.example.collectionbook.model.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private static final String TAG = "BookmarkFragment";
    private RecyclerView recyclerView;
    private BookmarkAdapter adapter;
    private List<ModelPdf> bookList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        loadingDialog = new LoadingDialog(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bookList = new ArrayList<>();
        adapter = new BookmarkAdapter(getContext(), bookList);
        recyclerView.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            loadBookmarkedBooks();
        }

        return view;
    }

    private void loadBookmarkedBooks() {
        loadingDialog.show();
        userRef.child("bookmarks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                bookList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String bookId = ds.getKey();
                    loadBookDetails(bookId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                loadingDialog.dismiss();
            }
        });
    }

    private void loadBookDetails(String bookId) {
        loadingDialog.show();
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                ModelPdf book = snapshot.getValue(ModelPdf.class);
                if (book != null) {
                    loadingDialog.dismiss();
                    bookList.add(book);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                loadingDialog.dismiss();
            }
        });
    }
}
