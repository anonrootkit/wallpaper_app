package com.yash.wallpaper.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yash.wallpaper.R;
import com.yash.wallpaper.activity.CategoryActivity;
import com.yash.wallpaper.communication.CommunicationHelper;


public class AddBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    private int id;
    private PublicWallpaperOptionsListener publicWallpaperOptionsListener;
    private PersonalWallpaperOptionsListener personalWallpaperOptionsListener;
    private CategoryActivity categoryActivityListener;
    private boolean isRemoveVisible;

    public AddBottomSheet(boolean isRemoveVisible){
        this.isRemoveVisible = isRemoveVisible;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.wallpaper_bottom_sheet, container,
                false);

    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.set_wallpaper).setOnClickListener(this);
        view.findViewById(R.id.save_wallpaper).setOnClickListener(this);
        view.findViewById(R.id.share_wallpaper).setOnClickListener(this);
        if (isRemoveVisible){
            view.findViewById(R.id.remove_wallpaper).setVisibility(View.VISIBLE);
            view.findViewById(R.id.remove_wallpaper).setOnClickListener(this);
        }else {
            view.findViewById(R.id.remove_wallpaper).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        id = view.getId();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CommunicationHelper.WRITE_ACCESS_PERMISSION_CODE);
        }else {

            startOnClickProcess(id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == CommunicationHelper.WRITE_ACCESS_PERMISSION_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startOnClickProcess(id);
            } else {
                Toast.makeText(getContext(), "Can't continue without permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startOnClickProcess(int id){

        if (personalWallpaperOptionsListener != null){
            switch (id){
                case R.id.save_wallpaper:
                    personalWallpaperOptionsListener.loadImage(CommunicationHelper.SAVE_WALLPAPER);
                    break;
                case R.id.set_wallpaper:
                    personalWallpaperOptionsListener.loadImage(CommunicationHelper.SET_WALLPAPER);
                    break;
                case R.id.share_wallpaper:
                    personalWallpaperOptionsListener.loadImage(CommunicationHelper.SHARE_WALLPAPER);
                    break;
                case R.id.remove_wallpaper:
                    personalWallpaperOptionsListener.removeImage();
                    break;
            }
        }else if (publicWallpaperOptionsListener != null){
            switch (id){
                case R.id.save_wallpaper:
                    publicWallpaperOptionsListener.loadImage(CommunicationHelper.SAVE_WALLPAPER);
                    break;
                case R.id.set_wallpaper:
                    publicWallpaperOptionsListener.loadImage(CommunicationHelper.SET_WALLPAPER);
                    break;
                case R.id.share_wallpaper:
                    publicWallpaperOptionsListener.loadImage(CommunicationHelper.SHARE_WALLPAPER);
                    break;
            }
        }else {
            switch (id) {
                case R.id.save_wallpaper:
                    categoryActivityListener.loadImage(CommunicationHelper.SAVE_WALLPAPER);
                    break;
                case R.id.set_wallpaper:
                    categoryActivityListener.loadImage(CommunicationHelper.SET_WALLPAPER);
                    break;
                case R.id.share_wallpaper:
                    categoryActivityListener.loadImage(CommunicationHelper.SHARE_WALLPAPER);
                    break;
            }
        }
        dismiss();
    }

    void addListener(PublicWallpaperFragment publicWallpaperFragment) {
        publicWallpaperOptionsListener = publicWallpaperFragment;
    }

    void addListener(PersonalCollectionFragment personalCollectionFragment) {
        personalWallpaperOptionsListener = personalCollectionFragment;
    }

    public void addListener(CategoryActivity categoryActivity) {
        categoryActivityListener = categoryActivity;
    }

    public interface PublicWallpaperOptionsListener{
        void loadImage(int flag);
    }

    interface PersonalWallpaperOptionsListener{
        void loadImage(int flag);
        void removeImage();
    }

}
