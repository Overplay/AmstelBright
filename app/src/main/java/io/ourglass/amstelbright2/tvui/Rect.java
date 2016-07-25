package io.ourglass.amstelbright2.tvui;

/**
 * Created by mkahn on 5/11/16.
 */
public class Rect {

    public float width;
    public float height;

    public Rect(float width, float height){
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString(){
        return this.width+"x"+this.height;
    }
}
