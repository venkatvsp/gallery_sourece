package com.example.venkata.codechallenge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by venka on 6/15/2017.
 */

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {
    private ArrayList<PhotoHolder> mDataset;
    private int mResID;
    private Handler mHandler;

    public void setCallBackHandler(Handler handler) {
        mHandler = handler;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;
        private final ImageView mImage;
        final Context mContext;

        ViewHolder(View v, Context context) {
            super(v);
            mContext = context;
            mTitle = (TextView) v.findViewById(R.id.pictureName);
            mImage = (ImageView) v.findViewById(R.id.pictureData);
        }
    }

    PhotoListAdapter(int resID, ArrayList<PhotoHolder> myDataset) {
        mResID = resID;
        mDataset = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mResID, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTitle.setText(String.format("%s", mDataset.get(position).getTitle()));
        holder.mImage.setImageBitmap(mDataset.get(position).getImage());
        if (mResID == R.layout.album_card) {
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String fpath = mDataset.get(holder.getAdapterPosition()).getImagepath();
                    Intent i = new Intent();
                    i.setAction("com.example.album.gallery.VIEW");
                    i.putExtra("title", mDataset.get(holder.getAdapterPosition()).getTitle());
                    i.putExtra("photopath", fpath);
                    holder.mContext.startActivity(i);
                }
            });
            holder.mImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (mHandler != null) {
                        String fpath = mDataset.get(holder.getAdapterPosition()).getImagepath();
                        Bundle b = new Bundle();
                        b.putString("photopath", fpath);
                        Message m = new Message();
                        m.what = 1001;
                        m.setData(b);
                        mHandler.sendMessage(m);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
