from __future__ import print_function

import subprocess
import os
import neat
import visualize

import socket
import json

class ServerSocket:

    def __init__(self):
        self.s = socket.socket()
        host = 'localhost'
        port = 4444
        self.s.bind((host, port))
        self.s.listen(20)

    # accept connection from game
    def acceptCon(self):
        self.c, addr = self.s.accept()

    def receiveFeatures(self):
        return json.loads(self.c.recv(4096))

    # Converts responses to JSON format and sends.
    def sendResponses(self, responses):
        self.c.send(json.dumps(responses).encode() + b'\r\n')

    def closeSocket(self):
        self.c.close()

ss = ServerSocket()
num_rounds = 3      # Number of rounds used to evaluate network

def eval_genomes(genomes, config):
    for genome_id, genome in genomes:
        net = neat.nn.FeedForwardNetwork.create(genome, config)

        # Fitnesses for each round
        fitnesses = []

        # Run FightingICE
        p = subprocess.Popen(r'C:/Users/Robbie/Documents/FightingICE-neuroevolution/FightingICE/FIEvo.bat', creationflags=subprocess.CREATE_NEW_CONSOLE)
        ss.acceptCon()
        while (True):
            stim = ss.receiveFeatures()
            if (len(stim) == 2):
                fitnesses.append((stim[0] - stim[1] + 1000 / 2000))
                print(len(fitnesses) + ' fitness received')
                if (len(fitnesses) == 3):
                    ss.closeSocket
                    genome.fitness = sum(fitnesses) / len(fitnesses)
                    break
            ss.sendResponses(net.activate(stim))

def run(config_file):
    # Load NEAT configuration
    config = neat.Config(neat.DefaultGenome, neat.DefaultReproduction,
        neat.DefaultSpeciesSet, neat.DefaultStagnation,
        config_file)

    # Create the population.
    p = neat.Population(config)

    # stdout reporter to show progress in terminal.
    p.add_reporter(neat.StdOutReporter(True))
    stats = neat.StatisticsReporter()
    # Reporter to write training/evolution statistics to files.
    p.add_reporter(stats)
    # Checkpointer to write evolution state to file every 5 gens.
    p.add_reporter(neat.Checkpointer(5))

    # Run evolution for 300 generations.
    winner = p.run(eval_genomes, 5)

    # Print best genome to terminal.
    print('\nBest genome:\n{!s}'.format(winner))

    visualize.draw_net(config, winner, True)
    visualize.plot_stats(stats, ylog=False, view=True)
    visualize.plot_species(stats, view=True)

if __name__ == '__main__':
    # Determine path to configuration file. This path manipulation is
    # here so that the script will run successfully regardless of the
    # current working directory.
    local_dir = os.path.dirname(__file__)
    config_path = os.path.join(local_dir, 'config-fightingice')
    run(config_path)
