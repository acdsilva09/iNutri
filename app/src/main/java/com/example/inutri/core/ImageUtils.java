package com.example.inutri.core;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilitários para lidar com imagens/URIs:
 * - Ler Bitmap de Uri com redimensionamento
 * - Corrigir rotação via EXIF
 * - Salvar bitmap no cache e obter Uri via FileProvider
 * - Converter Uri/Bitmap em bytes
 */
public final class ImageUtils {

    private ImageUtils() {}

    /** Lê um Bitmap de um Uri, redimensionando para o lado maior em px (mantém proporção). */
    public static Bitmap decodeBitmapFromUri(@NonNull Context ctx, @NonNull Uri uri, int maxSidePx) throws Exception {
        // 1) bounds
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, bounds);
        }

        int srcW = bounds.outWidth;
        int srcH = bounds.outHeight;
        if (srcW <= 0 || srcH <= 0) {
            // fallback simples
            try (InputStream is2 = ctx.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(is2);
            }
        }

        int sample = 1;
        int maxSrcSide = Math.max(srcW, srcH);
        while (maxSrcSide / sample > maxSidePx) sample *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = Math.max(1, sample);
        Bitmap bmp;
        try (InputStream is3 = ctx.getContentResolver().openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(is3, null, opts);
        }

        // 2) rotação por EXIF
        return rotateBitmapIfRequired(ctx, bmp, uri);
    }

    /** Corrige rotação do Bitmap com base no EXIF do arquivo apontado pelo Uri. */
    public static Bitmap rotateBitmapIfRequired(@NonNull Context ctx, @NonNull Bitmap bitmap, @NonNull Uri uri) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            if (is != null) {
                ExifInterface exif = new ExifInterface(is);
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (Exception ignored) { }

        int rotation = exifToDegrees(orientation);
        if (rotation == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) bitmap.recycle();
        return rotated;
    }

    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:  return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    /** Salva um Bitmap como JPEG no cache interno e retorna um Uri do FileProvider. */
    public static Uri saveBitmapToCacheProvider(@NonNull Context ctx,
                                                @NonNull Bitmap bitmap,
                                                @NonNull String fileName,
                                                @NonNull String fileProviderAuthority) throws Exception {
        File dir = new File(ctx.getCacheDir(), "images");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, fileName.endsWith(".jpg") ? fileName : (fileName + ".jpg"));

        try (OutputStream os = new java.io.FileOutputStream(out)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, os);
            os.flush();
        }
        return FileProvider.getUriForFile(ctx, fileProviderAuthority, out);
    }

    /** Copia um Uri para o cache e retorna o arquivo de destino. Útil para upload. */
    public static File copyUriToCache(@NonNull Context ctx, @NonNull Uri uri, @NonNull String fileNameHint) throws Exception {
        File dir = new File(ctx.getCacheDir(), "images");
        if (!dir.exists()) dir.mkdirs();
        String name = (fileNameHint == null || fileNameHint.isEmpty()) ? getDisplayName(ctx.getContentResolver(), uri) : fileNameHint;
        if (name == null || name.isEmpty()) name = "image_" + System.currentTimeMillis() + ".jpg";

        File out = new File(dir, name);
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             OutputStream os = new java.io.FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) os.write(buf, 0, r);
            os.flush();
        }
        return out;
    }

    /** Lê todos os bytes de um Uri (cuidado com arquivos grandes). */
    public static byte[] readBytes(@NonNull Context ctx, @NonNull Uri uri) throws Exception {
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
            return bos.toByteArray();
        }
    }

    /** Obtém o display name (nome sugerido) de um Uri (quando disponível). */
    public static String getDisplayName(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        Cursor c = null;
        try {
            c = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            closeQuietly(c);
        }
        return null;
    }

    private static void closeQuietly(Closeable c) {
        try { if (c != null) c.close(); } catch (Exception ignored) {}
    }
}
