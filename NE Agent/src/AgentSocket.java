import java.io.*;
import java.net.Socket;

/* Singleton class for the socket object used to communicate with NN */
public class AgentSocket extends Socket {

    private static AgentSocket instance = null;

    private static BufferedReader in = null;         // Input stream (from NN)
    private static DataOutputStream out = null;       // Output stream (to NN)

    protected AgentSocket() throws IOException {
        super("localhost", 4444);
    }

    public static AgentSocket getInstance() {
        if (instance == null) {
            try {
                instance = new AgentSocket();
                in = new BufferedReader(new InputStreamReader(instance.getInputStream()));
                out = new DataOutputStream(instance.getOutputStream());
            } catch (IOException e) {
                System.err.println("Error initialising socket.");
                e.printStackTrace();
            }
        }

        return instance;
    }

    public static void main(String[] args) {
        AgentSocket as = AgentSocket.getInstance();
        try {
            // Read from server
            System.out.println(in.readLine());

            System.out.println(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
