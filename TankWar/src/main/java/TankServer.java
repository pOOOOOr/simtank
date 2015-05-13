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
        System.out.println("Server started....");
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
                int tankID = INIT_ID++;
                outputStream.writeInt(tankID);
                Client client = new Client(IP, udpPort, socket);
                client.setTankID(tankID);
                synchronized (clients) {
                    clients.add(client);
                }

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

                synchronized (clients) {
                    for (Iterator<Client> iterator = clients.iterator(); iterator.hasNext(); ) {
                        Client client = iterator.next();
                        try {
                            if (!client.getSocket().isClosed()) {
                                inputStream = new DataInputStream(client.getSocket().getInputStream());
                                System.out.println(String.format("message from [%s]: %s.", client.getSocket().getInetAddress(), inputStream.readInt()));
                                outputStream = new DataOutputStream(client.getSocket().getOutputStream());
                                outputStream.writeBytes(generateClientsMessage());
                            }
                        } catch (IOException e) {
                            System.out.println(String.format("Client %s:%s dropped.", client.getIp(), client.getUdpPort()));
                            if (client.getSocket() != null) {
                                try {
                                    client.getSocket().close();
                                    client.setSocket(null);
                                    iterator.remove();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            clientDrops(client.getTankID());
                            break;
                        }
                    }
                }
            }
        }

        private void clientDrops(int tankID) {
            if (clients.size() == 0) {
                System.out.println("No player exists.");
                currentLeader = null;
            }

            clients.stream().filter(client -> !client.getSocket().isClosed()).forEach(client -> {
                try {
                    DataOutputStream pauseStream = new DataOutputStream(client.getSocket().getOutputStream());
                    pauseStream.writeBytes(String.format("Drop:%s\n", tankID));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            clients.stream().filter(client -> !client.getSocket().isClosed()).forEach(client -> {
                try {
                    DataInputStream randomReturn = new DataInputStream(client.getSocket().getInputStream());
                    while (true) {
                        int weight = randomReturn.readInt();
                        if (weight != 1) {
                            client.setWeight(weight);
                            System.out.println(String.format("Client: %s:%s, Weight: %s", client.getIp(), client.getUdpPort(), client.getWeight()));
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            int currentWeight = 0;
            for (Client c : clients) {
                if (c.getWeight() > currentWeight) {
                    currentWeight = c.getWeight();
                    currentLeader = c;
                }
            }
        }
    }
}
