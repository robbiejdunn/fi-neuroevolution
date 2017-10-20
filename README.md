# FightingICE-neuroevolution
Implementing an agent using neuroevolution in the FightingICE game framework.

## Installation
In order to run an evolution on your own machine follow the steps below.


## Implementation

### Features

| ID | Feature | Range | Comments |
| --- | --- | --- | --- |
| 1 | Agent X Position | -800/800 | -800 at far left of arena and 800 at the right. |
| 2 | Agent Y Position | -465/465 | -465 when standing or crouching on ground. 465 at highest point of jump. |
| 3 | Agent Energy | 0/1000 | Amount of energy the agent has. Capped at 1000. |
| 4 | Agent Hitpoints | -2000/0 | -2000 assumed to be maximum damage taken. 0 when agent is at full hitpoints. |
| 5 | Enemy X position | -800/800 | As feature 1. |
| 6 | Enemy Y Position | -465/465 | As feature 2. |
| 7 | Enemy Energy | 0/100 | As feature 3. |
| 8 | Enemy HP | -2000/0 | As feature 4. |
| 9 | Is opponent using attack? | false/true | -1 for false, 1 for true. |
| 10 | Opponent attack startup frames | 0/31 | -1 used for all following features when attack is not being actioned. |
| 11 | Opponent attack active frames | 0/20 | |
| 12 | Attack type high? | false/true | Features 12-15 only one can be true at a point in time. |
| 13 | Attack type mid? | false/true | |
| 14 | Attack type low? | false/true | |
| 15 | Attack type throw? | false/true | |