package com.example.venkata.codechallenge;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by venka on 6/15/2017.
 */

public class GalleryPagerAdapter  extends PagerAdapter {

    private ArrayList<PhotoHolder> photoHolders;
    private LayoutInflater inflater;

    public GalleryPagerAdapter(ArrayList<PhotoHolder> photoHolders, Context context) {
        this.photoHolders = photoHolders;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (photoHolders != null) {
            return photoHolders.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        final PhotoHolder photoHolder = photoHolders.get(position);

        View photoLayout = inflater.inflate(R.layout.photo_card,
                view, false);

        TextView mTitle = (TextView) photoLayout.findViewById(R.id.pictureName);
        ImageView mImage = (ImageView) photoLayout.findViewById(R.id.pictureData);

        mTitle.setText(String.format("%s", photoHolder.getTitle()));
        mImage.setImageBitmap(photoHolder.getImage());
        photoLayout.setTag(position);
        view.addView(photoLayout, 0);

        return photoLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
