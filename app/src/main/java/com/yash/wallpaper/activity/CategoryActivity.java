package com.yash.wallpaper.activity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yash.wallpaper.R;
import com.yash.wallpaper.adapter.GalleryAdapter;
import com.yash.wallpaper.communication.CommunicationHelper;
import com.yash.wallpaper.fragment.AddBottomSheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements AbsListView.OnScrollListener, AddBottomSheet.PublicWallpaperOptionsListener {

    private Toolbar toolbar;
    private String category;
    private DatabaseReference dbRef;
    private int limit;
    private List<String> wallpaperUrls;
    private GridView wallpapers;
    private GalleryAdapter adapter;
    private boolean flag_loading;
    private RelativeLayout loader;
    private ImageView wallpaper;
    private String path;
    private int screenWidth, screenHeight;
    private TextView noWallpaperBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        init();
        initToolbar();
        wallpapers.setOnScrollListener(this);
        initClickListener();
        loadWallpapers();
    }

    private void initClickListener() {
        wallpapers.setOnItemClickListener((adapterView, view, position, l) -> {
            wallpaper = view.findViewById(R.id.wallpaper);
            AddBottomSheet bottomSheet = new AddBottomSheet(false);
            bottomSheet.addListener(this);
            bottomSheet.show(getSupportFragmentManager(), "Show bottom sheet");
        });
    }

    private void loadWallpapers() {
        limit += 5;
        dbRef.limitToFirst(limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    hideLoader();
                    showWallpapers();
                    hideNoWallpaperbanner();
                    wallpaperUrls.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren())
                            wallpaperUrls.add((String) snapshot1.getValue());
                    }
                    adapter.notifyDataSetChanged();
                    flag_loading = false;
                } else {
                    showNoWallpapersBanner();
                    hideLoader();
                    hideWallpapers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void hideWallpapers() {
        wallpapers.setVisibility(View.GONE);
    }

    private void showWallpapers() {
        wallpapers.setVisibility(View.VISIBLE);
    }

    private void showNoWallpapersBanner() {
        noWallpaperBanner.setVisibility(View.VISIBLE);
    }

    private void hideNoWallpaperbanner() {
        noWallpaperBanner.setVisibility(View.GONE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
    }

    private void init() {
        limit = 10;
        Intent intent = getIntent();
        int intentExtra = intent.getIntExtra("category", 0);
        category = CommunicationHelper.WALLPAPER_CATEGORIES[intentExtra];
        toolbar = findViewById(R.id.category_toolbar);
        dbRef = FirebaseDatabase.getInstance().getReference(CommunicationHelper.getCategoryDbRef(intentExtra));
        wallpaperUrls = new ArrayList<>();
        wallpapers = findViewById(R.id.category_wallpapers);
        adapter = new GalleryAdapter(this, wallpaperUrls);
        wallpapers.setAdapter(adapter);
        loader = findViewById(R.id.loading_category_wallpaper);
        noWallpaperBanner = findViewById(R.id.category_no_wallpapers_banner);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        getSupportActionBar().setTitle(category);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (wallpaperUrls.size() > 0) {
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && visibleItemCount >= CommunicationHelper.getVisibleItems()) {
                if (!flag_loading) {
                    flag_loading = true;
                    loadWallpapers();
                }
            }
        }
    }


    @Override
    public void loadImage(int flag) {
        Bitmap bitmap;
        try {
            bitmap = ((BitmapDrawable) wallpaper.getDrawable()).getBitmap();
            path = saveImageInMemory(bitmap);
            if (path != null) {
                switch (flag) {
                    case CommunicationHelper.SAVE_WALLPAPER:
                        Toast.makeText(this, "Wallpaper saved", Toast.LENGTH_SHORT).show();
                        break;
                    case CommunicationHelper.SET_WALLPAPER:
                        setWallpaper(path);
                        break;
                    case CommunicationHelper.SHARE_WALLPAPER:
                        share();
                        break;
                }
            } else {
                Toast.makeText(this, "Some error occurred!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Fetching wallpaper...", Toast.LENGTH_SHORT).show();
        }

    }

    private String saveImageInMemory(Bitmap resource) {
        try {
            String savedImagePath = null;
            String imageFileName = Calendar.getInstance().getTimeInMillis() + ".jpg";
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Wallpapers");
            boolean success = true;
            if (!storageDir.exists()) {
                success = storageDir.mkdirs();
            }
            if (success) {
                File imageFile = new File(storageDir, imageFileName);
                savedImagePath = imageFile.getAbsolutePath();
                try {
                    OutputStream fOut = new FileOutputStream(imageFile);
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return savedImagePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivity(intent);
    }

    private void setDisplayDimensions() {
        DisplayMetrics displaymetrics = Resources.getSystem().getDisplayMetrics();
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;
    }

    private void setWallpaper(String path) {
        try {
            setDisplayDimensions();
            openWallpaperIntent(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openWallpaperIntent(String path) {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(this);
        myWallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight);
        Intent intent = myWallpaperManager.getCropAndSetWallpaperIntent(getContentTypeImagePath(path));
        startActivity(intent);
    }

    private Uri getContentTypeImagePath(String path) {
        return FileProvider.getUriForFile(this, "com.yash.wallpaper.fileprovider", new File(path));
    }

}
