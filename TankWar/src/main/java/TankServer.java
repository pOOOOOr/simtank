package main.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class TankServer {
    public static final int TCP_PORT = 8888;
    public static final int UDP_PORT = 6666;
    private static int INIT_ID = 100;
    private List<Client> clients = new ArrayList<>();
    private List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        TankServer tankServer = new TankServer();
        tankServer.start();
    }

    private String generateClientsMessage() {
        StringBuilder message = new StringBuilder();

        for (Client c : clients) {
            message.append(c.IP).append("|");
        }
        message.append("\n");

        return message.toString();
    }

    /*
    * send client table
    * health check
    * */
    private class Beat implements Runnable {
        @Override
        public void run() {
            DataInputStream inputStream;
            DataOutputStream outputStream;

            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Socket s : sockets) {
                    try {
                        inputStream = new DataInputStream(s.getInputStream());
                        System.out.println(String.format("message from [%s]: %s.", s.getInetAddress(), inputStream.readInt()));
                        outputStream = new DataOutputStream(s.getOutputStream());
                        outputStream.writeBytes(generateClientsMessage());
                    } catch (IOException e) {
                        System.out.println(s.getInetAddress() + " die");
                    }
                }
            }
        }
    }

    public void start() throws IOException {

        new Thread(new UDPThread()).start();
        new Thread(new Beat()).start();

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                String IP = socket.getInetAddress().getHostAddress();
                int udpPort = inputStream.readInt();

                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeInt(INIT_ID++);

                clients.add(new Client(IP, udpPort));
                sockets.add(socket);

                System.out.println(String.format("Client %s:%s - UDP[%s] connect!",
                        socket.getInetAddress(), socket.getPort(), udpPort));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // if (socket != null) socket.close();
            }
        }
    }

    private class Client {
        String IP;
        int udpPort;

        public Client(String IP, int udpPort) {
            this.IP = IP;
            this.udpPort = udpPort;
        }
    }

    private class UDPThread implements Runnable {

        byte[] buf = new byte[1024];

        public void run() {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(UDP_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            System.out.println("UDP thread started at port: " + UDP_PORT);

            try {
                while (datagramSocket != null) {
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(datagramPacket);
                    for (Client c : clients) {
                        datagramPacket.setSocketAddress(new InetSocketAddress(c.IP, c.udpPort));
                        datagramSocket.send(datagramPacket);

                        System.out.println(String.format("Package sent to %s:%s", c.IP, c.udpPort));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
