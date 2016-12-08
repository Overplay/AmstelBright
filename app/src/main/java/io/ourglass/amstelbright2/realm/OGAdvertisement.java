package io.ourglass.amstelbright2.realm;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import io.ourglass.amstelbright2.core.OGConstants;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ethan on 8/24/16.
 */

public class OGAdvertisement extends RealmObject{
    private String text1;
    private String text2;
    private String text3;

    //todo get rid of these fields because the images are being stored directly on disk
    private byte[] crawlerImg;
    private byte[] widgetImg;

    //needed to make these strings (of the path to the file) because realm doesn't allow storage of files
    private String crawlerImgFileLoc;
    private String widgetImgFileLoc;

    private String crawlerURL;
    private String widgetURL;

    @PrimaryKey
    private String id;

    public OGAdvertisement(){

    }

    public OGAdvertisement(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    //TODO this is shite. Gson?
    public JSONObject adAsJSON() {
        JSONObject toReturn = new JSONObject();

        JSONArray arr = new JSONArray();
        if(text1 != null) arr.put(text1);
        if(text2 != null) arr.put(text2);
        if(text3 != null) arr.put(text3);

        try {
            toReturn.put("textAds", arr);
            if(crawlerImg != null) {
                toReturn.put("crawlerUrl", crawlerURL);
                //toReturn.put("crawlerImg_location", crawlerImgFileLoc);
            }
            if(widgetImg != null) {
                toReturn.put("widgetUrl", widgetURL);
                //toReturn.put("widgetImg_location", widgetImgFileLoc);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    /**
     * method to convert byte array (crawler and widget images) to base64 image encoded string
     * @param arr bytes to convert to base64 string
     * @return base 64 encoded string with image header
     */
    private String encodeByteArrAsImg(byte[] arr){
        StringBuilder bobTheBuilder = new StringBuilder();
        bobTheBuilder.append("data:image/png;base64,");
        bobTheBuilder.append(Base64.encodeToString(arr, Base64.NO_WRAP));
        return bobTheBuilder.toString();
    }

    /**
     * sets the appropriate text advertisement based on what has already been populated
     * @param toSet
     */
    public void setNextText(String toSet){
        if(toSet != null){
            if(text1 == null){
                text1 = toSet;
            }
            else if(text2 == null){
                text2 = toSet;
            }
            else if(text3 == null){
                text3 = toSet;
            }
        }
    }

    /**
     * sets the byte[] for the advertisment (probably not needed right now, but will leave in temporarily
     * then stores this image to a file in memory and stores references to that file in this object
     * @param img The widget image as bytes
     */
    public void setWidgetImg(byte[] img) {
        if (this.widgetImg != null) {
            Log.w("OGAdvertisement", "Widget image is already set, cannot be set again");
            return;
        }
        this.widgetImg = img;
        final String fileName = "/widget.png";

        File widgetImageFile = storeImage(this.widgetImg, fileName);
        if(widgetImageFile != null) {
            this.widgetImgFileLoc = widgetImageFile.getAbsolutePath();
            this.widgetURL = OGConstants.EXTERNAL_PATH_TO_MEDIA + this.id + fileName;
        }
        else {
            //todo figure out why this just caused the app to crash for the first time out of the blue
            Log.w("OGAdvertisement", "There was a null pointer exception that caused a crash earlier, need to figure out what is going on");
        }
    }

    /**
     * sets the byte[] for the advertisment (probably not needed right now, but will leave in temporarily
     * then stores this image to a file in memory and stores references to that file in this object
     * @param img The crawler image as bytes
     */
    public void setCrawlerImg(byte[] img){
        if(this.crawlerImg != null){
            Log.w("OGAdvertisement", "Crawler image is already set, cannot be set again");
            return;
        }
        this.crawlerImg = img;
        final String fileName = "/crawler.png";

        File crawlerImageFile = storeImage(this.crawlerImg, fileName);
        this.crawlerImgFileLoc = crawlerImageFile.getAbsolutePath();
        this.crawlerURL = OGConstants.EXTERNAL_PATH_TO_MEDIA + this.id + fileName;
    }

    /**
     * helper function to open a file inside of where media is stored for the given advertisement
     * (it will create the file and all nonexistent parent directories if needed)
     * once file exists, write the contents of the image to this file
     * @param img the bytes which constitute the image
     * @param imageName The name of the image, either "/crawler.png" or "/widget.png" depending
     * @return File reference for the written image
     */
    private File storeImage(byte[] img, String imageName){
        File newFile = new File(OGConstants.INTERNAL_PATH_TO_MEDIA + this.id + imageName);
        if(!newFile.exists()) {
            newFile.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            newFile.createNewFile();
            fos = new FileOutputStream(newFile);
            fos.write(img);
        } catch(Exception e){
            return null;
        } finally {
            try {
                if(fos != null) fos.close();
            } catch(Exception e){}
        }
        return newFile;
    }

    public String getWidgetImgFileLoc(){
        return this.widgetImgFileLoc;
    }

    public String getCrawlerImgFileloc(){
        return this.crawlerImgFileLoc;
    }
}
