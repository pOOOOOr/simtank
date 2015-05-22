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

/**
 * Created by Chuan on 15/5/22.
 */
public class ItemTakeMsg implements Msg {
    private TankClient tankClient;

    public ItemTakeMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    @Override
    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(arrayOutputStream);
        try {
            outputStream.writeInt(ITEM_TAKE);
            outputStream.writeInt(tankClient.tank.getId());
            outputStream.writeLong(System.currentTimeMillis());
            byte[] buf = arrayOutputStream.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, udpPort));
            datagramSocket.send(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parse(DataInputStream inputStream) {
        try {
            int id = inputStream.readInt();
            for (Tank t : tankClient.tanks) {
                if (t.getId() == id) {
                    t.setHasItem(true);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
