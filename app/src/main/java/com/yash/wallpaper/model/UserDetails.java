package com.yash.wallpaper.model;

import android.net.Uri;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public class UserDetails {
    private static String personName;
    private static Uri personPhoto;
    private static String personEmail;
    private static String personId;

    public static void setInstance(FirebaseUser signInAccount){
        UserDetails userDetails = new UserDetails(signInAccount);
    }

    private UserDetails(FirebaseUser signInAccount) {
        personName = signInAccount.getDisplayName();
        personEmail = signInAccount.getEmail();
        personPhoto = signInAccount.getPhotoUrl();
        personId = signInAccount.getUid();
    }


    public static String getPersonName() {
        return personName;
    }

    public static Uri getPersonPhoto() {
        return personPhoto;
    }

    public static String getPersonEmail() {
        return personEmail;
    }

    public static String getPersonId() {
        return personId;
    }
}
