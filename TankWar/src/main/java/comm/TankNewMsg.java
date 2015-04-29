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
    private Tank tank;
    private TankClient tankClient;

    public TankNewMsg(Tank tank) {
        this.tank = tank;
    }

    public TankNewMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeInt(TANK_NEW);
            outputStream.writeInt(tank.getId());
            outputStream.writeInt(tank.getPosX());
            outputStream.writeInt(tank.getPosY());
            outputStream.writeInt(tank.getDirection().ordinal());
            outputStream.writeInt(tank.getColorIndex());

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

            for (Tank t : tankClient.tanks)
                if (t.getId() == id) return;

            tankClient.netClient.send(new TankNewMsg(tankClient.tank));
            tankClient.tanks.add(new Tank(id, x, y, direction, tankClient));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
