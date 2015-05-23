package main.java.comm;

import java.io.DataInputStream;
import java.net.DatagramSocket;

public interface Msg {

    int TANK_NEW = 1;
    int TANK_MOVE = 2;
    int TANK_DEAD = 3;
    int MISSILE_NEW = 4;
    int MISSILE_DEAD = 5;
    int ITEM_TAKE = 6;
    int ITEM_TAKEN = 7;

    void send(DatagramSocket datagramSocket, String IP, int udpPort);

    void parse(DataInputStream inputStream);
}
