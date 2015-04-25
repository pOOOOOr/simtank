package main.java;

import main.java.comm.MissileDeadMsg;
import main.java.comm.TankDeadMsg;
import main.java.model.Dir;
import main.java.model.Explode;
import main.java.model.Missile;
import main.java.model.Tank;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class TankClient extends Frame {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 600;

    public Tank tank = new Tank(100, 50, Dir.STOP, this);
    public List<Missile> missiles = new ArrayList<>();
    public List<Explode> explodes = new ArrayList<>();
    public List<Tank> tanks = new ArrayList<>();
    public NetClient netClient = new NetClient(this);
    ConnDialog dialog = new ConnDialog();
    private Image image = null;

    public static void main(String[] args) {
        TankClient tankClient = new TankClient();
        tankClient.launch();
    }

    @Override
    public void paint(Graphics g) {
        // g.drawString("missiles count:" + missiles.size(), 10, 50);
        // g.drawString("explodes count:" + explodes.size(), 10, 70);
        g.drawString("tanks: " + tanks.size(), 10, 40);

        for (Missile m : missiles) {
            if (m.hitTank(tank)) {
                TankDeadMsg tankDeadMsg = new TankDeadMsg(tank.id);
                netClient.send(tankDeadMsg);
                MissileDeadMsg missileDeadMsg = new MissileDeadMsg(m.tankId, m.id);
                netClient.send(missileDeadMsg);
            }
            m.draw(g);
        }

        for (Explode explode : explodes)
            explode.draw(g);

        for (Tank t : tanks)
            t.draw(g);

        tank.draw(g);
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
        TextField tcpPortField = new TextField(String.valueOf(TankServer.TCP_PORT), 4);
        TextField udpPortField = new TextField("1234", 4);

        public ConnDialog() {
            super(TankClient.this, true);

            this.setTitle("Connection settings");
            this.setLayout(new FlowLayout());
            this.add(new Label("Server IP:"));
            this.add(ipField);
            this.add(new Label("TCP Port:"));
            this.add(tcpPortField);
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
            button.addActionListener(e -> {
                String ip = ipField.getText().trim();
                int tcpPort = Integer.parseInt(tcpPortField.getText().trim());
                int udpPort = Integer.parseInt(udpPortField.getText().trim());
                netClient.setIP(ip);
                netClient.setTcpPort(tcpPort);
                netClient.setUdpPort(udpPort);
                netClient.connect();
                setVisible(false);
            });
        }
    }
}
