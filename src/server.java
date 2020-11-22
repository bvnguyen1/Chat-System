import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class server {

    // Global Variables
    public static String IP = "localhost";
    public static int PORT = 4999;
    public static List<TCPserver> servers;
    public static DatagramSocket UDPsocket;
    public static String[] clientList;

    public static void main(String[] args) throws IOException, InterruptedException {
        // testing TCP servers
        servers = new ArrayList<>();
        TCPserver server1 = new TCPserver("Avenger");
        TCPserver server2 = new TCPserver("Suicide Squad");
        TCPserver server3 = new TCPserver("The Simpsons");
        servers.add(server1);
        servers.add(server2);
        servers.add(server3);

        Thread server1Thread = new Thread(new Wait_For_TCPconnection(server1));
        Thread server2Thread = new Thread(new Wait_For_TCPconnection(server2));
        Thread server3Thread = new Thread(new Wait_For_TCPconnection(server3));

        server1Thread.start();
        server2Thread.start();
        server3Thread.start();

        UDPsocket = new DatagramSocket(PORT);
        System.out.println("[SERVER] Server is online...");
        Thread ONLINE_THREAD = new Thread(new Wait_For_UDPconnection());
        ONLINE_THREAD.start();
        ONLINE_THREAD.join();

    }

    private static void broadcast() {

    }

    // This thread wait for connection with UDP protocol
    public static class Wait_For_UDPconnection implements Runnable {
        @Override
        public void run() {
            while (true){
                byte[] recvBuffer = new byte[512];

                DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                try {
                    System.out.println("waiting to receive packet...");
                    UDPsocket.receive(recvPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int clientPort = recvPacket.getPort();
                InetAddress clientIP = recvPacket.getAddress();

                byte[] recvData = new byte[recvPacket.getLength()];
                System.arraycopy(recvPacket.getData(), recvPacket.getOffset(), recvData, 0, recvPacket.getLength());
                String clientData = new String(recvData);


                if (clientData.equals("Connection")) {
                    System.out.println("[CONNECTION] New client had established connection.");
                    try { // try to send a string with all the server name
                        String sendData = "Online Session:\n";
                        for (TCPserver server: servers) {
                            sendData = sendData + server.getName() + "\n";
                        }
                        byte[] sendBuffer = new byte[512];
                        sendBuffer = sendData.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientIP, clientPort);
                        UDPsocket.send(sendPacket);
                        sendPacket = null;
                    }catch (Exception e) {
                        System.out.println("failed to send packet");
                    }
                }else if (clientData.substring(0,6).equals("Create")){
                    TCPserver tcpServer = null;
                    String sessionName = clientData.substring(7, clientData.length());
                    try {       // create session and establish session TCP server
                        tcpServer = new TCPserver(sessionName);
                        servers.add(tcpServer);
                        Thread tcpServerThread = new Thread(new Wait_For_TCPconnection(tcpServer));
                        tcpServerThread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // send server PORT number to the client
                    String portNum = Integer.toString(tcpServer.getPORT());
                    byte[] sendBuffer = new byte[512];
                    String sendData = portNum;
                    sendBuffer = sendData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientIP, clientPort);
                    try {
                        UDPsocket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if (clientData.substring(0,4).equals("Join")){
                    String sessionName = clientData.substring(5, clientData.length());
                    boolean sessionBool = false;
                    String portNum = "";

                    // Check if the session exist, join that session, it not, send does not exist message along with existing session.
                    for (TCPserver server: servers) {
                        if (server.getName().equalsIgnoreCase(sessionName)) {
                            sessionBool = true;
                            portNum = Integer.toString(server.getPORT());
                            break;
                        }
                    }
                    if (sessionBool){       // session found
                        byte[] sendBuffer = new byte[512];
                        // send the client the port number of their sessionName for them to connect.
                        String sendData = portNum;
                        sendBuffer = sendData.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientIP, clientPort);
                        try {
                            UDPsocket.send(sendPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else{                  // session not found
                        byte[] sendBuffer = new byte[512];
                        String sendData = "The session you entered does not exist.";
                        sendBuffer = sendData.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientIP, clientPort);
                        try {
                            UDPsocket.send(sendPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    System.out.println("in else");
                }




            }
        }
    }


    public static class Wait_For_TCPconnection implements Runnable {
        private TCPserver server;
        Socket tcpSocket;
        public  Wait_For_TCPconnection(TCPserver server) {
            System.out.println("[NEW SESSION] " + server.getName() + " is established");
            this.server = server;
            tcpSocket = null;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    tcpSocket = server.getServer().accept();
                    Person person = new Person(tcpSocket.getInetAddress() , tcpSocket);
                    System.out.println("[CONENCTION] " + tcpSocket.getInetAddress() + " had connected to " + server.getName() + " session.");
                    // start handle message thread here
                    Thread thread = new Thread(new Handle_Client(person, server));
                    thread.start();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Handle_Client implements Runnable {
        private Person person = null;
        private TCPserver tcpServer = null;
        public Handle_Client(Person person, TCPserver tcpServer) {
            this.person = person;
            this.tcpServer = tcpServer;
            this.tcpServer.addPerson(person);
        }
        @Override
        public void run() {
            Socket tcpSocket = person.getSocket();
            try {
                DataInputStream input = new DataInputStream(tcpSocket.getInputStream());
                String name = input.readUTF();
                person.setName(name);
                tcpServer.broadcast(person, "has join the chat!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true){
                String msg = "";
                try{
                    DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());
                    msg = inputStream.readUTF();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if (msg.equalsIgnoreCase("{quit}")) {
                    tcpServer.removePerson(person);
                    System.out.println(person.getName() + " quit chat session " + tcpServer.getName());
                    try {
                        tcpServer.broadcast(person," has left the chat.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (tcpServer.getTotalPeople() == 0) {
                        System.out.println(tcpServer.getName() + " have 0 client. Deleting chat session." );
                        try {
                            tcpServer.closeServer();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        servers.remove(tcpServer);
                    }
                    break;
                }else {
                    try {
                        tcpServer.broadcast(person, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
