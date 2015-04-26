package main.java.comm;

import main.java.TankClient;
import main.java.model.Tank;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class TankDeadMsg implements Msg {
    int msgType = TANK_DEAD_MSG;
    TankClient tc;
    int id;

    public TankDeadMsg(int id) {
        this.id = id;
    }

    public TankDeadMsg(TankClient tc) {
        this.tc = tc;
    }

    public void parse(DataInputStream dis) {
        try {
            int id = dis.readInt();
            if (tc.tank.getId() == id) {
                return;
            }

            // System.out.println("id:" + id + "-posX:" + posX + "-posY:" + posY + "-direction:" +
            // direction + "-good:" + good);
            for (int i = 0; i < tc.tanks.size(); i++) {
                Tank t = tc.tanks.get(i);
                if (t.getId() == id) {
                    t.setLive(false);
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
