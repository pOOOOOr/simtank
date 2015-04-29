package main.java.model;

public class Client {
    private String ip;
    private int udpPort;

    public Client(String ip, int udpPort) {
        this.ip = ip;
        this.udpPort = udpPort;
    }

    public String getIp() {
        return ip;
    }

    public int getUdpPort() {
        return udpPort;
    }
}
