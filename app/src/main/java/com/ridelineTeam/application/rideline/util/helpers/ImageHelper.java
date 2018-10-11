package com.ridelineTeam.application.rideline.util.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by vale on 17/06/2018.
 */

public class ImageHelper {
    public static byte[] resizeBytesImage(Context context, ImageView uploadPicture, Intent data) {
        final Uri imageUri = data.getData();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploadPicture.setDrawingCacheEnabled(true);
        uploadPicture.buildDrawingCache();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        bmOptions.inSampleSize = 1;
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmOptions.inJustDecodeBounds = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        final byte[] picture = baos.toByteArray();
        return picture;
    }
    public static void setImageViewPicture(ImageView imageView,Context context,String pictureUrl){
        Picasso.with(context).load(pictureUrl).fit().centerCrop()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(pictureUrl).fit().into(imageView);

                    }
                });
    }

}
