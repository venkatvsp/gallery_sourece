package com.example.venkata.codechallenge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by venka on 6/15/2017.
 */

public class PhotoActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private String mPhotopath;
    private RecyclerView mRecyclerView;
    private ArrayList<PhotoHolder> mPhotosList;
    private ArrayList<PhotoHolder> mTempPhotosList;
    private PhotoListAdapter mPhotoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        titleText.setText(getIntent().getStringExtra("title"));
        mPhotopath = getIntent().getStringExtra("photopath");

        mRecyclerView = (RecyclerView) findViewById(R.id.albumlist);
        LinearLayoutManager mListLayoutManager = new LinearLayoutManager(PhotoActivity.this);
        mListLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mListLayoutManager);
        mPhotosList = new ArrayList<>();
        mTempPhotosList = new ArrayList<>();
        mPhotoListAdapter = new PhotoListAdapter(R.layout.photo_card, mPhotosList);
        mRecyclerView.setAdapter(mPhotoListAdapter);
        new ImportPhotoTask().execute(1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImportPhotoTask extends AsyncTask<Integer, Integer, Integer> {

        int mtotal = 0;

        protected Integer doInBackground(Integer... value) {
            mTempPhotosList.clear();
            if (TextUtils.isEmpty(mPhotopath)) {
                return 0;
            }
            File f = new File(mPhotopath);
            if (f.exists()) {
                File file[] = f.listFiles();
                mtotal = file.length;
                for (File album : file) {
                    String fname = album.getName();
                    String fpath = album.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) options.inPurgeable = true;
                    Bitmap bitmap;
                    bitmap = BitmapFactory.decodeFile(fpath, options);
                    ExifInterface exif = null;
                    try
                    {
                        exif = new ExifInterface(fpath);
                    }
                    catch (IOException e)
                    {
                        //Error
                        e.printStackTrace();
                    }
                    String orientString = exif != null ? exif.getAttribute(ExifInterface.TAG_ORIENTATION): null;
                    int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

                    int rotationAngle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

                    Matrix matToDet = new Matrix();
                    matToDet.postRotate(rotationAngle);
                    Bitmap rotatedBmp;
                    rotatedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matToDet, true);
                    PhotoHolder lPhotoHolder = new PhotoHolder();
                    lPhotoHolder.setImage(rotatedBmp);
                    lPhotoHolder.setTitle(fname);
                    lPhotoHolder.setImagepath(fpath);
                    mTempPhotosList.add(lPhotoHolder);
                }
            }
            return mtotal;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mPhotosList.clear();
            mPhotosList.addAll(mTempPhotosList);
            mTempPhotosList.clear();
            mPhotoListAdapter.notifyDataSetChanged();
        }
    }
}
