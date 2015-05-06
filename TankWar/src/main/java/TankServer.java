package main.java;

import main.java.model.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TankServer {
    public static final int TCP_PORT = 8888;
    private static int INIT_ID = 100;
    private List<Client> clients = new ArrayList<>();
    private Client currentLeader = null;

    public static void main(String[] args) throws IOException {
        TankServer tankServer = new TankServer();
        tankServer.start();
    }

    private String generateClientsMessage() {
        StringBuilder message = new StringBuilder();
        if (currentLeader != null)
            message.append(String.format("%s:%s|", currentLeader.getIp(), currentLeader.getUdpPort()));

        clients.stream().filter(c -> !c.equals(currentLeader)).forEach(c -> message.append(String.format("%s:%s|", c.getIp(), c.getUdpPort())));
        message.append("\n");

        return message.toString();
    }

    public void start() throws IOException {

        new Thread(new Beat()).start();

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                String IP = socket.getInetAddress().getHostAddress();
                int udpPort = inputStream.readInt();

                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeInt(INIT_ID++);

                Client client = new Client(IP, udpPort, socket);
                clients.add(client);

                if (currentLeader == null)
                    currentLeader = client;

                System.out.println(String.format("Client %s:%s - UDP[%s] connect!",
                        socket.getInetAddress(), socket.getPort(), udpPort));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // if (socket != null) socket.close();
            }
        }
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
                for (Iterator<Client> iterator = clients.iterator(); iterator.hasNext();) {
                    Client c = iterator.next();
                    try {
                        if (!c.getSocket().isClosed()) {
                            inputStream = new DataInputStream(c.getSocket().getInputStream());
                            System.out.println(String.format("message from [%s]: %s.", c.getSocket().getInetAddress(), inputStream.readInt()));
                            outputStream = new DataOutputStream(c.getSocket().getOutputStream());
                            outputStream.writeBytes(generateClientsMessage());
                        }
                    } catch (IOException e) {
                        System.out.println(c.getSocket().getInetAddress() + " die");
                        if (c.getSocket() != null) {
                            try {
                                c.getSocket().close();
                                c.setSocket(null);
                                iterator.remove();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        sendPauseMessage();
                        break;
                    }
                }
            }
        }

        private void sendPauseMessage() {
            clients.stream().filter(client -> !client.getSocket().isClosed()).forEach(client -> {
                try {
                    DataOutputStream pauseStream = new DataOutputStream(client.getSocket().getOutputStream());
                    pauseStream.writeBytes("pause\n");
                    DataInputStream randomReturn = new DataInputStream(client.getSocket().getInputStream());
                    randomReturn.readInt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
