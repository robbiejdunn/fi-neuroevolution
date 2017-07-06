package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;

/**
 * Corresponds to tasks 1.1 and 1.2 in Oliver J. Coleman, "Evolving Neural Networks for Visual Processing",
 * Undergraduate Honours Thesis (Bachelor of Computer Science), 2010. The number of small squares is hard coded in
 * variable numSmallSquares.
 */
public class FightingICEEvolution extends HyperNEATFitnessFunction {

    public void init(Properties props) {
        super(props);
        // TODO Create files for evolution
        System.out.println("Evolution initialised");
    }

    public void initialiseEvaluation() {

    }

    public void evaluate() {
        // TODO Run FightingICE jar
    }

}