package io.ourglass.amstelbright2.tvui;

/**
 * Created by mkahn on 5/11/16.
 */
public class WidthHeight {

    public float width;
    public float height;

    public WidthHeight(float width, float height){
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString(){
        return this.width+"x"+this.height;
    }
}
