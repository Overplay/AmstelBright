package io.ourglass.amstelbright2.services.http.ogutil;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;
import com.sromku.simple.storage.helpers.OrderType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by mkahn on 5/7/16.
 */

// Performs the super-clunky feat of moving the www.zip from assets on to the SDCard

public class AssetExtractor {

    private Context mContext;

    public AssetExtractor(Context context){
        mContext = context;
    }

    public void update(){

        AssetManager assetManager = mContext.getAssets();

        String [] wwwAssets;


        // Punt if no www assets
        try {
            wwwAssets = assetManager.list("www");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (wwwAssets.length==0)
                return;

        moveFromAssetsToExternalSDCard();
        String outPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/";
        unpackZip(outPath, "www.zip");

    }

    private void moveFromAssetsToExternalSDCard() {

        AssetManager assetManager = mContext.getAssets();

        InputStream in = null;
        OutputStream out = null;

        Storage storage = SimpleStorage.getExternalStorage();
        storage.createDirectory("www", true);

        try {
            in = assetManager.open("www/www.zip");

            String outPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/www/";

            File outFile = new File(outPath, "www.zip");

            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file!", e);
        }

        List<File> files = storage.getFiles("www", OrderType.DATE);
        Log.d("B", "yo");
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private boolean unpackZip(String path, String zipname) {

        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Storage storage = SimpleStorage.getExternalStorage();
        List<File> files = storage.getFiles("www", OrderType.DATE);
        Log.d("B", "yo");
        return true;
    }



}
