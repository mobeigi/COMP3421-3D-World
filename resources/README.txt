COMP3421 16s2 Assignment 2
Mohammad Ghasembeigi (z3464208)

List of Extensions attempted:

- Build a complex model or a model with walking animation or something beautiful or interesting for your avatar or your others! (2..4 marks)

I added various interesting things for the avatar/others.
Avatar has a player name that the user provided and is displayed above them in third person mode using bitmap text. See Avatar.java
The enemy (others) has a bouncing animation based on the motion of a positive sine wave (see Enemy.java).
The enemy can attack the player! If the player gets too close to the enemy, the enemy will look at the player and start patching to it.
If the enemy bounces on the enemy (the Y axis has to be on the ground) then the enemy will kill the player and its game over! (See checkEnemyBehaviour() in Game.java)

- Add a 'night' mode with low ambient lighting. Give the player a torch which shines in the direction they are facing. (2 marks)

Night mode toggleable by pressing 'n'.
Low ambient lighting is present.
Player uses a torch (implemented as spotlight) to look around in the direction they are facing.
See setupTorch() in Game.java. Track variable nightMode for other uses.

- Add Portal style portals. Portals you can walk through (4 marks)

Portals are defined in portal pairs. Ie 2 portals belong to one portal pair.
The portals in a portal pair are interlinked and can teleport the player to the other portal in the pair.
There can be many portal pairs.
Scene language is used to define portals in the terrain.
Both the avatar (player) and enemy (others) can use portals.
See PortalPair.java, Portal.java.