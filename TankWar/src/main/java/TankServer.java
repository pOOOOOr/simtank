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

    public static void main(String[] args) {
        try {
            new TankServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {

        new Thread(new UDPThread()).start();

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                String IP = socket.getInetAddress().getHostAddress();
                int udpPort = inputStream.readInt();
                Client client = new Client(IP, udpPort);
                clients.add(client);
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeInt(INIT_ID++);

                System.out.println(String.format("Client %s:%s - UDP[%s] connect!",
                        socket.getInetAddress(), socket.getPort(), udpPort));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null)
                    socket.close();
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
