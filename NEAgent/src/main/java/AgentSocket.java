import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;



/* Singleton class for the socket object used to communicate with NN library. */
public class AgentSocket extends Socket {

    private static AgentSocket instance = null;     // Class object instance.

    private static BufferedReader socketIn = null;  // Input stream (from NN).
    private static PrintWriter socketOut = null;    // Output stream (to NN).

    private static Gson gson = null;                // Object for parsing JSON.

    protected AgentSocket() throws IOException {
        super("localhost", 4444);
    }

    public static AgentSocket getInstance() {
        if (instance == null) {
            try {
                instance = new AgentSocket();
                socketIn = new BufferedReader(new InputStreamReader(instance.getInputStream()));
                socketOut = new PrintWriter(instance.getOutputStream());
                gson = new Gson();
            } catch (IOException e) {
                System.err.println("Error initialising socket.");
                e.printStackTrace();
            }
        }
        return instance;
    }

    // Send features to the NN library.
    public static void sendFeatures(double[] features) {
        socketOut.println(gson.toJson(features));
        socketOut.flush();
    }

    // Receive responses for the agent according to the features sent.
    public static double[] getResponses() {
        double[] responses = null;
        try {
            responses = gson.fromJson(socketIn.readLine(), double[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responses;
    }

}
