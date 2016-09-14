package io.ourglass.amstelbright2.tvui;

/**
 * Created by mkahn on 5/11/16.
 */
public class Frame {
    public Point location;
    public WidthHeight size;

    public Frame(float x, float y, float width, float height) {

        location = new Point(x, y);
        size = new WidthHeight(width, height);

    }
}
