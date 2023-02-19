package project.cspaint;

import android.graphics.Path;

public class Stroke {
    // colour
    public int color;

    // width
    public int strokeWidth;

    // path drawn
    public Path path;

    // constructor
    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
