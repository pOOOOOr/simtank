package main.java.comm;

import main.java.TankClient;
import main.java.model.Explode;
import main.java.model.Missile;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MissileDeadMsg implements Msg {
    int msgType = MISSILE_DEAD_MSG;
    TankClient tc;
    int tankId;
    int id;

    public MissileDeadMsg(int tankId, int id) {
        this.tankId = tankId;
        this.id = id;
    }

    public MissileDeadMsg(TankClient tc) {
        this.tc = tc;
    }

    public void parse(DataInputStream dis) {
        try {
            int tankId = dis.readInt();
            int id = dis.readInt();

            // System.out.println("id:" + id + "-posX:" + posX + "-posY:" + posY + "-direction:" +
            // direction + "-good:" + good);
            for (int i = 0; i < tc.missiles.size(); i++) {
                Missile m = tc.missiles.get(i);
                if (m.getTankID() == tankId && m.getId() == id) {
                    m.setLive(false);
                    tc.explodes.add(new Explode(m.getX(), m.getY()));
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(DatagramSocket ds, String IP, int udpPort) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(msgType);
            dos.writeInt(tankId);
            dos.writeInt(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buf = baos.toByteArray();
        try {
            DatagramPacket dp = new DatagramPacket(buf, buf.length,
                    new InetSocketAddress(IP, udpPort));
            ds.send(dp);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
