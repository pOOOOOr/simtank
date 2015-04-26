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

public class TankMoveMsg implements Msg {
    int msgType = TANK_MOVE_MSG;
    int x, y;
    int id;
    Direction ptDirection;
    Direction direction;
    TankClient tc;

    public TankMoveMsg(int id, int x, int y, Direction direction, Direction ptDirection) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.ptDirection = ptDirection;
    }

    public TankMoveMsg(TankClient tc) {
        this.tc = tc;
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
            Direction ptDirection = Direction.values()[dis.readInt()];
            // System.out.println("id:" + id + "-posX:" + posX + "-posY:" + posY + "-direction:" +
            // direction + "-good:" + good);
            boolean exist = false;
            for (int i = 0; i < tc.tanks.size(); i++) {
                Tank t = tc.tanks.get(i);
                if (t.getId() == id) {
                    t.setPosX(x);
                    t.setPosY(y);
                    t.setDirection(direction);
                    t.setCanonDirection(ptDirection);
                    exist = true;
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
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(direction.ordinal());
            dos.writeInt(ptDirection.ordinal());
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
