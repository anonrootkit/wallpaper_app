package com.yash.wallpaper.communication;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseUser;
import com.yash.wallpaper.R;
import com.yash.wallpaper.model.UserDetails;

public class CommunicationHelper {

    private static GoogleSignInClient googleSignInClient;
    public static final String TAB_ONE_NAME = "Wallpapers";
    public static final String TAB_TWO_NAME = "Categories";
    public static final String TAB_THREE_NAME = "Your Collection";
    private static String USER_DB_REF = "/users/";
    private static String CATEGORY_DB_REF = "/categories/";
    private static String USER_ID = null;
    public static final int SAVE_WALLPAPER = 1;
    public static final int SET_WALLPAPER = 2;
    public static final int SHARE_WALLPAPER = 3;
    public static final int WRITE_ACCESS_PERMISSION_CODE = 999;
    private static int visibleItems;
    public static final String[] WALLPAPER_CATEGORIES = {"Random", "Animals", "Anime", "Bollywood", "Logos", "Cars & Vehicles", "Designs", "Drawings", "Entertainment", "Funny", "Games", "Holidays", "Love", "Music", "Nature", "News & Politics", "Other", "People", "Patterns", "Spiritual", "Sport", "Technology"};

    public static void setVisibleItems(int count){
        visibleItems = count;
    }

    public static int getVisibleItems(){
        return visibleItems;
    }

    public static void init(Context context){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getResources().getString(R.string.default_web_client_id))
                .requestEmail().requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static GoogleSignInClient getGoogleSignInClient(){
        return googleSignInClient;
    }

    public static void initUserDetails(FirebaseUser user){
        UserDetails.setInstance(user);
    }

    public static String getUserCollectionDbRef(){
        return getUserDbRef() + getUserId();
    }

    public static void setUserId(String id){
        USER_ID = id;
    }

    private static String getUserId(){
        return USER_ID;
    }

    public static String getUserDbRef(){
        return USER_DB_REF;
    }

    public static String getUserCategoryDbRef(int position){
        return CATEGORY_DB_REF + WALLPAPER_CATEGORIES[position] + "/" + UserDetails.getPersonId() ;
    }

    public static String getCategoryDbRef(int position){
        return CATEGORY_DB_REF + WALLPAPER_CATEGORIES[position];
    }

    public static String getCategoryDbRef(String category){
        return CATEGORY_DB_REF + category + "/" + UserDetails.getPersonId() ;
    }
}
