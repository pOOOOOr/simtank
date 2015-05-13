package main.java.model;

import java.net.Socket;

public class Client {
    private String ip;
    private int udpPort;
    private Socket socket;
    private int weight = 0;
    private int tankID = 0;

    public Client(String ip, int udpPort) {
        this.ip = ip;
        this.udpPort = udpPort;
    }

    public Client(String ip, int udpPort, Socket socket) {
        this(ip, udpPort);
        this.socket = socket;
    }

    public int getTankID() {
        return tankID;
    }

    public void setTankID(int tankID) {
        this.tankID = tankID;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return ip;
    }

    public int getUdpPort() {
        return udpPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Client)) return false;
        Client client = (Client) obj;
        return this.ip.equals(client.getIp()) && this.udpPort == client.getUdpPort();
    }
}
