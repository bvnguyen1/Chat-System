import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
public class testing {
    public static void main(String[] args) throws IOException{
        ServerSocket server1 = new ServerSocket(4999);
        if(server1 != null)
            server1.close();
        ServerSocket server2 = new ServerSocket(4999);

    }
}
