package com.example.gestionlivres.bookRecyclerView;



import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Book {
    private String title, author, description, bookImageUrl,borrowedDate, returnDate,bookId;;
    private boolean isAvailable, isBorrowed;



    public Book() { }

    public Book(String title, String author, String description, String bookImageUrl, boolean isAvailable, boolean isBorrowed, String borrowedDate, String returnDate) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.bookImageUrl = bookImageUrl;
        this.isAvailable = isAvailable;
        this.isBorrowed = isBorrowed;
        this.borrowedDate = borrowedDate;
        this.returnDate = returnDate;
    }

    // Getters and Setters
    public String getBookId() {
        return bookId;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBookImageUrl() { return bookImageUrl; }
    public void setBookImage(String bookImage) { this.bookImageUrl = bookImage; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public boolean isBorrowed() { return isBorrowed; }
    public void setBorrowed(boolean borrowed) { isBorrowed = borrowed; }
    public String getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(String borrowedDate) { this.borrowedDate = borrowedDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void addBook(Context context, String title, String author, String description, String bookImageUrl, boolean isAvailable, boolean isBorrowed, String borrowedDate, String returnDate) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
        String bookId = booksRef.push().getKey();
        Book newBook = new Book(title, author, description, bookImageUrl, isAvailable, isBorrowed,borrowedDate,returnDate);
        if (bookId != null) {
            newBook.setBookId(bookId);
            booksRef.child(bookId).setValue(newBook).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context ,"Book added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText( context, "Failed to add book", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    public void updateBook(String bookId, Context context) {
//        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
//
//
//        bookRef.setValue(updatedBook).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(context, "Book updated successfully!", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Toast.makeText(context, "Failed to update book", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void deleteBook(String bookId, Context context) {
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);

        bookRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Book deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete book", Toast.LENGTH_SHORT).show();
            }
        });
    }
//    public void getAllBooks() {
//        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("Books");
//
//        booksRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
//                    Book book = bookSnapshot.getValue(Book.class);
//                    if (book != null) {
//                        Log.d("Firebase", "Title: " + book.getTitle() + ", Author: " + book.getAuthor());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Error fetching data", error.toException());
//            }
//        });
//    }


}

