package main.java.model;

import main.java.TankClient;

import java.awt.*;

public class Explode {
    private int x;
    private int y;
    private int step = 0;
    private int[] diameters = {4, 7, 12, 18, 26, 32, 49, 30, 14, 6};

    public Explode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isDone() {
        return step >= diameters.length;
    }

    public void draw(Graphics g) {
        Color c = g.getColor();
        g.setColor(Color.ORANGE);
        g.fillOval(x, y, diameters[step], diameters[step]);
        g.setColor(c);
        step++;
    }
}
