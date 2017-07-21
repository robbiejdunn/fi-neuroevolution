package com.ojcoleman.ahni.experiments.fightingiceevo;

import com.ojcoleman.ahni.evaluation.HyperNEATFitnessFunction;
import com.ojcoleman.ahni.hyperneat.Properties;

/**
 * Extension of AHNI fitness function to run FightingICE program and evolve the game agent.
 * init method used to create
 *
c   */
public class TestFF extends HyperNEATFitnessFunction {

    public void init(Properties props) {
        System.out.println("init method called.");
    }

    public void initialiseEvaluation() {
        System.out.println("initialiseEvaluation method called.");
    }

    public void evaluate() {
        System.out.println("evaluate method called.");
    }
}