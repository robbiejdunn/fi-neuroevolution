import gym
import os
import platform
import time

from multiprocessing import Pipe
from py4j.java_gateway import CallbackServerParameters, GatewayParameters, JavaGateway
from subprocess import PIPE, Popen
from threading import Thread

from fiagent.env.gym_ai import GymAI
from fiagent.env.action import ACTIONS
from fiagent.env.agents.machete import Machete
from fiagent.env.utils import on_parent_exit, RunningOS


def game_thread(env):
    try:
        env.manager.runGame(env.game_to_start)
    except Exception as e:
        print("EXCEPTION: {}".format(e))


class FightingICETrain(gym.Env):
    def __init__(self):
        print("Initialising fightingice gym environment")
        self.observation_space = gym.spaces.Box(low=0, high=1, shape=(143,))
        self.action_space = gym.spaces.Discrete(len(ACTIONS))

        file_path = os.path.realpath(__file__)
        java_env_path = os.path.abspath(os.path.join(file_path, "../../../FTG4.50"))
        print("Java env path = {}".format(java_env_path))
        self.port = 4296
        print("Java port = {}".format(self.port))

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

        jar_path, lwjgl_path, system_lib_path, lib_path, ai_path = self._get_paths(
            java_env_path, os_type
        )

        classpath_items = [jar_path, lwjgl_path, system_lib_path, lib_path, ai_path]
        if os_type is RunningOS.WINDOWS:
            classpath_str = ";".join(classpath_items)
        else:
            classpath_str = ":".join(classpath_items)
        print("Classpath string = {}".format(classpath_str))

        java_home = os.environ.get("JAVA_HOME")
        if java_home:
            java_path = os.path.join(java_home, "bin", "java")
            print("Java path = {}".format(java_path))
        else:
            print('$JAVA_HOME not set, attempting to use "java"')
            java_path = "java"
        javaopts = [
            "Main",
            "--port",
            str(self.port),
            "--py4j",
            "--fastmode",
            "--disable-window",
            "--inverted-player",
            "1",
        ]
        print("Javaopts = {}".format(javaopts))
        self.command = [java_path, "-classpath", classpath_str] + javaopts
        print("Command = {}".format(self.command))
        # TODO: this can be improved so that it takes relative path from project root
        self.fi_dir = os.path.abspath(
            os.path.join(os.path.realpath(__file__), "..", "..", "..", "FTG4.50")
        )
        self._launch_game()

    def _launch_game(self):
        print("Creating subprocess with working directory = {}".format(self.fi_dir))
        self.proc = Popen(
            self.command, stdout=PIPE, preexec_fn=on_parent_exit("SIGTERM"), cwd=self.fi_dir
        )
        time.sleep(5)
        self.java_gateway = JavaGateway(
            java_process=self.proc,
            gateway_parameters=GatewayParameters(port=self.port),
            callback_server_parameters=CallbackServerParameters(port=0),
        )
        python_port = self.java_gateway.get_callback_server().get_listening_port()
        self.java_gateway.java_gateway_server.resetCallbackClient(
                self.java_gateway.java_gateway_server.getCallbackClient().getAddress(),
            python_port,
        )
        print("Creating manager")
        self.manager = self.java_gateway.entry_point
        print("Creating Machete AI")
        machete_ai = Machete(self.java_gateway)
        print("Registering Machete AI")
        machete_cname = machete_ai.__class__.__name__
        self.manager.registerAI(machete_cname, machete_ai)
        print("Registering Mercy AI")
        server, client = Pipe()
        self.pipe = server
        self.p1 = GymAI(self.java_gateway, client, False)
        self.manager.registerAI(self.p1.__class__.__name__, self.p1)
        print("Creating game")
        self.game_to_start = self.manager.createGame(
            "ZEN", "ZEN", self.p1.__class__.__name__, machete_cname, 3
        )
        print("Running game")
        self.game_thread = Thread(target=game_thread, name="game_thread", args=(self,))
        self.game_thread.start()

    @staticmethod
    def _get_paths(java_env_path: str, os_type: RunningOS):
        """Determines the paths required for running the game"""
        jar_path = os.path.join(java_env_path, "FightingICE.jar")
        print("JAR path = {}".format(jar_path))
        data_path = os.path.join(java_env_path, "data")
        print("Data path = {}".format(data_path))
        lib_path = os.path.join(java_env_path, "lib")
        print("Lib path = {}".format(lib_path))
        system_lib_path = os.path.join(lib_path, "natives", os_type.value)
        print("System lib path = {}".format(system_lib_path))
        ai_path = os.path.join(java_env_path, "data", "ai")
        print("AI path = {}".format(ai_path))
        lwjgl_path = os.path.join(lib_path, "lwjgl")
        print("LWJGL path = {}".format(lwjgl_path))
        for path in [
            jar_path,
            data_path,
            lib_path,
            system_lib_path,
            ai_path,
            lwjgl_path,
        ]:
            if not os.path.exists(path):
                # TODO: use a better error type
                raise SystemError(
                    "Path {} not found. Check FightingICE installation".format(path)
                )
        lwjgl_all = os.path.join(lwjgl_path, "*")
        lib_all = os.path.join(lib_path, "*")
        system_lib_all = os.path.join(system_lib_path, "*")
        ai_all = os.path.join(ai_path, "*")
        return jar_path, lwjgl_all, system_lib_all, lib_all, ai_all

    def reset(self):
        self.pipe.send("reset")
        obs = self.pipe.recv()
        return obs


    def close(self):
        print("Closing environment")
        self.gateway.shutdown()

    def step(self, action):
        self.pipe.send(["step", action])
        new_obs, reward, done, info = self.pipe.recv()
        return new_obs, reward, done, {}


if __name__ == "__main__":
    FightingICETrain()
