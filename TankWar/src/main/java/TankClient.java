package main.java;

import main.java.comm.MissileDeadMsg;
import main.java.comm.TankDeadMsg;
import main.java.model.Direction;
import main.java.model.Explode;
import main.java.model.Missile;
import main.java.model.Tank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TankClient extends Frame {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 600;

    public Tank tank = new Tank(100, 50, Direction.STOP, this);
    public List<Missile> missiles = new ArrayList<>();
    public List<Explode> explodes = new ArrayList<>();
    public List<Tank> tanks = new ArrayList<>();
    public NetClient netClient = new NetClient(this);
    private boolean spItem = true;
    private boolean hasItem = false;
    ConnDialog dialog = new ConnDialog();
    private Image image = null;
    public void setHasItem(boolean b)
    {
        hasItem = b;
    }
    public void setSpItem(boolean b)
    {
        spItem = b;
    }
    public static void main(String[] args) {
        TankClient tankClient = new TankClient();
        tankClient.launch();
    }

    @Override
    public void paint(Graphics g) {
        g.drawString("tanks: " + tanks.stream().filter(t -> t.isLive()).toArray().length, 10, GAME_HEIGHT - 20);
        if (netClient.isLeader()) {
            g.drawString("Leader", 10, GAME_HEIGHT - 40);
        }
        if(hasItem)
        {
            g.drawString("Special Item!", 10, GAME_HEIGHT - 60);
        }
        if(spItem)
        {
            g.drawRect(GAME_WIDTH/2-10,GAME_HEIGHT/2-10,20,20);
            g.fillRect(GAME_WIDTH/2-10,GAME_HEIGHT/2-10,20,20);
        }
        for (Missile m : missiles) {
            if (m.hit(tank)) {
                netClient.send(new TankDeadMsg(tank.getId()));
                netClient.send(new MissileDeadMsg(m.getTankID(), m.getId(), tank));
            }
            if (m.isLive()) m.draw(g);
        }

        explodes.stream().filter(explode -> !explode.isDone()).forEach(explode -> explode.draw(g));

        for (Tank t : tanks)
            t.draw(g);
    }

    @Override
    public void update(Graphics g) {
        if (image == null)
            image = this.createImage(800, 600);

        Graphics gOffScreen = image.getGraphics();
        Color color = gOffScreen.getColor();
        gOffScreen.setColor(Color.GRAY);
        gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gOffScreen.setColor(color);
        paint(gOffScreen);
        g.drawImage(image, 0, 0, null);
    }

    public void launch() {
        this.setLocation(400, 300);
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setTitle("SimTank");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
        this.setResizable(false);
        this.setBackground(Color.GRAY);
        this.addKeyListener(new KeyMonitor());
        this.setVisible(true);
        dialog.setVisible(true);

        new Thread(new PaintThread()).start();
    }

    class PaintThread implements Runnable {
        public void run() {
            while (true) {
                repaint();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class KeyMonitor extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_C) {
                dialog.setVisible(true);
            } else {
                tank.keyReleased(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            tank.keyPressed(e);
        }
    }

    class ConnDialog extends Dialog {
        Button button = new Button("Set");
        TextField ipField = new TextField("127.0.0.1", 12);
        TextField udpPortField = new TextField("1234", 4);

        public ConnDialog() {
            super(TankClient.this, true);

            Action action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String ip = ipField.getText().trim();
                    int udpPort = Integer.parseInt(udpPortField.getText().trim());
                    netClient.setServerIP(ip);
                    netClient.setUdpPort(udpPort);
                    netClient.connect();
                    setVisible(false);
                }
            };

            this.setTitle("Connection settings");
            this.setLayout(new FlowLayout());
            this.add(new Label("Server IP:"));
            this.add(ipField);
            ipField.addActionListener(action);
            this.add(new Label("UDP Port:"));
            this.add(udpPortField);
            this.add(button);
            this.setLocation(300, 300);
            this.pack();
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
            button.addActionListener(action);
        }
    }
}
