package ru.ifmo.acm.backend.graphics;

import java.awt.*;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public abstract class Graphics {
    protected int x, y, width, height;
    protected Font font;
    protected Color color;

    public abstract Graphics create();

    public abstract Graphics create(int x, int y, int width, int height);

    public abstract void drawRect(int x, int y, int width, int height, Color color, double opacity, boolean italic);

    public void drawRect(int x, int y, int width, int height, Color color, double opacity) {
        drawRect(x, y, width, height, color, opacity);
    }

    public enum Position {
        POSITION_LEFT,
        POSITION_CENTER,
        POSITION_RIGHT
    }

    public abstract void drawRectWithText(String text, int x, int y, int width, int height, Position position, Font font,
                                          Color color, Color textColor, double opacity, double textOpacity,
                                          double margin, boolean italic, boolean scale);

    public abstract void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color,
                                          double opacity, double margin);

    public abstract void drawStar(int x, int y, int size, Color color);

    public void setFont(Font font) {
        this.font = font;
    }

    public void clip(int x, int y, int width, int height) {
        this.x += x;
        this.y += y;
        this.width = width;
        this.height = height;
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
    }

    public abstract void init();

    public abstract void dispose();

}
