package com.yash.wallpaper.fragment;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yash.wallpaper.R;
import com.yash.wallpaper.adapter.GalleryAdapter;
import com.yash.wallpaper.communication.CommunicationHelper;
import com.yash.wallpaper.model.Wallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PersonalCollectionFragment extends Fragment implements AddBottomSheet.PersonalWallpaperOptionsListener, ValueEventListener, AbsListView.OnScrollListener {

    private static int loadLimit = 5;
    private GridView gridView;
    private TextView topBanner;
    private GalleryAdapter adapter;
    private RelativeLayout noWallpaperFound;
    private List<String> wallpaperUrls;
    private DatabaseReference userDbRef;
    private FirebaseDatabase firebaseDatabase;
    private ValueEventListener userCollectionDbListener;
    private boolean flag_loading;
    private List<Wallpaper> wallpaperList;
    private ImageView wallpaper;
    private int wallpaperPosition;
    private String path;
    private int screenWidth;
    private int screenHeight;

    public PersonalCollectionFragment() {
    }

    public static PersonalCollectionFragment newInstance() {
        PersonalCollectionFragment fragment = new PersonalCollectionFragment();
        return fragment;
    }

    private void setWallpaperUrls() {
        wallpaperUrls.clear();
        for (Wallpaper wallpaper : wallpaperList) {
            wallpaperUrls.add(wallpaper.getImageUri());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_collection, container, false);
        flag_loading = false;
        init(view);
        gridView.setOnScrollListener(this);
        initItemClickListener();
        return view;
    }

    private void init(View view) {
        topBanner = view.findViewById(R.id.your_wallpapers_banner);
        gridView = view.findViewById(R.id.personal_collection_grid_view);
        noWallpaperFound = view.findViewById(R.id.no_wallpaper_found);
        wallpaperUrls = new ArrayList<>();
        wallpaperList = new ArrayList<>();
        adapter = new GalleryAdapter(getContext(), wallpaperUrls);
        gridView.setAdapter(adapter);
    }

    private void initItemClickListener() {
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            wallpaper = view.findViewById(R.id.wallpaper);
            wallpaperPosition = position;
            AddBottomSheet bottomSheet = new AddBottomSheet(true);
            bottomSheet.addListener(PersonalCollectionFragment.this);
            bottomSheet.show(getParentFragmentManager(), "Show bottom sheet");
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference().child(CommunicationHelper.getUserCollectionDbRef());
        loadWallpaperUrls();
    }

    private void loadWallpaperUrls() {
        userCollectionDbListener = this;
        loadLimit += 5;
        userDbRef.limitToFirst(loadLimit).addValueEventListener(userCollectionDbListener);
    }

    private void hideNoWallpaperBanner() {
        topBanner.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.VISIBLE);
        noWallpaperFound.setVisibility(View.GONE);
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
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getContext());
        myWallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight);
        Intent intent = myWallpaperManager.getCropAndSetWallpaperIntent(getContentTypeImagePath(path));
        getContext().startActivity(intent);
    }

    private Uri getContentTypeImagePath(String path) {
        return FileProvider.getUriForFile(getContext(), "com.yash.wallpaper.fileprovider", new File(path));
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
                        Toast.makeText(getContext(), "Wallpaper saved", Toast.LENGTH_SHORT).show();
                        break;
                    case CommunicationHelper.SET_WALLPAPER:
                        setWallpaper(path);
                        break;
                    case CommunicationHelper.SHARE_WALLPAPER:
                        share();
                        break;
                }
            } else {
                Toast.makeText(getContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Fetching wallpaper...", Toast.LENGTH_SHORT).show();
        }

    }

    private void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivity(intent);
    }

    private String saveImageInMemory(Bitmap resource) {
        try {
            String savedImagePath = null;
            String imageFileName = wallpaperList.get(wallpaperPosition).getWallpaperId() + ".jpg";
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


    @Override
    public void removeImage() {
        try {
            Wallpaper wallpaper = wallpaperList.get(wallpaperPosition);
            StorageReference wallpaperImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(wallpaper.getImageUri());
            userDbRef.child(wallpaper.getWallpaperId()).removeValue();
            wallpaperImageRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Wallpaper removed", Toast.LENGTH_SHORT).show()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
                }
            });
            removeImageFromCategory(wallpaper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeImageFromCategory(Wallpaper wallpaper) {
        DatabaseReference categoryDbRef = firebaseDatabase.getReference().child(CommunicationHelper.getCategoryDbRef(wallpaper.getCategory()));
        categoryDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        key = snapshot.getKey();
                        String imageUri = (String) snapshot.getValue();
                        if (imageUri.equals(wallpaper.getImageUri())) {
                            categoryDbRef.child(key).removeValue();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            hideNoWallpaperBanner();
            if (dataSnapshot.getChildrenCount() != wallpaperList.size()) {
                wallpaperList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Wallpaper wallpaper = snapshot.getValue(Wallpaper.class);
                    wallpaper.setWallpaperId(snapshot.getKey());
                    wallpaperList.add(wallpaper);
                }

                setWallpaperUrls();
                adapter.notifyDataSetChanged();
                flag_loading = false;
            }
        } else {
            showNoWallpaperBanner();
        }
    }

    private void showNoWallpaperBanner() {
        topBanner.setVisibility(View.GONE);
        gridView.setVisibility(View.GONE);
        noWallpaperFound.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (wallpaperList.size() > 0) {
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && visibleItemCount >= CommunicationHelper.getVisibleItems()) {
                if (!flag_loading) {
                    flag_loading = true;
                    loadWallpaperUrls();
                }
            }
        }
    }
}
