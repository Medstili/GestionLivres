package com.example.gestionlivres.bookRecyclerView;

public class Book {
    private String title;
    private int imageResId;

    public Book(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}
