package com.example.collectionbook.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.collectionbook.R;
import com.example.collectionbook.adapter.ListAdapter;
import com.example.collectionbook.adapter.SortAdapter;
import com.example.collectionbook.helper.LoadingDialog;
import com.example.collectionbook.model.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {

    private RecyclerView rvMain, recyclerView;
    private ListAdapter bookAdapter;
    private SortAdapter sortAdapter;
    private List<ModelPdf> bookList, filteredBookList;
    private EditText searchEt;
    private Handler handler;
    private Runnable searchRunnable;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        rvMain = view.findViewById(R.id.rvMain);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEt = view.findViewById(R.id.search);

        // Initialize handler and loading dialog
        handler = new Handler();
        loadingDialog = new LoadingDialog(requireContext());

        // Initialize bookList and adapter
        bookList = new ArrayList<>();
        filteredBookList = new ArrayList<>();
        bookAdapter = new ListAdapter(getContext(), filteredBookList);
        rvMain.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMain.setAdapter(bookAdapter);

        // Initialize sort adapter
        String[] sortCharacters = {"#", "!", "@", "1", "2", "3", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        sortAdapter = new SortAdapter(sortCharacters, this::scrollToSection);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sortAdapter);

        // Load books
        loadBooks();

        // Set up search functionality
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null && searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> {
                    loadingDialog.show();
                    filterBooks(s.toString());
                    loadingDialog.dismiss();
                };
                handler.postDelayed(searchRunnable, 500); // Delay of 500 milliseconds
            }
        });

        return view;
    }

    private void loadBooks() {
        loadingDialog.show();
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                bookList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                    bookList.add(modelPdf);
                }
                // Sort the book list
                Collections.sort(bookList, (book1, book2) -> book1.getTitle().compareToIgnoreCase(book2.getTitle()));
                // Initially, show all books
                filteredBookList.clear();
                filteredBookList.addAll(bookList);
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                loadingDialog.dismiss();
            }
        });
    }

    private void filterBooks(String query) {
        filteredBookList.clear();
        if (query.isEmpty()) {
            filteredBookList.addAll(bookList);
        } else {
            for (ModelPdf book : bookList) {
                if (book.getTitle().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) ||
                        book.getAuthor().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                    filteredBookList.add(book);
                }
            }
        }
        bookAdapter.notifyDataSetChanged();
    }

    private void scrollToSection(String character) {
        for (int i = 0; i < filteredBookList.size(); i++) {
            ModelPdf book = filteredBookList.get(i);
            if (book.getTitle().toUpperCase().startsWith(character)) {
                rvMain.scrollToPosition(i);
                break;
            }
        }
    }
}
