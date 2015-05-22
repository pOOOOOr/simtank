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
    private TankClient tankClient;

    public TankNewMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeInt(TANK_NEW);
            outputStream.writeInt(tankClient.tank.getId());
            outputStream.writeInt(tankClient.tank.getPosX());
            outputStream.writeInt(tankClient.tank.getPosY());
            outputStream.writeInt(tankClient.tank.getDirection().ordinal());
            outputStream.writeInt(tankClient.tank.getColorIndex());
            outputStream.writeBoolean(tankClient.tank.isHasItem());

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

            // exist
            for (Tank t : tankClient.tanks) {
                if (t.getId() == id)
                    return;
            }

            // self
            if (tankClient.tank.getId() == id) {
                tankClient.tanks.add(tankClient.tank);
                return;
            }

            int x = inputStream.readInt();
            int y = inputStream.readInt();
            Direction direction = Direction.values()[inputStream.readInt()];
            int colorIndex = inputStream.readInt();
            boolean hasItem = inputStream.readBoolean();

            tankClient.tanks.add(new Tank(id, x, y, direction, tankClient, colorIndex, hasItem));

            // broadcast self for new client
            tankClient.netClient.send(new TankNewMsg(tankClient));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
