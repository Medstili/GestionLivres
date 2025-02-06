package com.example.gestionlivres.Activities;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
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

import com.bumptech.glide.Glide;
import com.example.gestionlivres.R;
import com.example.gestionlivres.bookRecyclerView.Book;
import com.example.gestionlivres.bookRecyclerView.BookAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Locale;
import java.util.Objects;
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

    //    public  Dialog dialog = new Dialog(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLanguage();
        super.onCreate(savedInstanceState);
        // Load last saved language preference

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
        FloatingActionButton fab = findViewById(R.id.fab_language);
        fab.setOnClickListener(view -> showPopup(view));
//        SearchView searchView = findViewById(R.id.searchBar);


        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        storage = FirebaseStorage.getInstance();
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, this);
        fetchBooksFromFirebase();
        recyclerView.setAdapter(bookAdapter);
        btnShowAddBookDialog.setOnClickListener(v -> showAddBookDialog());


        // Handle Search Input
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                bookAdapter.filter(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                bookAdapter.filter(newText);
//                return false;
//            }
//        });

    }


    private void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Arabic");  // Arabic
        popup.getMenu().add("English");  // English

        popup.setOnMenuItemClickListener(item -> {
            if (Objects.equals(item.getTitle(),"Arabic")) {
                changeLanguage("ar");
            } else if (item.getTitle().equals("English")) {
                changeLanguage("en");
            }
            return true;
        });

        popup.show();
    }
    private void changeLanguage(String lang) {
        SharedPreferences sharedPref = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("My_Lang", lang);
        editor.apply();

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // Restart activity to apply changes
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
    private void loadLanguage() {
        SharedPreferences sharedPref = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = sharedPref.getString("My_Lang", "en"); // Default is English
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
    // Method to show the add book dialog
    public void showAddBookDialog() {

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
            String title = editBookTitle.getText().toString().trim();
            String author = editBookAuthor.getText().toString().trim();
            String description = editBookDescription.getText().toString().trim();
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
    // Method to show the update book dialog
    public void showUpdateBookDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_book);

        EditText editBookTitle = dialog.findViewById(R.id.editBookTitle);
        EditText editBookAuthor = dialog.findViewById(R.id.editBookAuthor);
        EditText editBookDescription = dialog.findViewById(R.id.editBookDescription);
        MaterialSwitch switchAvailability = dialog.findViewById(R.id.switchAvailability);
        RadioGroup radioGroupBorrowed = dialog.findViewById(R.id.radioGroupBorrowed);
        Button btnSelectBorrowDate = dialog.findViewById(R.id.btnSelectBorrowDate);
        Button btnSelectReturnDate = dialog.findViewById(R.id.btnSelectReturnDate);
        Button btnUploadImage = dialog.findViewById(R.id.dialogBtnUploadImage);
        Button dialogBtnUpdateBook = dialog.findViewById(R.id.dialogBtnUpdateBook);
        Button dialogBtnAddBook = dialog.findViewById(R.id.dialogBtnAddBook);
        RadioButton borrowed = dialog.findViewById(R.id.radioBorrowed);
        RadioButton notBorrowed = dialog.findViewById(R.id.radioNotBorrowed);
        dialogBookImage = dialog.findViewById(R.id.dialogBookImage);
        View layoutBorrowed = dialog.findViewById(R.id.layoutBorrowed);
        View layoutDates = dialog.findViewById(R.id.layoutDates);

        dialogBtnUpdateBook.setVisibility(View.VISIBLE);
        dialogBookImage.setVisibility(View.VISIBLE);
        dialogBtnAddBook.setVisibility(View.GONE);

//        retrieving the book data from the database
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(BookAdapter.book_Id);
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                imageUri = Uri.parse(book.getBookImageUrl());
                Log.d("ImageUri", "Image URI: " + imageUri.toString());

                String title = book.getTitle().trim();
                String author = book.getAuthor().trim();
                String description = book.getDescription().trim();
                boolean isAvailable = book.isAvailable();
                boolean isBorrowed = book.isBorrowed();
                String borrowedDate = book.getBorrowedDate();
                String returnDate = book.getReturnDate();

                editBookTitle.setText(title);
                editBookAuthor.setText( author);
                editBookDescription.setText(description);
                switchAvailability.setChecked(isAvailable);
                layoutBorrowed.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
                layoutDates.setVisibility(isBorrowed ? View.VISIBLE : View.GONE);
                btnSelectBorrowDate.setText("Borrow Date: " + borrowedDate);
                btnSelectReturnDate.setText("Return Date: " + returnDate);
                radioGroupBorrowed.check(isBorrowed ? R.id.radioBorrowed : R.id.radioNotBorrowed);

                Glide.with(DashboardActivity.this)
                        .load(imageUri)
                        .into(dialogBookImage);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error: " + error.getMessage());
            }
        });

//        if (switchAvailability.isChecked()){
//            layoutBorrowed.setVisibility( View.VISIBLE );
//        }else{
//            layoutBorrowed.setVisibility( View.GONE );
//            btnSelectBorrowDate.setText("Borrow Date: ");
//            btnSelectReturnDate.setText("Return Date: ");
//            borrowDate = "";
//            returnDate ="";
//        }
//
//        if (layoutBorrowed.isShown()){
//            layoutDates.setVisibility(View.VISIBLE);
//        }else{
//            layoutDates.setVisibility(View.GONE);
//        }

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutBorrowed.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (layoutBorrowed.getVisibility() == View.GONE){

               borrowed.setChecked(false);
               notBorrowed.setChecked(true);
                btnSelectBorrowDate.setText("Borrow Date: ");
                btnSelectReturnDate.setText("Return Date: ");
                borrowDate = "";
                returnDate ="";
            }
        });


        radioGroupBorrowed.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBorrowed) {
                layoutDates.setVisibility(View.VISIBLE);
            } else {
                layoutDates.setVisibility(View.GONE);
                borrowDate = "";
                returnDate = "";
                btnSelectBorrowDate.setText("Borrow Date: ");
                btnSelectReturnDate.setText("Return Date: ");
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

        dialogBtnUpdateBook.setOnClickListener(v -> {
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
            uploadImage(this);
            Log.e("image Image Uri", "the image uri = : " + imageUri);
                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setDescription(description);
                book.setBookImage(imageUri.toString());
                book.setAvailable(isAvailable);
                book.setBorrowed(isBorrowed);
                book.setBorrowedDate(borrowDate);
                book.setReturnDate(returnDate);
                book.setBookId(BookAdapter.book_Id);

                book.updateBook(this);

            dialog.dismiss();
        });

        dialog.show();
    }
    // Helper method to show the date picker dialog
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
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//        startActivityForResult(
//                Intent.createChooser(
//                        intent,
//                        "Select Image from here..."),
//                PICK_IMAGE_REQUEST);
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
            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storage.getReference().child("images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                            })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                view.clearFocus();
            }
        }
        return super.onTouchEvent(event);
    }
}
