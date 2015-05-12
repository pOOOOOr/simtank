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
    private TankClient tankClient;
    private int tankId;
    private int id;

    public MissileDeadMsg(int tankId, int id) {
        this.tankId = tankId;
        this.id = id;
    }

    public MissileDeadMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeInt(MISSILE_DEAD);
            outputStream.writeInt(tankId);
            outputStream.writeInt(id);

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
            int id = inputStream.readInt();
            for (Missile m : tankClient.missiles) {
                if (m.getTankID() == tankId && m.getId() == id) {
                    m.setLive(false);
                    tankClient.explodes.add(new Explode(m.getX(), m.getY()));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
