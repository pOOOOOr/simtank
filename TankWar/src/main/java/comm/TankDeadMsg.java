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
    private int id;
    private TankClient tankClient;

    public TankDeadMsg(int id) {
        this.id = id;
    }

    public TankDeadMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeInt(TANK_DEAD);
            outputStream.writeInt(id);
            byte[] buf = arrayOutputStream.toByteArray();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, udpPort));
            datagramSocket.send(dp);
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

            for (Tank t : tankClient.tanks) {
                if (t.getId() == id) {
                    t.setLive(false);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
