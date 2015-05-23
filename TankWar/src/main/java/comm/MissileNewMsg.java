package main.java.comm;

import main.java.TankClient;
import main.java.model.Direction;
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
    private TankClient tankClient;
    private Missile missile;

    public MissileNewMsg(Missile missile) {
        this.missile = missile;
    }

    public MissileNewMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeBoolean(FORWARD);
            outputStream.writeInt(MISSILE_NEW);
            outputStream.writeInt(missile.getTankID());
            outputStream.writeInt(missile.getId());
            outputStream.writeInt(missile.getX());
            outputStream.writeInt(missile.getY());
            outputStream.writeInt(missile.getDirection().ordinal());

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
            int tankId = inputStream.readInt();
            if (tankId == tankClient.tank.getId()) return;
            int id = inputStream.readInt();
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            Direction direction = Direction.values()[inputStream.readInt()];

            tankClient.missiles.add(new Missile(id, tankId, x, y, direction, tankClient));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
