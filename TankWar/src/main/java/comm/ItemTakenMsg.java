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


public class ItemTakenMsg implements Msg {
    private TankClient tankClient;
    private ByteArrayOutputStream arrayOutputStream;
    private DataOutputStream outputStream;
    private byte[] buf;

    public ItemTakenMsg(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    @Override
    public void send(DatagramSocket datagramSocket, String IP, int udpPort) {
        try {
            arrayOutputStream = new ByteArrayOutputStream();
            outputStream = new DataOutputStream(arrayOutputStream);

            outputStream.writeBoolean(REPROCESS);
            outputStream.writeInt(ITEM_TAKEN);
            outputStream.writeLong(System.currentTimeMillis());
            outputStream.writeInt(tankClient.tank.getId());

            buf = arrayOutputStream.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, new InetSocketAddress(IP, udpPort));
            datagramSocket.send(datagramPacket);
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
                } else {
                    t.setHasItem(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

