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

import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

public class NetClient {
    private boolean isLeader = false;
    private String localIP = getInetAddress().getHostAddress();
    private DatagramSocket datagramSocket = null;
    private TankClient tankClient;
    private String serverIP;
    private int tcpPort = TankServer.TCP_PORT;
    private int udpPort;
    private Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private List<Client> clients = new ArrayList<>();
    private Client leader = null;

    public NetClient(TankClient tankClient) {
        this.tankClient = tankClient;
    }

    private static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("unknown host!");
        }
        return null;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
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

        new Thread(new UDPRecvThread()).start();

        try {
            socket = new Socket(this.serverIP, this.tcpPort);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(this.udpPort);
            dataInputStream = new DataInputStream(socket.getInputStream());
            int id = dataInputStream.readInt();
            tankClient.tank.setId(id);

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

        while (leader == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TankNewMsg tankNewMsg = new TankNewMsg(this.tankClient);
        send(tankNewMsg);
    }

    public void send(Msg msg) {
        msg.send(this.datagramSocket, leader.getIp(), leader.getUdpPort());
    }

    private class IptableThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    dataOutputStream.writeInt(1);
                    String recv = dataInputStream.readLine().trim();
                    System.out.println(recv);
                    if (recv.equals("pause")) {
                        int roll = showConfirmDialog(null, "Roll a new leader");

                        if (roll == 0) {
                            dataOutputStream.writeInt(new Random().nextInt(100) + 2);
                            continue;
                        } else {
                            showMessageDialog(null, "do not want to roll, quit game");
                            System.exit(1);
                        }
                    }

                    synchronized (clients) {
                        clients.clear();
                        String[] iptable = recv.split("\\|");
                        for (String s : iptable) {
                            String[] temp = s.split(":");
                            Client c = new Client(temp[0], Integer.parseInt(temp[1]));
                            clients.add(c);
                        }
                    }

                    if (leader == null || !leader.equals(clients.get(0)))
                        leader = clients.get(0);

                    isLeader = (leader.getIp().equals(localIP) && leader.getUdpPort() == udpPort);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UDPRecvThread implements Runnable {

        byte[] buf = new byte[1024];

        public void run() {

            while (datagramSocket != null) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    if (isLeader) {
                        synchronized (clients) {
                            // send to non-leaders
                            for (Client c : clients.subList(1, clients.size())) {
                                datagramPacket.setSocketAddress(new InetSocketAddress(c.getIp(), c.getUdpPort()));
                                datagramSocket.send(datagramPacket);
                                System.out.println(String.format("Sending message to %s:%s", c.getIp(), c.getUdpPort()));
                            }
                        }
                    }

                    parse(datagramPacket);
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
