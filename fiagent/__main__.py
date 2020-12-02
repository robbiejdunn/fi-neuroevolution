import gym

import gym_fightingice


if __name__ == "__main__":
    env = gym.make("FightingiceDisplayNoFrameskip-v0", java_env_path="/home/robbie/dev/data-science/FightingICE-neuroevolution/FTG4.50")
    #env = gym.make("FightingiceDataFrameskip-v0", java_env_path="/home/robbie/dev/data-science/FightingICE-neuroevolution/FTG4.50")
    for i_episode in range(10):
        observation = env.reset()
        for t in range(20):
            env.render()
            action = env.action_space.sample()
            observation, reward, done, info = env.step(action)
            if done:
                print("Episode finished after {} timesteps".format(t + 1))
                break
    env.close()

