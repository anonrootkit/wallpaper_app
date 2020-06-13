package com.yash.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.yash.wallpaper.R;
import com.yash.wallpaper.communication.CommunicationHelper;

public class CategoryAdapter extends ArrayAdapter<String> {

//    private String backgroundUrls[] = {
//            "https://wallpaperaccess.com/full/400179.jpg", //Random
//            "https://c4.wallpaperflare.com/wallpaper/699/872/46/animals-black-cats-dark-wallpaper-preview.jpg", //Animals
//            "https://wallpapercave.com/wp/9qNjlYn.jpg", //Anime
//            "https://www.whoa.in/20140224-Whoa/Katrina-Kaif-Black-Drace-with-Dark-Background-HD-Wallpaper.jpg", //Bollywood
//            "https://wallpaperaccess.com/full/1420498.jpg", //Logos
//            "https://wallpaperplay.com/walls/full/b/5/f/43064.jpg", //Cars
//            "https://thumbs.dreamstime.com/b/seamless-d-geometric-pattern-optical-illusion-black-grey-geometric-background-texture-seamless-d-geometric-pattern-optical-131438820.jpg", //Design
//            "https://wallpapercave.com/wp/wp2446692.jpg", //Drawing
//            "https://wallpaperaccess.com/full/2222747.jpg", //Entertainment
//            "https://wallpaperboat.com/wp-content/uploads/2019/08/Free-download-Cookie-monster-Samsung-Wallpapers-Samsung-Galaxy-S-Galaxy.jpg", //Funny
//            "https://images.wallpapersden.com/image/download/teppen-game_67864_3841x2161.jpg", //Games
//            "https://wallpaperplay.com/walls/full/e/5/f/195590.jpg", //Holidays
//            "https://wallpaperplay.com/walls/full/8/d/9/301486.jpg", //Love
//            "https://c4.wallpaperflare.com/wallpaper/615/275/818/boombox-sound-dark-technology-wallpaper-preview.jpg", //Music
//            "https://wallpapercave.com/wp/u9AVLry.jpg", //Nature
//            "https://tubularinsights.com/wp-content/uploads/2018/08/news-politics-leaderboard-tubular-july-2018.jpg", //News & Politics
//            "https://images.wallpaperscraft.com/image/hourglass_stones_blur_120797_300x188.jpg", //Other
//            "https://images.pexels.com/photos/853168/pexels-photo-853168.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500", //People
//            "https://previews.123rf.com/images/elenka1/elenka11801/elenka1180100512/94428339-floral-pattern-wallpaper-baroque-damask-seamless-vector-background-black-ornament.jpg", //Pattern
//            "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80", //Spiritual
//            "https://images.unsplash.com/photo-1484482340112-e1e2682b4856?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80", //Sport
//            "https://wallpapercave.com/wp/wp2848515.jpg" //Technology
//    };

    private int background[] = {
            R.drawable.category_one,
            R.drawable.category_two,
            R.drawable.category_three,
            R.drawable.category_four,
            R.drawable.category_five,
            R.drawable.category_six,
            R.drawable.category_seven,
            R.drawable.category_eight,
            R.drawable.category_nine,
            R.drawable.category_ten,
            R.drawable.category_eleven,
            R.drawable.category_twelve,
            R.drawable.category_thirteen,
            R.drawable.category_fourteen,
            R.drawable.category_fifteen,
            R.drawable.category_sixteen,
            R.drawable.category_seventeen,
            R.drawable.category_eighteen,
            R.drawable.category_nineteen,
            R.drawable.category_twenty,
            R.drawable.category_twenty_one,
            R.drawable.category_twenty_two
    };

    public CategoryAdapter(@NonNull Context context, String[] list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);

        ImageView categoryBackground = convertView.findViewById(R.id.category_background);
        TextView category = convertView.findViewById(R.id.category_title);
        category.setText(CommunicationHelper.WALLPAPER_CATEGORIES[position]);
        Glide.with(getContext()).load(background[position]).override(95).into(categoryBackground);
        return convertView;
    }
}
