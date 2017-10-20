package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.anji.integration.Activator;
import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;
import com.ojcoleman.ahni.hyperneat.Properties;
import org.jgapcustomised.Chromosome;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by robbi on 14/08/2017.
 */
public class FightingICETest extends HyperNEATFitnessFunction {

    ServerSocket ss;
    Socket cs;

    public void init(Properties props) {
        super.init(props);
        initServerSocket();
        System.out.println("Evolution initialised.");
    }

    protected double evaluate(Chromosome genotype, Activator activator, int threadIndex) {
        try {
//            FileWriter fwIn = new FileWriter("/in.txt", false);
//            FileWriter fwOut = new FileWriter("/out.txt", false);
//            PrintWriter pwIn = new PrintWriter(fwIn);
//            PrintWriter pwOut = new PrintWriter(fwOut);

            double fitness;
            System.out.println("Evaluating chromosome.");

            // Used to check whether activator works (if it is first chromosome)
            double[] testIns = {0.0, 1.0, 0.0, 0.699, 0.33, 0.66, 0.22, 0.22, 0.1};
            double[] testOuts = activator.next(testIns);
            if (testOuts[0] == 0) {
                System.out.println("First chromosome...");
                genotype.setPerformanceValue(0);
                return 0;
            }


            Runtime.getRuntime().exec("cmd /c cd ../FightingICE & start FIEvo.bat");

            System.out.println("Awaiting client socket connection.");
            // Accepts socket connection from client (agent)
            cs = ss.accept();
            System.out.println("Client socket connected.");
            ObjectInputStream in = new ObjectInputStream(cs.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(cs.getOutputStream());
            double[]  stimuli;
            while (true) {
                Object objin = in.readObject();
                if ((stimuli = (double[]) objin) != null) {
                    if (stimuli.length == 1) {
                        System.out.println("Round finished");
                        System.out.println("Fitness = " + stimuli[0]);
                        fitness = stimuli[0];
                        genotype.setPerformanceValue(fitness);
//                        pwIn.close();
//                        pwOut.close();
                        return fitness;
                    }
                    double[] testOut = activator.next(stimuli);
                    System.out.println("Responses: " + Arrays.toString(testOut));
//                    pwIn.println(Arrays.toString(stimuli));
//                    pwOut.println(Arrays.toString(testOut));
                    out.writeObject(testOut);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        genotype.setPerformanceValue(0.5);
        return 0.5;
    }

    /* Initialises server socket for communication with FightingICE */
    private void initServerSocket() {
        try {
            ss = new ServerSocket(4444);
            System.out.println("Server socket initialised.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}