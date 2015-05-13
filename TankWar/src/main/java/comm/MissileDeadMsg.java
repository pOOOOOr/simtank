package main.java.comm;

import main.java.TankClient;
import main.java.model.Explode;
import main.java.model.Missile;
import main.java.model.Tank;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.stream.Collectors;

public class MissileDeadMsg implements Msg {
    private TankClient tankClient;
    private int tankId;
    private int id;
    private Tank target;

    public MissileDeadMsg(int tankId, int id, Tank tank) {
        this.tankId = tankId;
        this.id = id;
        this.target = tank;
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
            outputStream.writeInt(target.getId());

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
                    break;
                }
            }

            int targetID = inputStream.readInt();
            tankClient.explodes.addAll(tankClient.tanks.stream().filter(t -> t.getId() == targetID).map(t -> new Explode(t.getPosX(), t.getPosY())).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
