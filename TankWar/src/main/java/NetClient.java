package main.java;

import main.java.comm.*;
import main.java.model.Client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetClient {
    DatagramSocket datagramSocket = null;
    boolean isLeader = false;
    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;
    private TankClient tankClient;
    private String IP;
    private int tcpPort;
    private int udpPort;
    private List<Client> clients = new ArrayList<>();

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


        try {
            socket = new Socket(this.IP, this.tcpPort);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(this.udpPort);
            dataInputStream = new DataInputStream(socket.getInputStream());
            int id = dataInputStream.readInt();
            tankClient.tank.setId(id);
            tankClient.tanks.add(tankClient.tank);

            System.out.println("Connected! ID: " + id);
            new Thread(new IptableThread()).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                //socket.close();
            }
        }

        TankNewMsg tankNewMsg = new TankNewMsg(this.tankClient.tank);
        send(tankNewMsg);

        new Thread(new UDPRecvThread()).start();
    }

    public void send(Msg msg) {
        msg.send(this.datagramSocket, this.IP, TankServer.UDP_PORT);
    }

    private class IptableThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    dataOutputStream.writeInt(1);
                    //dos.writeInt(1);
                    //System.out.println(dataInputStream.readLine());
                    clients.clear();
                    String recv = dataInputStream.readLine().trim();
                    System.out.println(recv);
                    if (recv.equals("pause")) {
                        dataOutputStream.writeInt(new Random().nextInt(100));
                        continue;
                    }

                    String[] iptable = recv.split("\\|");
                    for (String s : iptable) {
                        System.out.println(s);
                        String[] temp = s.split(":");
                        Client c = new Client(temp[0], Integer.parseInt(temp[1]));
                        clients.add(c);
                    }
                    for (Client c : clients) {
                        System.out.println(c.getIp());
                    }
                    System.out.println("Iptable retrived!");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /*private class UDPLeaderThread implements Runnable {

        byte[] buf = new byte[1024];

        public void run() {
            //DatagramSocket datagramSocket = null;
            //try {
            //  datagramSocket = new DatagramSocket(UDP_PORT);
            //} catch (SocketException e) {
            //  e.printStackTrace();
            //}
            if (isLeader) {
                System.out.println("Leather thread started at port: " + udpPort);

                try {
                    while (datagramSocket != null) {
                        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                        datagramSocket.receive(datagramPacket);
                        for (Client c : clients) {
                            datagramPacket.setSocketAddress(new InetSocketAddress(c.getIp(), c.getUdpPort()));
                            datagramSocket.send(datagramPacket);

                            System.out.println(String.format("Package sent to %s:%s", c.getIp(), c.getUdpPort()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
    private class UDPRecvThread implements Runnable {

        byte[] buf = new byte[1024];

        public void run() {

            while (datagramSocket != null) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    parse(datagramPacket);
                    if (isLeader) {
                        for (Client c : clients) {
                            datagramPacket.setSocketAddress(new InetSocketAddress(c.getIp(), c.getUdpPort()));
                            datagramSocket.send(datagramPacket);

                            System.out.println(String.format("Package sent to %s:%s", c.getIp(), c.getUdpPort()));
                        }
                    }
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
                case Msg.TANK_NEW:
                    msg = new TankNewMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.TANK_MOVE:
                    msg = new TankMoveMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.MISSILE_NEW:
                    msg = new MissileNewMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.TANK_DEAD:
                    msg = new TankDeadMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
                case Msg.MISSILE_DEAD:
                    msg = new MissileDeadMsg(NetClient.this.tankClient);
                    msg.parse(inputStream);
                    break;
            }

        }
    }
}
