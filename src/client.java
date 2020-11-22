import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class client {
    private static int PORT;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        InetAddress IP = InetAddress.getByName("localhost");
        DatagramSocket socket = new DatagramSocket();

        while(true) {
            byte[] sendBuffer = new byte[512];
            byte[] recvBuffer = new byte[512];
            System.out.print("---Welcome to Messenger---\nInput C to create new session or J to join existing session: ");
            String clientData = "";
            do {
                System.out.print("Incorrect input, try again (C/J): ");
                clientData = sc.nextLine();
            }while(clientData != "C" || clientData != "J");
            sendBuffer = clientData.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, PORT);
            socket.send(sendPacket);
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
            socket.receive(recvPacket);
            String serverData = new String(recvPacket.getData());
            System.out.println("\n [SERVER]: " +  serverData);
            clientData = sc.nextLine();
            sendBuffer = clientData.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, PORT);
            socket.send(sendPacket);
        }
    }

}
