package com.miguel_lm.socialmedia.utils;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import id.zelory.compressor.Compressor;


public class CompressorBitmapImage {

    //Metodo que permite comprimir imagenes y transformarlas a bitmap
    public static byte[] getImage(Context ctx, String path, int width, int height) {
        final File file_thumb_path = new File(path);
        Bitmap thumb_bitmap = null;

        try {
            thumb_bitmap = new Compressor(ctx)
                    .setMaxWidth(width)
                    .setMaxHeight(height)
                    .setQuality(75)
                    .compressToBitmap(file_thumb_path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Objects.requireNonNull(thumb_bitmap).compress(Bitmap.CompressFormat.JPEG,80,baos);

        return baos.toByteArray();
    }
}
