package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;

/**
 * Extension of AHNI fitness function to run FightingICE program and evolve the game agent.
 * init method used to create
 *
 * @author Robbie Dunn
 */
public class FightingICEEvolution extends HyperNEATFitnessFunction {

    public void init(Properties props) {
        super(props);

        try {
            File f = new File(".\\test.txt");

            if (f.createNewFile()) {
                System.out.println("New file created");
            } else {
                System.out.println("File already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO Create files for evolution
        System.out.println("Evolution initialised");
    }

    public void initialiseEvaluation() {

    }

    public void evaluate() throws Exception {
        // FROM https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "java"
        );
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
    }

}