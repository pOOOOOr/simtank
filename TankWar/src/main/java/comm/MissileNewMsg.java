package main.java.comm;

import main.java.TankClient;
import main.java.model.Dir;
import main.java.model.Missile;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MissileNewMsg implements Msg {
    int msgType = MISSILE_NEW_MSG;
    TankClient tc;
    Missile m;


    public MissileNewMsg(Missile m) {
        this.m = m;
    }

    public MissileNewMsg(TankClient tc) {
        this.tc = tc;
    }

    public void send(DatagramSocket ds, String IP, int udpPort) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(msgType);
            dos.writeInt(m.tankId);
            dos.writeInt(m.id);
            dos.writeInt(m.x);
            dos.writeInt(m.y);
            dos.writeInt(m.dir.ordinal());
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

    public void parse(DataInputStream dis) {
        try {
            int tankId = dis.readInt();
            if (tankId == tc.tank.id) {
                return;
            }
            int id = dis.readInt();
            int x = dis.readInt();
            int y = dis.readInt();
            Dir dir = Dir.values()[dis.readInt()];

            Missile m = new Missile(tankId, x, y, dir, tc);
            m.id = id;
            tc.missiles.add(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
