package com.example.gestionlivres.auth;

import android.content.Context;
import android.view.View;

import com.example.gestionlivres.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Admin {
    private String username, password, email, id;

    public Admin() { } // Required for Firebase

    public Admin(String username, String password, String email, String id) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.id = id;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public void addAdmin(View view, String adminName, String adminPassword, String adminEmail, String adminId ){

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin");
        Admin newAdmin = new Admin(adminName, adminPassword, adminEmail, adminId);
        adminRef.child(adminId).setValue(newAdmin).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Snackbar.make(view , "Admin added successfully", Snackbar.LENGTH_SHORT).show();
            }
            else {
                Snackbar.make(view, "Admin failed to add", Snackbar.LENGTH_SHORT).show();
            }
        });

    }
}
