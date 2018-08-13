import subprocess
import os
import neat
import visualize
import random
import time
import sys
import random
from time import sleep
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters, get_field

global used
used = [False, False]

def close_gateway():
	gateway.close_callback_server()
	gateway.close()

def evala(genome, config):
	time.sleep(random.randrange(5))
	print(used)
	if used[0] == False:
		used[0] = True
		print(used)
		print('Evaling1')
		net = neat.nn.FeedForwardNetwork.create(genome, config)
		manager1.registerAI("NeatAI", NeatAI(gateway1, genome, net))
		game = manager1.createGame("ZEN", "ZEN", "NeatAI", "IncStage0")
		manager1.runGame(game)
		sys.stdout.flush()
		used[0] = False
	else:
		used[1] = True
		print('Evaling2')
		net = neat.nn.FeedForwardNetwork.create(genome, config)
		manager2.registerAI("NeatAI", NeatAI(gateway2, genome, net))
		game = manager2.createGame("ZEN", "ZEN", "NeatAI", "IncStage0")
		manager2.runGame(game)
		sys.stdout.flush()
		used[1] = False

	return 1.0

def eval_genomes(genomes, config):
	for genome_id, genome in genomes:
		net = neat.nn.FeedForwardNetwork.create(genome, config)
		manager.registerAI("NeatAI", NeatAI(gateway, genome, net))
		game = manager.createGame("ZEN", "ZEN", "NeatAI", "IncStage0")
		manager.runGame(game)
		sys.stdout.flush()

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
	p.add_reporter(neat.Checkpointer(2))
	x = neat.threaded.ThreadedEvaluator(2, evala)
	# x = neat.parallel.ParallelEvaluator(2, evala)
	# Run evolution for 300 generations.
	winner = p.run(x.evaluate, 5)

	# Print best genome to terminal.
	print('\nBest genome:\n{!s}'.format(winner))

	visualize.draw_net(config, winner, True)
	visualize.plot_stats(stats, ylog=False, view=True)
	visualize.plot_species(stats, view=True)
	close_gateway()

