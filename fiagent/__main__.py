import gym
import neat
import os

from gym.envs.registration import register


register(id="FightingICETrain-v0", entry_point="fiagent.env:FightingICETrain")
env = gym.make("FightingICETrain-v0")


def eval_genomes(genomes, config):
    for genome_id, genome in genomes:
        print("Processing genome {}".format(genome_id))
        genome.fitness = 0.0
        net = neat.nn.FeedForwardNetwork.create(genome, config)
        obs = env.reset()
        done = False
        while not done:
            # nn_output = net.activate(obs)
            # action = nn_output.index(max(nn_output))
            obs, reward, done, info = env.step(1)
            genome.fitness += reward
        print("Genome {} processed. Fitness = {}".format(genome_id, genome.fitness))


if __name__ == "__main__":
    local_dir = os.path.dirname(__file__)
    config_path = os.path.join(local_dir, "resources", "neat-config")
    config = neat.Config(
        neat.DefaultGenome,
        neat.DefaultReproduction,
        neat.DefaultSpeciesSet,
        neat.DefaultStagnation,
        config_path,
    )
    p = neat.Population(config)

    p.add_reporter(neat.StdOutReporter(True))
    stats = neat.StatisticsReporter()
    p.add_reporter(stats)
    p.add_reporter(neat.Checkpointer(5))

    winner = p.run(eval_genomes, 4)
