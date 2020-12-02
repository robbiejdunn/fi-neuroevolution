import gym
import os
import platform

from py4j.java_gateway import CallbackServerParameters, GatewayParameters, JavaGateway

from fiagent.env.action import ACTIONS
from fiagent.env.agents.machete import Machete
from fiagent.env.utils import RunningOS


class FightingICETrain(gym.Env):
    def __init__(self):
        print("Initialising fightingice gym environment")
        self.observation_space = gym.spaces.Box(low=0, high=1, shape=(143,))
        self.action_space = gym.spaces.Discrete(len(ACTIONS))
        
        file_path = os.path.realpath(__file__)
        java_env_path = os.path.abspath(os.path.join(file_path, "../../../FTG4.50"))
        print("Java env path = {}".format(java_env_path))
        port = 4296
        print("Java port = {}".format(port))

        # determine running OS- needed for finding platform specific files
        os_name = platform.system()
        if os_name.startswith("Linux"):
            os_type = RunningOS.LINUX
        elif os_name.startswith("Darwin"):
            os_type = RunningOS.MACOS
        elif os_name.startswith("Window"):
            os_type = RunningOS.WINDOWS
        else:
            raise SystemError("Unsupported OS: {}".format(os_name))
        print("Running on OS = {}".format(os_type.value))
        
        jar_path, lib_path, lwjgl_path, system_lib_path, ai_path = self._get_paths(java_env_path, os_type)
        
        classpath_items = [jar_path, lwjgl_path, system_lib_path, lib_path, ai_path]
        if os_type is RunningOS.WINDOWS:
            classpath_str = ";".joion(classpath_items)
        else:
            classpath_str = ":".join(classpath_items)
        print("Classpath string = {}".format(classpath_str))

        self._launch_game(classpath_str, port)
    
    @staticmethod
    def _launch_game(classpath: str, port: int):
        javaopts = ["Main", "--port", "4242", "--py4j", "--fastmode", "--grey-bg", "--inverted-player", "1", "--mute"]
        print("Javaopts = {}".format(javaopts))
        jg = JavaGateway.launch_gateway(classpath=classpath, port=4242, javaopts=javaopts, die_on_exit=True)
        manager = jg.entry_point

        game_to_start = manager.createGame("ZEN", "ZEN", Machete, Machete, 3)

        manager.runGame(game_to_start)
        print(jg)

    @staticmethod
    def _get_paths(java_env_path: str, os_type: RunningOS):
        """Determines the paths required for running the game"""
        jar_path = os.path.join(java_env_path, "FightingICE.jar")
        print("JAR path = {}".format(jar_path))
        data_path = os.path.join(java_env_path, "data")
        print("Data path = {}".format(data_path))
        lib_path = os.path.join(java_env_path, "lib", "*")
        print("Lib path = {}".format(lib_path))
        lwjgl_path = os.path.join(java_env_path, "lwjgl", "*")
        print("LWJGL path = {}".format(lwjgl_path))
        system_lib_path = os.path.join(java_env_path, "lib", "natives", os_type.value, "*")
        print("System lib path = {}".format(system_lib_path))
        ai_path = os.path.join(java_env_path, "data", "ai", "*")
        print("AI path = {}".format(ai_path))
        return jar_path, lib_path, lwjgl_path, system_lib_path, ai_path
        
    def reset(self):
        # start game
        return self._get_obs()

    def close(self):
        print("Closing environment")
        self.gateway.shutdown()


if __name__ == "__main__":
    FightingICETrain()
