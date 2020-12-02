import os
import re

from setuptools import setup, find_packages


here_path = os.path.abspath(os.path.dirname(__file__))


with open(os.path.join(here_path, "fiagent/__init__.py"), encoding="utf8") as f:
    version = re.search(r'__version__ = "(.*?)"', f.read()).group(1)


setup(
    name="fiagent",
    version=version,
    packages=find_packages()
)

