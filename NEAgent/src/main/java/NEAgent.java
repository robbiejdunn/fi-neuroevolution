import commandcenter.CommandCenter;
import enumerate.Action;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;

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

    private AgentSocket sock;

    boolean p;          // player number (bool)
    GameData gd;        // game data
    Key inputKey;       // input key (keys pressed in game)
    FrameData fd;       // frame data
    CommandCenter cc;   // command center

    int roundNum;

    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        gd = gameData;
        p = playerNumber;

        inputKey = new Key();
        fd = new FrameData();
        cc = new CommandCenter();

        roundNum = 0;

        // Set up the socket for NN communication.
        sock = AgentSocket.getInstance();

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
        if (fd.getRemainingTimeMilliseconds() < 1000 && fd.getRound() == roundNum) {
            double[] results = new double[3];
            results[0] = fd.getRound();
            results[1] = cc.getMyHP();
            results[2] = cc.getEnemyHP();
            sock.sendFeatures(results);
            if (fd.getRound() == 2) {
                close();
            }
            roundNum++;
        }
        else if (!fd.getEmptyFlag()) {
            if (fd.getRemainingTimeMilliseconds() > 0) {

                if (cc.getSkillFlag()) {
                    inputKey = cc.getSkillKey();
                } else {
                    inputKey.empty();
                    cc.skillCancel();
                    double[] stimuli = getNormalisedInputs();
                    try {
                        // Sending inputs
                        sock.sendFeatures(stimuli);
                        processResponses(sock.getResponses());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
        System.exit(1);
    }

    public String getCharacter(){
        return CHARACTER_ZEN;
    }

    /**
     * Retrieves the neural network inputs from the game and normalises
     * them to a range (-1,1)
     */
    private double[] getNormalisedInputs() {
        double[] in = new double[15];        // normalised inputs for the neural net

        // NEURAL NETWORK INPUTS (min possible value/max value)
        // TODO Test all lower and upper bounds
        // 1. Agent X position (-800/800)
        // TODO Could use just distances ? (since actions are reversed depending on side)
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

        // MOTION DATA
        Action oppAct = cc.getEnemyCharacter().getAction();
        MotionData oppMotion;
        if (p)
            oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());
        else
            oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());

        // 9. Is opponent using an attack (false/true)
        if (oppMotion.getAttackStartUp() > 0 || oppMotion.getAttackActive() > 0)
            in[8] = 1;
        else
            in[8] = -1;
        // 10. Opponent attack startup frames (0/31)
        in[9] = (((double) oppMotion.getAttackStartUp() / (double) 31) * 2) - 1;
        // 11. Opponent attack active frames (0/20)
        in[10] = ((double) oppMotion.getAttackActive() / (double) 10) - 1;
        // 12. Attack type- high
        if (oppMotion.getAttackType() == 1)     // 1-high,2-mid,3-low,4-throw
            in[11] = 1;
        else
            in[11] = -1;
        // 13. Attack type- middle
        if (oppMotion.getAttackType() == 2)
            in[12] = 1;
        else
            in[12] = -1;
        // 14. Attack type- low
        if (oppMotion.getAttackType() == 3)
            in[13] = 1;
        else
            in[13] = -1;
        // 15. Attack type- throw
        if (oppMotion.getAttackType() == 4)
            in[14] = 1;
        else
            in[14] = -1;

        // TODO Extend with motion data inputs & projectiles etc.
        // TODO Extend with input to determine enemy character?
        return in;
    }

    private void processResponses(double[] responses) {
        inputKey.empty();
        // Find max value in array
        double max = -1;
        int maxInd = -1;
        System.out.println(responses.length + " output.");
        for (int i = 0; i < responses.length; i++) {
//            System.out.println("Response " + i + " = " + responses[i]);
            if (responses[i] > max) {

                max = responses[i];
                maxInd = i;
//                System.out.println(maxInd);
            }

        }
        System.out.println(maxInd);
        switch (maxInd) {
            case 0 : cc.commandCall("4 _ A");
                break;
            case 1 : cc.commandCall("4 _ B");
                break;
            case 2: cc.commandCall("A");
                break;
            case 3: cc.commandCall("B");
                break;
            case 4: cc.commandCall("2 _ A");
                break;
            case 5: cc.commandCall("6 _ A");
                break;
            case 6: cc.commandCall("6 _ B");
                break;
            case 7: cc.commandCall("3 _ A");
                break;
            case 8: cc.commandCall("3 _ B");
                break;
            case 9: cc.commandCall("2 3 6 _ A");
                break;
            case 10: cc.commandCall("2 3 6 _ B");
                break;
            case 11: cc.commandCall("2 3 6 _ C");
                break;
            case 12: cc.commandCall("6 2 3 _ A");
                break;
            case 13: cc.commandCall("6 2 3 _ B");
                break;
            case 14: cc.commandCall("2 1 4 _ A");
                break;
            case 15: cc.commandCall("2 1 4 _ B");
                break;
            // MOVEMENT ACTIONS
            case 16: cc.commandCall("1");
                break;
            case 17: cc.commandCall("2");
                break;
            case 18: cc.commandCall("3");
                break;
            case 19: cc.commandCall("4");
                break;
            case 20: cc.commandCall("5");
                break;
            case 21: cc.commandCall("6");
                break;
            case 22: cc.commandCall("7");
                break;
            case 23: cc.commandCall("8");
                break;
            case 24: cc.commandCall("9");
                break;
        }
    }

}