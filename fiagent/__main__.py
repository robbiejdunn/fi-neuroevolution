import gym

from gym.envs.registration import register


if __name__ == "__main__":
    register(id="FightingICETrain-v0", entry_point="fiagent.env:FightingICETrain")
    env = gym.make("FightingICETrain-v0")
    env.reset()
    for _ in range(100):
        env.step(env.action_space.sample())
    exit()

