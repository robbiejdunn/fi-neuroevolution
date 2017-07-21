package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;

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
        super(props);

        try {
            File f = new File(".\\inputs-outputs.txt");

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

    private void initServerSocket() throws IOException {
        try {
            ss = new ServerSocket(4444);
        }
    }

    public void evaluate() throws Exception {
        // FROM https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
        cs = ss.accept();
        PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
        BufferedReader in = new BufferedReader(cs.getInputStream());
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "java FightingICE.jar"
        );
        builder.redirectErrorStream(true);
        Process p = builder.start();
        // Receive inputs from game
        double[] nnin;
        while ((input = in.readLine()) != null) {
            // TODO if array of doubles then continue
            splitstr = input.split(" ");
            for (int i = 0; i < splitstr.length; i++) {
                nnin[i] = Double.parseDouble(splitstr[i]);
            }
        }

        // Feed through network

        // Send output back to game
        double[] nnout;
        out.println(nnout.toString());

        // Write inputs/outputs to file
    }

}

