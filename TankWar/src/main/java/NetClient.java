package main.java;

import main.java.comm.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class NetClient {
    DatagramSocket datagramSocket = null;
    private TankClient tankClient;
    private String IP;
    private int tcpPort;
    private int udpPort;

    public NetClient(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public void connect() {
        try {
            datagramSocket = new DatagramSocket(this.udpPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Socket socket = null;
        try {
            socket = new Socket(this.IP, this.tcpPort);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(this.udpPort);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int id = dataInputStream.readInt();

            tankClient.tank.id = id;

            System.out.println("Connected! ID: " + id);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        TankNewMsg tankNewMsg = new TankNewMsg(this.tankClient.tank);
        send(tankNewMsg);

        new Thread(new UDPRecvThread()).start();
    }

    public void send(Msg msg) {
        msg.send(this.datagramSocket, this.IP, TankServer.UDP_PORT);
    }

    private class UDPRecvThread implements Runnable {

        byte[] buf = new byte[1024];

        public void run() {

            while (datagramSocket != null) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    parse(datagramPacket);
                    System.out.println("a packet received from server!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parse(DatagramPacket packet) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf, 0, packet.getLength());
            DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
            int msgType = 0;
            try {
                msgType = inputStream.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Msg msg;
            switch (msgType) {
                case Msg.TANK_NEW_MSG:
                    msg = new TankNewMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.TANK_MOVE_MSG:
                    msg = new TankMoveMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.MISSILE_NEW_MSG:
                    msg = new MissileNewMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.TANK_DEAD_MSG:
                    msg = new TankDeadMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.MISSILE_DEAD_MSG:
                    msg = new MissileDeadMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
            }

        }
    }
}
