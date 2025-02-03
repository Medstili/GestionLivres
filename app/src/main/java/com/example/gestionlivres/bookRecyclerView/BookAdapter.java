package com.example.gestionlivres.bookRecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gestionlivres.DetailsActivity;
import com.example.gestionlivres.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private static List<Book> bookList;
//    private OnBookClickListener listener;
    private static Book book = new Book();

//    public interface OnBookClickListener {
//        void onDeleteClick(int position);
//        void onUpdateClick(int position);
//        void onDetailsClick(int position);
//    }

    public BookAdapter(List<Book> bookList) {
        BookAdapter.bookList = bookList;
//        this.listener = listener;

    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_cardview, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
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

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        ImageView bookImage;
        MaterialButton deleteBtn, updateBtn, detailsBtn;
        public BookViewHolder(@NonNull View itemView) {
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
        }
    }

}
