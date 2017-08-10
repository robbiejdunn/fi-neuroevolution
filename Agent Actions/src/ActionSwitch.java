/**
* Created by robbi on 09/06/2017.
*/

import commandcenter.CommandCenter;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import gameInterface.AIInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ActionSwitch implements AIInterface {
    GameData gd;
    Key inputKey;
    CommandCenter cc;
    FrameData fd;
    boolean p;
    int action;
    String actionString;

    @Override
    public void close() {

    }

    @Override
    public void getInformation(FrameData frameData) {
        fd = frameData;
        cc.setFrameData(fd, p);
    }

    @Override
    public int initialize(GameData gameData,boolean playerNumber) {
        gd = gameData;
        p = playerNumber;

        inputKey = new Key();
        fd = new FrameData();
        cc = new CommandCenter();

        File f = new File("data/aiData/Switch/signal.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            action = Integer.parseInt(br.readLine());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        switch (action) {
            case 1: actionString = "THROW_A";
                    break;
            case 2: actionString = "THROW_B";
                    break;
            case 3: actionString = "STAND_A";
                    break;
            case 4: actionString = "STAND_B";
                    break;
            case 5: actionString = "CROUCH_A";
                    break;
            case 6: actionString = "STAND_D_DF_FC";
        }

        System.out.println("Action " + actionString + " selected.");
        return 0;
    }

    @Override
    public Key input() {
        return inputKey;
    }

    @Override
    public void processing() {
        if(!fd.getEmptyFlag() && fd.getRemainingTimeMilliseconds() > 0) {
            if (cc.getSkillFlag()) {
                inputKey = cc.getSkillKey();
                System.out.println("Action completed.");
            }
            else if (fd.getFrameNumber() == 200)
                System.out.println("3...");
            else if (fd.getFrameNumber() == 300)
                System.out.println("2...");
            else if (fd.getFrameNumber() == 400)
                System.out.println("1...");
            else if (fd.getFrameNumber() == 500) {
                inputKey.empty();
                cc.skillCancel();
                cc.commandCall(actionString);
                inputKey = cc.getSkillKey();
            }
        }
    }

    public String getCharacter(){
        return CHARACTER_ZEN;
    }

}
