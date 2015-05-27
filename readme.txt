Group 7
621715	Chuan Qin
644368	Chikai Zhang
685269	Xin Huang

Code Structure:
TankWar
└─src
    └─main
        └─java
            │  NetClient.java   # Functions for communications server to client and client to client
            │  TankClient.java  # UI drawing and key events handling
            │  TankServer.java  # Server for checking clients
            ├─comm
            │   *.java          # Classes for messages, including sending and parsing
            └─model
                *.java          # Classes for all objects, tanks, clients and so forth

How to execute:
1. create project based on code using Eclipse or IntelliJ IDEA
2. run TankServer and copy ip address in output
3. run TankClient and input server's ip address to connect