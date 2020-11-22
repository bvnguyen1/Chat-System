import java.net.InetAddress;
import java.net.Socket;

public class Person {
    private InetAddress addr = null;
    private Socket socket = null;
    private String name = null;

    public Person(InetAddress addr, Socket client) {
        this.addr =  addr;
        this.socket = client;
        this.name = null;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public Socket getSocket() {
        return this.socket;
    }
    public InetAddress getAddr() {
        return  this.addr;
    }
}
