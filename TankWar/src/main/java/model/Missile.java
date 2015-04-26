package main.java.model;

import main.java.TankClient;

import java.awt.*;

public class Missile {
    public static int WIDTH = 10;
    public static int HEIGHT = 10;
    private int YSPEED = 10;
    private int XSPEED = 10;
    private int INIT_ID = 1;
    private int tankID;
    private int id;
    private int x;
    private int y;
    private Direction direction = Direction.R;
    private TankClient tankClient;
    private boolean live = true;
    private Rectangle rectangle;

    public Missile(int tankID, int x, int y, Direction direction, TankClient tankClient) {
        this.tankID = tankID;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.id = INIT_ID++;
        this.tankClient = tankClient;
        this.rectangle = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public int getTankID() {
        return tankID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction getDirection() {
        return direction;
    }

    public void draw(Graphics g) {
        Color c = g.getColor();
        g.setColor(tankClient.tank.getColor());
        g.fillOval(x, y, WIDTH, HEIGHT);
        g.setColor(c);

        move();
    }

    private void move() {
        switch (direction) {
            case L:
                x -= XSPEED;
                break;
            case LU:
                x -= XSPEED;
                y -= YSPEED;
                break;
            case U:
                y -= YSPEED;
                break;
            case RU:
                x += XSPEED;
                y -= YSPEED;
                break;
            case R:
                x += XSPEED;
                break;
            case RD:
                x += XSPEED;
                y += YSPEED;
                break;
            case D:
                y += YSPEED;
                break;
            case LD:
                x -= XSPEED;
                y += YSPEED;
                break;
            case STOP:
                break;
        }

        this.rectangle.setLocation(x, y);

        if (x < 0 || y < 0 || x > TankClient.GAME_WIDTH || y > TankClient.GAME_HEIGHT) {
            this.live = false;
        }
    }

    public boolean hit(Tank tank) {
        if (this.live && this.tankID != tank.getId() && tank.isLive() && this.rectangle.intersects(tank.getRect())) {
            this.live = false;
            tank.setLive(false);
            tankClient.explodes.add(new Explode(x, y));
            return true;
        }

        return false;
    }
}
