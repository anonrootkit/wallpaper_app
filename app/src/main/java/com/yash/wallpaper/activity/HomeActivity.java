package com.yash.wallpaper.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yash.wallpaper.R;
import com.yash.wallpaper.adapter.HomePagerAdapter;
import com.yash.wallpaper.communication.CommunicationHelper;
import com.yash.wallpaper.fragment.CategoriesFragment;
import com.yash.wallpaper.fragment.PersonalCollectionFragment;
import com.yash.wallpaper.fragment.PublicWallpaperFragment;
import com.yash.wallpaper.model.UserDetails;
import com.yash.wallpaper.model.Wallpaper;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE = 100;
    private static final int SIGN_OUT_PROMPT = -100;
    private static final int UPLOAD_IMAGE_PROMPT = 200;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar myToolbar;
    private ImageView profilePicture;
    private TextView userEmail, userName;
    private NavigationView navigationView;
    private Uri uploadImageUri;
    private FirebaseStorage storage;
    private StorageReference wallpaperStorageRef;
    private Uri uploadedImageUri;
    private FirebaseDatabase database;
    private DatabaseReference wallpaperDbRef;
    private DatabaseReference categoryDbRef;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton uploadWallpaperFab;
    private int selectedItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        initFab();
        initToolbar();
        setUserDetails();
        setupNavigationViewClickListener();
        initViewPager();
    }

    private void initFab() {
        uploadWallpaperFab.setOnClickListener(view -> openFileChooser());
    }

    private void initViewPager() {
        HomePagerAdapter adapter = new HomePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(PublicWallpaperFragment.newInstance(), CommunicationHelper.TAB_ONE_NAME);
        adapter.addFragment(CategoriesFragment.newInstance(), CommunicationHelper.TAB_TWO_NAME);
        adapter.addFragment(PersonalCollectionFragment.newInstance(), CommunicationHelper.TAB_THREE_NAME);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        uploadImageUri = data.getData();
                        openCategoryChooser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void openCategoryChooser() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Select wallpaper category");

        b.setSingleChoiceItems(CommunicationHelper.WALLPAPER_CATEGORIES, selectedItemPosition, (dialog, which) -> {
            selectedItemPosition = which;
        }).setPositiveButton(R.string.Yes, (dialogInterface, i) -> {
            showPrompt(UPLOAD_IMAGE_PROMPT);
        }).setNegativeButton(R.string.Cancel, (dialogInterface, i) -> {
            uploadImageUri = null;
        });
        b.show();

    }

    private void startUploading() {
        Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show();
        String imageName = UserDetails.getPersonId() + Calendar.getInstance().getTimeInMillis();
        wallpaperStorageRef = storage.getReference().child("wallpapers/" + imageName);
        final UploadTask uploadTask = wallpaperStorageRef.putFile(uploadImageUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getUploadedImageUri(uploadTask);
                Toast.makeText(HomeActivity.this, "Upload complete", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show());


    }

    private void getUploadedImageUri(UploadTask uploadTask) {
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return wallpaperStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    uploadedImageUri = task.getResult();
                    startUploadingImageUrl(uploadedImageUri.toString());
                }
            }
        });
    }

    private void startUploadingImageUrl(String imageUri) {
        Wallpaper wallpaper = new Wallpaper(UserDetails.getPersonId(), UserDetails.getPersonName(), imageUri, CommunicationHelper.WALLPAPER_CATEGORIES[selectedItemPosition]);
        wallpaperDbRef = database.getReference().child(CommunicationHelper.getUserCollectionDbRef()).push();
        wallpaperDbRef.setValue(wallpaper);
        categoryDbRef = database.getReference().child(CommunicationHelper.getUserCategoryDbRef(selectedItemPosition)).push();
        categoryDbRef.setValue(wallpaper.getImageUri());
    }

    private void setupNavigationViewClickListener() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.upload_picture:
                    openFileChooser();
                    break;
                case R.id.logout:
                    showPrompt(SIGN_OUT_PROMPT);
                    break;
            }

            drawerLayout.closeDrawer(GravityCompat.START, true);
            return true;
        });
    }

    private void showPrompt(int prompt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (prompt) {
            case UPLOAD_IMAGE_PROMPT:
                builder.setMessage(R.string.upload_picture);
                builder.setPositiveButton(R.string.Yes, (dialog, id) -> startUploading());
                break;
            case SIGN_OUT_PROMPT:
                builder.setMessage(R.string.sign_out);
                builder.setPositiveButton(R.string.Yes, (dialog, id) -> {
                    Toast.makeText(HomeActivity.this, "Signing out", Toast.LENGTH_SHORT).show();
                    signout();
                });
                break;
        }

        builder.setNegativeButton(R.string.Cancel, (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    private void signout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setUserDetails() {
        if (UserDetails.getPersonPhoto() != null)
            Glide.with(getApplicationContext()).load(UserDetails.getPersonPhoto()).into(profilePicture);

        if (UserDetails.getPersonName() != null)
            userName.setText(UserDetails.getPersonName());
        else
            userName.setText("Guest");

        if (UserDetails.getPersonEmail() != null)
            userEmail.setText(UserDetails.getPersonEmail());
    }

    private void initToolbar() {
        setSupportActionBar(myToolbar);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void init() {
        myToolbar = findViewById(R.id.home_toolbar);
        drawerLayout = findViewById(R.id.activity_home);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, myToolbar, R.string.Open, R.string.Close);
        navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        profilePicture = headerView.findViewById(R.id.profile_pic);
        userEmail = headerView.findViewById(R.id.user_email);
        userName = headerView.findViewById(R.id.user_name);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        viewPager = findViewById(R.id.home_viewpager);
        tabLayout = findViewById(R.id.home_tab_layout);
        CommunicationHelper.setUserId(UserDetails.getPersonId());
        uploadWallpaperFab = findViewById(R.id.upload_picture_fab);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem != 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        } else {
            super.onBackPressed();
        }
    }

}
