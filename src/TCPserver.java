import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class TCPserver {
    private String name = null;
    private ServerSocket server = null;
    private static int count = 0;
    public List<Person> persons;
    private int PORT = 0;

    public TCPserver(String name) throws IOException {
        this.PORT = 5000 + count;
        this.server = new ServerSocket(PORT);
        this.name = name;
        persons = new ArrayList<Person>();
        this.count = count + 1;
    }

    public String getName() {
        return this.name;
    }

    public ServerSocket getServer() {
        return this.server;
    }
    public void addPerson(Person person) {
        persons.add(person);
    }
    public void removePerson(Person person){
        persons.remove(person);
    }
    public int getTotalPeople() {
        return persons.size();
    }
    public int getPORT() {
        return this.PORT;
    }

    public void broadcast(Person person, String message) throws IOException {
        for (Person x: persons) {
            DataOutputStream outputStream = new DataOutputStream(x.getSocket().getOutputStream());
            outputStream.writeUTF(person.getName() + ": " + message);
            outputStream.flush();
        }
    }

    public void closeServer() throws IOException {
        if(server != null) {
            this.server.close();
            this.count = count - 1 ;
        }
    }


}
