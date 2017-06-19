import commandcenter.CommandCenter;
import enumerate.Action;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;

/**
 * NEAgent.java
 * Controller class for the FightingICE neuroevolution agent. Implements AIInterface.
 *
 * Created by robbi on 09/06/2017.
 */
public class NEAgent implements AIInterface {

    boolean p;
    GameData gd;
    Key inputKey;
    FrameData fd;
    CommandCenter cc;

    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        gd = gameData;
        p = playerNumber;

        inputKey = new Key();
        fd = new FrameData();
        cc = new CommandCenter();

        return 0;
    }

    @Override
    public void getInformation(FrameData frameData) {
        // TODO Auto-generated method stub
        fd = frameData;
        cc.setFrameData(fd, p);
    }

    private double[] getNormalisedInputs() {
        double[] in = new double[10];

        Action oppAct = cc.getEnemyCharacter().getAction();
        MotionData oppMotion;
        if (p)
            oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());
        else
            oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());

        if (oppMotion.attackDownProperty)

        in[0] = cc.getMyX();
        in[1] = cc.getMyY();
        in[2] = cc.getEnemyX();
        in[3] = cc.getEnemyY();
        in[4] = cc.getEnemyEnergy();

        //
        oppMotion.
        in[5] = cc.getEnemy
        return in;
    }

    @Override
    public void processing() {
        if(!fd.getEmptyFlag()){
            if(fd.getRemainingTimeMilliseconds() > 0){
                double[] in = getNormalisedInputs();
                //  In order to get CancelAbleFrame's information on the current action of the opponent character, first you write as follows:
                Action oppAct = cc.getEnemyCharacter().getAction();
                // If you want the same information on a specific action, say "STAND_A", you can simply write:
                // Action action = Action.STAND_A;

                // Next, get the MotionData information on the opponent character's action of interest from GameData.
                // You can access the MotionData information with
                // gd.getPlayer???Motion.elementAt("an instance of action (e.g., oppAct or action)".ordinal())
                MotionData oppMotion = new MotionData();
                if(p)oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());
                else oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());

                System.out.println(oppMotion.getMotionName()+":cancelable " + oppMotion.getCancelAbleFrame() + " frame.");
            }
        }
    }

    @Override
    public Key input() {
        return inputKey;
    }

    @Override
    public void close() {

    }

    public String getCharacter(){
        return CHARACTER_ZEN;
    }

}
