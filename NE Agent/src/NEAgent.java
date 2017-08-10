import commandcenter.CommandCenter;
import enumerate.Action;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * NEAgent.java
 * Controller class for the FightingICE neuroevolution agent. Implements AIInterface in
 * order to communicate with the FightingICE framework. Communicates with process running
 * AHNI neural network library to send input and receive corresponding network output.
 * Communication using server and client socket.
 *
 * Created by Robbie on 09/06/2017.
 */
public class NEAgent implements AIInterface {

    // Socket for communication with AHNI
    private Socket socket;              // client socket to comm with ahni server socket
    private BufferedReader socketIn;    // read messages received from server
//    private PrintWriter socketOut;      // send messages to server socket
    private ObjectOutputStream socketOut;

    boolean p;          // player number (bool)
    GameData gd;        // game data
    Key inputKey;       // input key (keys pressed in game)
    FrameData fd;       // frame data
    CommandCenter cc;   // command center

    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        gd = gameData;
        p = playerNumber;

        inputKey = new Key();
        fd = new FrameData();
        cc = new CommandCenter();

        initClientSocket();

        System.out.println("Agent initialised");
        return 0;
    }

    @Override
    public void getInformation(FrameData frameData) {
        fd = frameData;
        cc.setFrameData(fd, p);
    }

    @Override
    public void processing() {
        if (!fd.getEmptyFlag()) {
            if (fd.getRemainingTimeMilliseconds() > 0) {
                if ((fd.getRemainingFramesNumber() % 10) == 0 && fd.getRemainingFramesNumber() != 0) {
                    // End evolution
                    if (fd.getRemainingFramesNumber() == 10) {
                        try {
                            // Send fitness to AHNI (1000 is max health loss)
                            socketOut.writeInt((cc.getMyHP() - cc.getEnemyHP() + 1000));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // Sending inputs
                    double[] stimuli = getNormalisedInputs();
                    try {
                        socketOut.writeObject(stimuli);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

//                System.out.println(Arrays.toString(getNormalisedInputs()));
//                socketOut.println(Arrays.toString(in));

//                //  In order to get CancelAbleFrame's information on the current action of the opponent character, first you write as follows:
//                Action oppAct = cc.getEnemyCharacter().getAction();
//                // If you want the same information on a specific action, say "STAND_A", you can simply write:
//                // Action action = Action.STAND_A;
//
//                // Next, get the MotionData information on the opponent character's action of interest from GameData.
//                // You can access the MotionData information with
//                // gd.getPlayer???Motion.elementAt("an instance of action (e.g., oppAct or action)".ordinal())
//                MotionData oppMotion = new MotionData();
//                if(p)oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());
//                else oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());
//
//                System.out.println(oppMotion.getMotionName()+":cancelable " + oppMotion.getCancelAbleFrame() + " frame.");
            }
        }
    }

    @Override
    public Key input() {
        return inputKey;
    }

    @Override
    public void close() {
//        socketOut.println("fin");              // end AHNI evaluation
        System.out.println("Game closed.");
    }

    public String getCharacter(){
        return CHARACTER_ZEN;
    }

    /**
     * Initialises the socket used for communication with the AHNI process
     */
    private void initClientSocket() {
        try {
            System.out.println("Initialising client socket...");
            socket = new Socket("localhost", 4444);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketOut = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Client socket initialised.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Unknown host provided for socket.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO error.");
            System.exit(1);
        }
    }

    /**
     * Retrieves the neural network inputs from the game and normalises
     * them to a range (-1,1)
     */
    private double[] getNormalisedInputs() {
        double[] in = new double[9];        // normalised inputs for the neural net

        // NEURAL NETWORK INPUTS (min possible value/max value)
        // TODO Test all lower and upper bounds
        // 1. Agent X position (-800/800)
        in[0] = ((double) (cc.getMyX() + 800) / (double) 800) - 1;
        // 2. Agent Y position (-465/465)
        in[1] = ((double) (cc.getMyY() + 465) / (double) 465) - 1;
        // 3. Agent energy (0/1000)
        in[2] = ((double) cc.getMyEnergy() / (double) 500) - 1;
        // 4. Agent HP (-2000/0)
        // TODO Test lowest possible hp from round (-2000 assumed currently)
        in[3] = ((double) (cc.getMyHP() + 2000) / (double) 1000) - 1;
        // 5. Enemy X position (-800/800)
        in[4] = ((double) (cc.getEnemyX() + 800) / (double) 800) - 1;
        // 6. Enemy Y position (-465/465)
        in[5] = ((double) (cc.getEnemyY() + 465) / (double) 465) - 1;
        // 7. Enemy Energy (0/1000)
        in[6] = ((double) cc.getEnemyEnergy() / (double) 500) - 1;
        // 8. Enemy HP (-2000/0)
        in[7] = ((double) (cc.getEnemyHP() + 2000) / (double) 1000) - 1;
        // 9. Is skill in use? (false/true)
        if (cc.getSkillFlag() == true)
            in[8] = 1.0;
        else
            in[8] = -1.0;
        // TODO Extend with motion data inputs & projectiles etc.
        // TODO Extend with input to determine enemy character?
        return in;
    }

    private void setActions(double[] response) {
        for (double r : response) {

        }
    }

}

//    Action oppAct = cc.getEnemyCharacter().getAction();
//    MotionData oppMotion;
//        if (p)
//                oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());
//                else
//                oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());