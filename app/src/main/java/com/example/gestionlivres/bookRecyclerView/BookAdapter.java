package com.example.gestionlivres.bookRecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gestionlivres.Activities.DashboardActivity;
import com.example.gestionlivres.Activities.DetailsActivity;
import com.example.gestionlivres.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private static List<Book> bookList;
//    public static List<Book> filteredList;
    private static final Book book = new Book();
    public static String book_Id ;
    private final DashboardActivity dashboardActivity;

    public BookAdapter(List<Book> bookList, DashboardActivity dashboardActivity) {
        BookAdapter.bookList = bookList;
        this.dashboardActivity = dashboardActivity;
//        filteredList = new ArrayList<>(BookAdapter.bookList);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_cardview, parent, false);
        return new BookViewHolder(view, dashboardActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
//        Book book = filteredList.get(position);
        holder.bookTitle.setText(book.getTitle());
        if (book.getBookImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getBookImageUrl())
                    .error(R.drawable.openbook)
                    .into(holder.bookImage);
        }
    }
    @Override
    public int getItemCount() {
        return bookList.size();
    }

//    public void filter(String query) {
//        filteredList = new ArrayList<>();
//        if (query.isEmpty()) {
//            filteredList.addAll(bookList);
//        } else {
//            for (Book book : bookList) {
//                if (book.getTitle()
//                        .toLowerCase()
//                        .contains(query.toLowerCase().trim())
//                ) {
//                    filteredList.add(book);
//
//                    System.out.println("Match found: " + book.getTitle());
//                }
//            }
//            this.notifyDataSetChanged();
//        }
//        System.out.println("Filtered List Size: " + filteredList.size());
//    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        ImageView bookImage;
        MaterialButton deleteBtn, updateBtn, detailsBtn;


        public BookViewHolder(@NonNull View itemView, DashboardActivity dashboardActivity) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookImage = itemView.findViewById(R.id.cardBookImage);
            deleteBtn = itemView.findViewById(R.id.cardDeleteBtn);
            updateBtn = itemView.findViewById(R.id.cardAddBtn);
            detailsBtn = itemView.findViewById(R.id.cardDetailsBtn);
            deleteBtn.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String bookId = bookList.get(position).getBookId();


                        if (bookId != null) {
                            System.out.println(bookId);
                            book.deleteBook(bookId, itemView.getContext());

                        }
                        else{
                            System.out.println("null");
                        }

                    }

            });
            detailsBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    String bookId = bookList.get(position).getBookId();
                    if (bookId != null) {
                        System.out.println(bookId);
                        Intent intent = new Intent(itemView.getContext(), DetailsActivity.class);
                        intent.putExtra("bookId", bookId);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) itemView.getContext(),
                                bookImage,  // The ImageView
                                "sharedBookImage" // The transition name
                        );
                        itemView.getContext().startActivity(intent, options.toBundle());

                    } else {
                        System.out.println("null");
                    }
                }
            });
            updateBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    book_Id = bookList.get(position).getBookId();
                    if (book_Id != null){
                        System.out.println(book_Id);
                    }
                    dashboardActivity.showUpdateBookDialog();

                }
            });
        }
    }

}
