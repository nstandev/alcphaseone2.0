package com.boss.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static final int RC_SIGN_IN = 1;
    public static FirebaseDatabase xFirebaseDatabase;
    public static DatabaseReference xDatabaseReference;
    public static FirebaseStorage xFirebaseStorage;
    public static StorageReference xStorageReference;
    public static FirebaseUtil xFirebaseUtil;
    public static ArrayList<TravelDeal> xDeals;
    private static FirebaseAuth xFirebaseAuth;
    private static FirebaseAuth.AuthStateListener xAuthStateListener;
    private static ListActivity xCallerActivity;
    public static boolean isAdmin;

    private FirebaseUtil(){}

    public static void openFbReference(final ListActivity callerActivity){
        if(xFirebaseUtil == null){

            xFirebaseUtil = new FirebaseUtil();

            xCallerActivity = callerActivity;
            xFirebaseDatabase = FirebaseDatabase.getInstance();
            xFirebaseAuth = FirebaseAuth.getInstance();
            xAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (xFirebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    } else {
                        String uid = xFirebaseAuth.getUid();
                        checkAdmin(uid);
                    }

                }
            };
            xDeals = new ArrayList<>();
            connectToStorage();
        }

        xDatabaseReference = xFirebaseDatabase.getReference().child("traveldeals");
    }

    public static void connectToStorage(){
        xFirebaseStorage = FirebaseStorage.getInstance();
        xStorageReference = xFirebaseStorage.getReference().child("deals_pictures");
    }
    private static void checkAdmin(String uid) {
        xFirebaseUtil.isAdmin = false;
        DatabaseReference dbRefAdmin = xFirebaseDatabase.getReference().child("administrators")
                .child(uid);
        dbRefAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                isAdmin = true;
                xCallerActivity.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        xCallerActivity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachAuthStateListener(){
        xFirebaseAuth.addAuthStateListener(xAuthStateListener);
    }

    public static void detachAuthStateListener(){
        xFirebaseAuth.removeAuthStateListener(xAuthStateListener);
    }
}