class NeatAI(object):

	def __init__(self, gateway, genome, network):
		self.gateway = gateway
		self.genome = genome
		self.network = network
		self.roundNum = 0
		self.results = [0] * 3

	def getCharacter(self):
		return "ZEN"

	def close(self):
		pass

	def getInformation(self, frameData):
		self.frameData = frameData
		self.cc.setFrameData(self.frameData, self.player)

	def initialize(self, gameData, player):
		self.inputKey = self.gateway.jvm.structs.Key()
		self.frameData = self.gateway.jvm.structs.FrameData()
		self.cc = self.gateway.jvm.commandcenter.CommandCenter()

		self.player = player
		self.gameData = gameData

		return 0

	def input(self):
		return self.inputKey

	def processing(self):
		if self.frameData.getEmptyFlag() or self.frameData.getRemainingTime() <= 0:
			return

		if self.cc.getSkillFlag():
			self.inputKey = self.cc.getSkillKey()
			return

		if self.frameData.getRemainingTimeMilliseconds() < 1000 and self.frameData.getRound() == self.roundNum:
			#hpDiff = self.cc.getMyHP() - self.cc.getEnemyHP()
			#if hpDiff >= 0:
			#	self.results[self.roundNum] = hpDiff * 1000
			#else:
			#	self.results[self.roundNum] = (1 / abs(hpDiff)) * 1000
			#
			#if self.roundNum == 2:
			#	self.genome.fitness = sum(self.results) / 3
			#	self.close()
			#
			#self.roundNum += 1
			self.results[self.roundNum] = self.cc.getMyHP() - self.cc.getEnemyHP()
			if self.roundNum == 2:
				self.genome.fitness = sum(self.results) / 3
				self.close()
			self.roundNum += 1
		if self.frameData.getFrameNumber() % 10:
			self.processResponses(self.network.activate(self.getFeatureVector()))

	def getFeatureVector(self):
		fv = [0] * 26					# Feature vector

		# Features 1-8 : positions, energy and HP
		fv[0] = ((self.cc.getMyX() + 800) / 800) - 1
		fv[1] = ((self.cc.getMyY() + 465) / 465) - 1
		fv[2] = (self.cc.getMyEnergy() / 500) - 1
		fv[3] = ((self.cc.getMyHP() + 2000) / 1000) - 1
		fv[4] = ((self.cc.getEnemyX() + 800) / 800) - 1
		fv[5] = ((self.cc.getEnemyY() + 465) / 465) - 1
		fv[6] = (self.cc.getEnemyEnergy() / 500) - 1
		fv[7] = ((self.cc.getEnemyHP() + 2000) / 1000) - 1

		# Get opponent motion data
		oppAct = self.cc.getEnemyCharacter().getAction()
		if self.player:
			oppMotion = self.gameData.getPlayerTwoMotion().elementAt(oppAct.ordinal())
		else:
			oppMotion = self.gameData.getPlayerOneMotion().elementAt(oppAct.ordinal())

		# Features 9-14 : opponent motion
		fv[8] = ((oppMotion.getAttackStartUp() / 31) * 2) - 1
		fv[9] = (oppMotion.getAttackActive() / 10) - 1
		fv[10] = 1 if oppMotion.getAttackType() == 1 else 0
		fv[11] = 1 if oppMotion.getAttackType() == 2 else 0
		fv[12] = 1 if oppMotion.getAttackType() == 3 else 0
		fv[13] = 1 if oppMotion.getAttackType() == 4 else 0

		if self.player:
			projs = self.frameData.getProjectilesByP2()
		else:
			projs = self.frameData.getProjectilesByP1()

		i = 0
		for proj in projs:
			index = 14 + (i * 4)
			hit = proj.getHitAreaNow()
			fv[index] = ((hit.getB() + 465) / 465) - 1
			fv[index + 1] = ((hit.getL() + 800) / 800) - 1
			fv[index + 2] = ((hit.getR() + 800) / 800) - 1
			fv[index + 3] = ((hit.getT() + 465) / 465) - 1
			i += 1
		return fv

	def processResponses(self, responses):
		# Softmax activation
		relvActs = []		#relevant/applicable actions based on energy
		energy = self.cc.getMyEnergy()

		if energy < 2:
			relvActs = responses[0:19]
		elif energy < 5:
			relvActs = responses[0:20]
		elif energy < 20:
			relvActs = responses[0:21]
		elif energy < 30:
			relvActs = responses[0:22]
		elif energy < 50:
			relvActs = responses[0:23]
		elif energy < 150:
			relvActs = responses[0:25]
		else:
			relvActs = responses

		mVal = max(relvActs)
		index = relvActs.index(mVal)

		# sum = 0
		# max = 0
		# for val in relvActs:
		# 	sum += val
		# for i in range(len(relvActs)):
		# 	relvActs[i] = relvActs[i] / sum
		# 	if relvActs[i] > max:
		# 		max = relvActs[i]
		# 		index = i
        #
		# visited = []
		# x = random.random()
		# while x > 0:
		# 	index = random.randint(0, (len(relvActs) - 1))
		# 	if index not in visited:
		# 		x -= relvActs[index]
		# 		visited.append(index)

		# Select action
		if index == 0:
			self.cc.commandCall("1")
		elif index == 1:
			self.cc.commandCall("2")
		elif index == 2:
			self.cc.commandCall("3")
		elif index == 3:
			self.cc.commandCall("4")
		elif index == 4:
			self.cc.commandCall("5")
		elif index == 5:
			self.cc.commandCall("6")
		elif index == 6:
			self.cc.commandCall("7")
		elif index == 7:
			self.cc.commandCall("8")
		elif index == 8:
			self.cc.commandCall("9")
		elif index == 9:
			self.cc.commandCall("A")
		elif index == 10:
			self.cc.commandCall("B")
		elif index == 11:
			self.cc.commandCall("2 _ A")
		elif index == 12:
			self.cc.commandCall("2 _ B")
		elif index == 13:
			self.cc.commandCall("6 _ A")
		elif index == 14:
			self.cc.commandCall("6 _ B")
		elif index == 15:
			self.cc.commandCall("3 _ A")
		elif index == 16:
			self.cc.commandCall("3 _ B")
		elif index == 17:
			self.cc.commandCall("6 2 3 _ A")
		elif index == 18:
			self.cc.commandCall("2 1 4 _ A")
		elif index == 19:
			self.cc.commandCall("2 3 6 _ A")
		elif index == 20:
			self.cc.commandCall("4 _ A")
		elif index == 21:
			self.cc.commandCall("4 _ B")
		elif index == 22:
			self.cc.commandCall("2 3 6 _ B")
		elif index == 23:
			self.cc.commandCall("6 2 3 _ B")
		elif index == 24:
			self.cc.commandCall("2 1 4 _ B")
		elif index == 25:
			self.cc.commandCall("2 3 6 _ C")

	class Java:
		implements = ["gameInterface.AIInterface"]


local_dir = os.path.dirname(__file__)
config_path = os.path.join(local_dir, 'config-fightingice')

gateway1 = JavaGateway(gateway_parameters=GatewayParameters(port=4000), callback_server_parameters=CallbackServerParameters(port=0))
gateway2 = JavaGateway(gateway_parameters=GatewayParameters(port=4001), callback_server_parameters=CallbackServerParameters(port=1))
python_port1 = gateway1.get_callback_server().get_listening_port()
python_port2 = gateway1.get_callback_server().get_listening_port()
gateway1.java_gateway_server.resetCallbackClient(gateway1.java_gateway_server.getCallbackClient().getAddress(), python_port1)
gateway2.java_gateway_server.resetCallbackClient(gateway2.java_gateway_server.getCallbackClient().getAddress(), python_port2)
manager1 = gateway1.entry_point
manager2 = gateway2.entry_point

if __name__ == "__main__":
	run(config_path)
