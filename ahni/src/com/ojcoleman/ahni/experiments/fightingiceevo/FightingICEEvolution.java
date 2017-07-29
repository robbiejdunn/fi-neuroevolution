package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.anji.integration.Activator;
import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;
import com.ojcoleman.ahni.hyperneat.Properties;
import org.jgapcustomised.Chromosome;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

//
//
// * Extension of AHNI fitness function to run FightingICE program and evolve the game agent.
// * init method used to create
// *
// * @author Robbie Dunn
//

public class FightingICEEvolution extends HyperNEATFitnessFunction {

    ServerSocket ss;    // server socket
    Socket cs;          // client socket

    public void init(Properties props) {
        super.init(props);

        initServerSocket();

        // TODO Create files for evolution
        System.out.println("Evolution initialised");
    }

    private void initServerSocket() {
        try {
            ss = new ServerSocket(4444);
            System.out.println("Server socket initialised");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected double evaluate(Chromosome genotype, Activator activator, int threadIndex) {
        System.out.println("Genotype evaluation beginning");
        double fitness = 0;

        try {
            System.out.println("Beginning FightingICE process");
            // Begin FightingICE in preparation for genotype evaluation
            // TODO Replace with command that runs in non-windowed etc
//            ProcessBuilder builder = new ProcessBuilder(
//                    "cmd.exe", "/c", "java FightingICE.jar"
//            );
//            builder.redirectErrorStream(true);
//            Process p = builder.start();
            Runtime.getRuntime().exec("cmd /c cd ../FightingICE & start FIEvo.bat");

            System.out.println("Awaiting client socket connection");
            // Accepts socket connection from client (agent)
            cs = ss.accept();
            ObjectInputStream in = new ObjectInputStream(cs.getInputStream());
            PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
//            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            // TODO Adjust timeout length
            System.out.println("Client socket connected");
            while (true) {
                double[] stimuli;
                if ((stimuli = (double[]) in.readObject()) != null) {
                    System.out.println("Array received: " + Arrays.toString(stimuli));
                    System.out.println("Activator inputs " + activator.getInputDimension().toString() +
                        ", count " + activator.getInputCount());
                    System.out.println("Act outputs " + activator.getOutputDimension().toString() +
                        ", count " + activator.getOutputCount());
                    double[] response = activator.next(stimuli);
                    System.out.println("Response: " + Arrays.toString(response));
                }
//                String line;
//                if ((line = in.readLine()) != null) {
//                    if (line.equals("fin")) {
//                        System.out.println("Genotype evaluation complete");
//                        break;
//                    }
//                    else    {
//                        System.out.println("Reading inputs");
//                        double[] inputs = new double[9];
//
//                        line.replaceAll("[\\[\\]]", "");
//                        System.out.println(line);
//                        String[] values = line.split(",");
//                        System.out.println(line);
//                        for (int i = 0; i < inputs.length; i++) {
//                            inputs[i] = Double.parseDouble(values[i]);
//                            System.out.println("Input " + i + ": " + inputs[i]);
//                        }
//                        double[] output = activator.next(inputs);
//                        System.out.println(Arrays.toString(output));
//                    }
//
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        genotype.setPerformanceValue(fitness);
        return fitness;
    }

}

