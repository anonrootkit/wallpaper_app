package com.yash.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.yash.wallpaper.R;

import java.util.List;

public class GalleryAdapter extends ArrayAdapter<String> {


    public GalleryAdapter(@NonNull Context context, List<String> wallpaperUrls) {
        super(context, 0, wallpaperUrls);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String url = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_item, parent, false);
        }
        ImageView wallpaper = convertView.findViewById(R.id.wallpaper);
        Glide.with(getContext()).load(url).override(Target.SIZE_ORIGINAL).into(wallpaper);

        return convertView;
    }


}
