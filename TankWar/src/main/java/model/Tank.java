package main.java.model;

import main.java.TankClient;
import main.java.comm.MissileNewMsg;
import main.java.comm.TankMoveMsg;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

public class Tank {
    private static int WIDTH = 30;
    private static int HEIGHT = 30;
    private static int YSPEED = 5;
    private static int XSPEED = 5;
    private static Random r = new Random();
    private static Color[] colors = {Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.ORANGE};
    private int id;
    private int posX;
    private int posY;
    private Direction direction = Direction.STOP;
    private Direction canonDirection = Direction.D;
    private boolean left, up, right, down;
    private TankClient tankClient;
    private boolean live = true;
    private Color color;

    public Tank(int posX, int posY, Direction direction, TankClient tankClient) {
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
        this.tankClient = tankClient;
        this.color = colors[r.nextInt(colors.length)];
    }

    public Tank(int id, int posX, int posY, Direction direction, TankClient tankClient) {
        this(posX, posY, direction, tankClient);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public Color getColor() {
        return color;
    }

    public int getColorIndex() {
        return Arrays.asList(colors).indexOf(this.color);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setCanonDirection(Direction canonDirection) {
        this.canonDirection = canonDirection;
    }

    public void draw(Graphics g) {
        if (!live) return;

        Color c = g.getColor();
        g.setColor(this.color);
        g.fillRect(posX, posY, WIDTH, HEIGHT);
        g.drawString(String.valueOf(id), posX, posY - 2);
        g.setColor(c);

        switch (canonDirection) {
            case L:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX, posY + HEIGHT / 2);
                break;
            case LU:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX, posY);
                break;
            case U:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX + WIDTH / 2, posY);
                break;
            case RU:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX + WIDTH, posY);
                break;
            case R:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX + WIDTH, posY + HEIGHT / 2);
                break;
            case RD:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX + WIDTH, posY + HEIGHT);
                break;
            case D:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX + WIDTH / 2, posY + HEIGHT);
                break;
            case LD:
                g.drawLine(posX + WIDTH / 2, posY + HEIGHT / 2, posX, posY + HEIGHT);
                break;
        }

        move();
    }

    private void move() {
        switch (direction) {
            case L:
                posX -= XSPEED;
                break;
            case LU:
                posX -= XSPEED;
                posY -= YSPEED;
                break;
            case U:
                posY -= YSPEED;
                break;
            case RU:
                posX += XSPEED;
                posY -= YSPEED;
                break;
            case R:
                posX += XSPEED;
                break;
            case RD:
                posX += XSPEED;
                posY += YSPEED;
                break;
            case D:
                posY += YSPEED;
                break;
            case LD:
                posX -= XSPEED;
                posY += YSPEED;
                break;
            case STOP:
                break;
        }

        if (direction != Direction.STOP) canonDirection = direction;

        if (posX < 0) posX = 0;
        if (posY < 30) posY = 30;
        if (posX + WIDTH > TankClient.GAME_WIDTH) posX = TankClient.GAME_WIDTH - WIDTH;
        if (posY + HEIGHT > TankClient.GAME_HEIGHT) posY = TankClient.GAME_HEIGHT - HEIGHT;
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
                break;
        }
        locateDirection();
    }

    private void locateDirection() {
        Direction oldDirection = this.direction;

        if (left && !up && !right && !down)
            direction = Direction.L;
        else if (left && up && !right && !down)
            direction = Direction.LU;
        else if (!left && up && !right && !down)
            direction = Direction.U;
        else if (!left && up && right && !down)
            direction = Direction.RU;
        else if (!left && !up && right && !down)
            direction = Direction.R;
        else if (!left && !up && right && down)
            direction = Direction.RD;
        else if (!left && !up && !right && down)
            direction = Direction.D;
        else if (left && !up && !right && down)
            direction = Direction.LD;
        else if (!left && !up && !right && !down)
            direction = Direction.STOP;

        if (direction != oldDirection) {
            TankMoveMsg msg = new TankMoveMsg(id, posX, posY, direction, canonDirection);
            tankClient.netClient.send(msg);
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                fire();
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
        }
        locateDirection();
    }

    private void fire() {
        if (!live) return;

        int x = this.posX + WIDTH / 2 - Missile.WIDTH / 2;
        int y = this.posY + HEIGHT / 2 - Missile.HEIGHT / 2;
        Missile m = new Missile(this.id, x, y, this.canonDirection, this.tankClient);
        tankClient.missiles.add(m);

        MissileNewMsg msg = new MissileNewMsg(m);
        tankClient.netClient.send(msg);
    }

    public Rectangle getRect() {
        return new Rectangle(posX, posY, WIDTH, HEIGHT);
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
