package com.example.venkata.codechallenge;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<PhotoHolder> mAlbumList;
    private ArrayList<PhotoHolder> mTempAlbumList;
    private PhotoListAdapter mPhotoListAdapter;
    private PopupWindow mPopupWindow;
    private Handler mHandler;
    private GalleryPagerAdapter mGalleryPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        setTitle(getString(R.string.albums));
        mRecyclerView = (RecyclerView) findViewById(R.id.albumlist);
        LinearLayoutManager mListLayoutManager = new LinearLayoutManager(AlbumActivity.this);
        mListLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mListLayoutManager);
        mAlbumList = new ArrayList<>();
        mTempAlbumList = new ArrayList<>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1001) {
                    mPhotopath = msg.getData().getString("photopath");
                    hidePopup();
                    showFullViewPopup();
                }
            }
        };
        mPhotoListAdapter = new PhotoListAdapter(R.layout.album_card, mAlbumList);
        mPhotoListAdapter.setCallBackHandler(mHandler);
        mRecyclerView.setAdapter(mPhotoListAdapter);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        } else {
            new ImportAlbumTask().execute(1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length <= 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Please allow app to access external storage",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                new ImportAlbumTask().execute(1);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showFullViewPopup() {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_viewpager, null);

        ViewPager viewPager = (ViewPager) popupView.findViewById(R.id.galleryview);

        mPhotosList = new ArrayList<>();
        mTempPhotosList = new ArrayList<>();
        mGalleryPagerAdapter = new GalleryPagerAdapter(mPhotosList, AlbumActivity.this);
        viewPager.setAdapter(mGalleryPagerAdapter);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int) (size.x*0.90F);
        int height = (int) (size.y*0.60F);
        int x = (size.x - width)/2;
        int y = (size.y - height)/2;
        mPopupWindow = new PopupWindow(popupView, width, height);
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopupWindow = null;
            }
        });
        mPopupWindow.showAtLocation(findViewById(R.id.albumlist), Gravity.NO_GRAVITY, x, y);

        new ImportPhotoTask().execute(1);
    }

    public void hidePopup() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    private final static String DIR = "/Download/Assets";

    private class ImportAlbumTask extends AsyncTask<Integer, Integer, Integer> {

        int mtotal = 0;

        protected Integer doInBackground(Integer... value) {
            mTempAlbumList.clear();
            String path = Environment.getExternalStorageDirectory().toString() + DIR;
            File f = new File(path);
            if (f.exists()) {
                File file[] = f.listFiles();
                mtotal = file.length;
                for (File album : file) {
                    if (album.listFiles().length > 0) {
                        String fname = album.getName();
                        String fpath = album.getAbsolutePath();
                        File[] photos = album.listFiles();
                        String tilePath = photos[0].getAbsolutePath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) options.inPurgeable = true;
                        Bitmap bitmap;
                        bitmap = BitmapFactory.decodeFile(tilePath, options);
                        ExifInterface exif = null;
                        try
                        {
                            exif = new ExifInterface(tilePath);
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

                        if(bitmap != null) {
                            rotatedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matToDet, true);
                            PhotoHolder lPhotoHolder = new PhotoHolder();
                            lPhotoHolder.setImage(rotatedBmp);
                            lPhotoHolder.setTitle(fname);
                            lPhotoHolder.setImagepath(fpath);
                            mTempAlbumList.add(lPhotoHolder);
                        }

                    }
                }
            }
            return mtotal;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mAlbumList.clear();
            mAlbumList.addAll(mTempAlbumList);
            mTempAlbumList.clear();
            mPhotoListAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<PhotoHolder> mPhotosList;
    private ArrayList<PhotoHolder> mTempPhotosList;
    private String mPhotopath;

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
            mGalleryPagerAdapter.notifyDataSetChanged();
        }
    }

}
