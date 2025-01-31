package com.example.gestionlivres;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionlivres.bookRecyclerView.Book;
import com.example.gestionlivres.bookRecyclerView.BookAdapter;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        recyclerView = findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookList = new ArrayList<>();
        bookList.add(new Book("Book 1", R.drawable.openbook));
        bookList.add(new Book("Book 2", R.drawable.openbook));
        bookList.add(new Book("Book 3", R.drawable.openbook));

        bookAdapter = new BookAdapter(bookList, this);
        recyclerView.setAdapter(bookAdapter);
    }

    @Override
    public void onDeleteClick(int position) {
        Toast.makeText(this, "Deleted: " + bookList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
        bookList.remove(position);
        bookAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onUpdateClick(int position) {
        Toast.makeText(this, "Update: " + bookList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetailsClick(int position) {
        Toast.makeText(this, "Details of: " + bookList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
    }
}