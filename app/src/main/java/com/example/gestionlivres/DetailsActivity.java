package com.example.gestionlivres;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.gestionlivres.bookRecyclerView.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity {

    private ImageView detailBookImage;
    private TextView detailBookTitle;
    private TextView detailBookAuthor;
    private TextView detailBookDescription;
    private TextView detailBookAvailability;
    private TextView detailBookBorrowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        detailBookImage = findViewById(R.id.detailBookimage);
        detailBookTitle = findViewById(R.id.detailBookTitle);
        detailBookAuthor = findViewById(R.id.detailBookAuthor);
        detailBookDescription = findViewById(R.id.detailBookDescription);
        detailBookAvailability = findViewById(R.id.detailBookAvailability);
        detailBookBorrowed = findViewById(R.id.detailBookBorrowed);
        TextView detailBorrowedDate = findViewById(R.id.detailBookBorrowedDate);
        TextView detailReturnDate = findViewById(R.id.detailBookReturnDate);
        ImageView detailBookImage = findViewById(R.id.detailBookimage);
        String bookId = getIntent().getStringExtra("bookId");
        Log.d("bookId", "bookId found: " + bookId);
        if (bookId != null) {
            Log.d("bookId", "bookId found: " + bookId);
        }
        assert bookId != null;
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Books").child(bookId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("FirebaseSnapshot", "Snapshot Exists: " + snapshot.exists());
                    return;
                }
                Log.d("FirebaseSnapshot", "Snapshot Value: " + snapshot.getValue());
                Book book = snapshot.getValue(Book.class);
                if (book == null) {
                    Log.e("Book", "Book object is null");
                    return;
                }
                String imageUrl = book.getBookImageUrl();
                String title = book.getTitle();
                String author = book.getAuthor();
                String description = book.getDescription();
                boolean isAvailable = book.isAvailable();
                boolean isBorrowed = book.isBorrowed();
                String borrowedDate = book.getBorrowedDate();
                String returnDate = book.getReturnDate();
                System.out.println(imageUrl);
                Log.d("imageUrl",  imageUrl);

                detailBookTitle.setText(title);
                detailBookAuthor.setText( author);
                detailBookDescription.setText(description);
                detailBookAvailability.setText(isAvailable  ? "Available" : "Not Available");
                detailBookBorrowed.setText(isBorrowed ? "Borrowed" : "Not Borrowed");
                detailBorrowedDate.setText(borrowedDate);
                detailReturnDate.setText(returnDate);
                Glide.with(DetailsActivity.this)
                        .load(imageUrl)
                        .error(R.drawable.openbook)
                        .into(detailBookImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error: " + error.getMessage());
            }
        });

        getWindow().setSharedElementEnterTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition)
        );
        getWindow().setSharedElementReturnTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition)
        );

        Button detailBackBtn = findViewById(R.id.detailBackBtn);
        detailBackBtn.setOnClickListener(v -> finish());
    }
}