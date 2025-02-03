package com.example.gestionlivres;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionlivres.bookRecyclerView.Book;
import com.example.gestionlivres.bookRecyclerView.BookAdapter;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class DashboardActivity extends AppCompatActivity  {

    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private String borrowDate = "", returnDate = "";
    private Uri imageUri ;
    private final Book newBook = new Book();
    private ImageView dialogBookImage;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private final int PICK_IMAGE_REQUEST = 22;
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
        RecyclerView recyclerView = findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnShowAddBookDialog = findViewById(R.id.btnShowAddBookDialog);
        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        storage = FirebaseStorage.getInstance();
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        fetchBooksFromFirebase();
        recyclerView.setAdapter(bookAdapter);
        btnShowAddBookDialog.setOnClickListener(v -> showAddBookDialog());
    }

    private void showAddBookDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_book);
        EditText editBookTitle = dialog.findViewById(R.id.editBookTitle);
        EditText editBookAuthor = dialog.findViewById(R.id.editBookAuthor);
        EditText editBookDescription = dialog.findViewById(R.id.editBookDescription);
        MaterialSwitch switchAvailability = dialog.findViewById(R.id.switchAvailability);
        RadioGroup radioGroupBorrowed = dialog.findViewById(R.id.radioGroupBorrowed);
        Button btnSelectBorrowDate = dialog.findViewById(R.id.btnSelectBorrowDate);
        Button btnSelectReturnDate = dialog.findViewById(R.id.btnSelectReturnDate);
        Button dialogBtnAddBook = dialog.findViewById(R.id.dialogBtnAddBook);
        Button btnUploadImage = dialog.findViewById(R.id.dialogBtnUploadImage);
        dialogBookImage = dialog.findViewById(R.id.dialogBookImage);


        View layoutBorrowed = dialog.findViewById(R.id.layoutBorrowed);
        View layoutDates = dialog.findViewById(R.id.layoutDates);

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutBorrowed.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        radioGroupBorrowed.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBorrowed) {
                layoutDates.setVisibility(View.VISIBLE);
            } else {
                layoutDates.setVisibility(View.GONE);
            }
        });

        btnSelectBorrowDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            borrowDate = date;
            btnSelectBorrowDate.setText("Borrow Date: " + date);
        }));

        btnSelectReturnDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            returnDate = date;
            btnSelectReturnDate.setText("Return Date: " + date);
        }));

        btnUploadImage.setOnClickListener(v -> {

            openGallery();

        });

        dialogBtnAddBook.setOnClickListener(v -> {
            String title = editBookTitle.getText().toString();
            String author = editBookAuthor.getText().toString();
            String description = editBookDescription.getText().toString();
            boolean isAvailable = switchAvailability.isChecked();
            boolean isBorrowed = ((RadioButton) dialog.findViewById(R.id.radioBorrowed)).isChecked();
            if (title.isEmpty() || author.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isBorrowed && (borrowDate.isEmpty() || returnDate.isEmpty())) {
                Toast.makeText(this, "Please select both borrow and return dates", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri== null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImage(this);
            Log.e("image Image Uri", "the image uri = : " + imageUri);
            newBook.addBook(this, title, author, description, imageUri.toString(), isAvailable, isBorrowed, borrowDate, returnDate);
                bookList.add(newBook);
                bookAdapter.notifyItemInserted(bookList.size() - 1);
                dialog.dismiss();
        });
        dialog.show();
    }
    private void showDatePickerDialog(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            listener.onDateSelected(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
    private void openGallery() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, 100); // Request code 100
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            imageUri = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                imageUri);
                dialogBookImage.setImageBitmap(bitmap);
                dialogBookImage.setVisibility(View.VISIBLE);

            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
    private void uploadImage(Context context) {
        if (imageUri != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref
                    = storage.getReference()
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .addOnSuccessListener(
                            taskSnapshot -> {

                                // Image uploaded successfully
                                // Dismiss dialog
                                progressDialog.dismiss();
                                Toast
                                        .makeText(context,
                                                "Image Uploaded!!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            })

                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast
                                .makeText(context,
                                        "Failed " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    })
                    .addOnProgressListener(
                            taskSnapshot -> {
                                double progress
                                        = (100.0
                                        * taskSnapshot.getBytesTransferred()
                                        / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage(
                                        "Uploaded "
                                                + (int)progress + "%");
                            });
        }
    }
    private void fetchBooksFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookList.clear();
                for (DataSnapshot bookSnapshot : snapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Error fetching books!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}