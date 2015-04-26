package main.java.comm;

import main.java.TankClient;
import main.java.model.Direction;
import main.java.model.Tank;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class TankNewMsg implements Msg {
    int msgType = TANK_NEW_MSG;
    Tank tank;
    TankClient tc;

    public TankNewMsg(Tank tank) {
        this.tank = tank;
    }

    public TankNewMsg(TankClient tc) {
        this.tc = tc;
    }

    public void send(DatagramSocket ds, String IP, int udpPort) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(msgType);
            dos.writeInt(tank.getId());
            dos.writeInt(tank.getPosX());
            dos.writeInt(tank.getPosY());
            dos.writeInt(tank.getDirection().ordinal());
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
            int id = dis.readInt();
            if (tc.tank.getId() == id) {
                return;
            }

            int x = dis.readInt();
            int y = dis.readInt();
            Direction direction = Direction.values()[dis.readInt()];

            boolean exist = false;
            for (int i = 0; i < tc.tanks.size(); i++) {
                Tank t = tc.tanks.get(i);
                if (t.getId() == id) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                TankNewMsg tnMsg = new TankNewMsg(tc.tank);
                tc.netClient.send(tnMsg);

                Tank t = new Tank(x, y, direction, tc);
                t.setId(id);
                tc.tanks.add(t);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
