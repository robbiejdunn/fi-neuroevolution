import signal

from ctypes import cdll
from enum import Enum


# Constant taken from http://linux.die.net/include/linux/prctl.h
PR_SET_PDEATHSIG = 1


class PrCtlError(Exception):
    pass


def on_parent_exit(signame):
    """
    Return a function to be run in a child process which will trigger
    SIGNAME to be sent when the parent process dies
    """
    signum = getattr(signal, signame)

    def set_parent_exit_signal():
        # http://linux.die.net/man/2/prctl
        result = cdll["libc.so.6"].prctl(PR_SET_PDEATHSIG, signum)
        if result != 0:
            raise PrCtlError("prctl failed with error code %s" % result)

    return set_parent_exit_signal


class RunningOS(Enum):
    WINDOWS = "windows"
    LINUX = "linux"
    MACOS = "macos"


if __name__ == "__main__":
    on_parent_exit("SIGTERM")
