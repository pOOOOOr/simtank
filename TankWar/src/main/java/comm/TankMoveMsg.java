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
    private int id;
    private int x;
    private int y;
    private Direction canonDirection;
    private Direction direction;
    private TankClient tankClient;

    public TankMoveMsg(int id, int x, int y, Direction direction, Direction canonDirection) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.canonDirection = canonDirection;
    }

    public TankMoveMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeBoolean(FORWARD);
            outputStream.writeInt(TANK_MOVE);
            outputStream.writeInt(id);
            outputStream.writeInt(x);
            outputStream.writeInt(y);
            outputStream.writeInt(direction.ordinal());
            outputStream.writeInt(canonDirection.ordinal());

            byte[] buf = arrayOutputStream.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, udpPort));
            datagramSocket.send(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parse(DataInputStream inputStream) {
        try {
            int id = inputStream.readInt();
            if (tankClient.tank.getId() == id) return;
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            Direction direction = Direction.values()[inputStream.readInt()];
            Direction canonDirection = Direction.values()[inputStream.readInt()];

            for (Tank t : tankClient.tanks) {
                if (t.getId() == id) {
                    t.setPosX(x);
                    t.setPosY(y);
                    t.setDirection(direction);
                    t.setCanonDirection(canonDirection);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
