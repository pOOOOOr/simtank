package main.java.comm;

import main.java.TankClient;

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
    public ItemTakeMsg(TankClient tank)
    {
        tankClient = tank;
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
        int tankId = -1;
        try {
            tankId = inputStream.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(tankId == this.tankClient.tank.getId())
        {
            this.tankClient.setHasItem(true);
        }
        else
            this.tankClient.setHasItem(false);
        this.tankClient.setSpItem(false);
    }
}
