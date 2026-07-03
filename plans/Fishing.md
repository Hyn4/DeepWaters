# HOW FISHING WORKS
### The Player must have:
 - A fishing rod
 - Access to a body of water
## 1. Throw the bobber
    The player holds left-click while holding the fishing rod, this will start the
    animation for throwing the bobber, after it ends the bobber spawns and flies
    in an arc until it hits a liquid. After it spawns it is linked to the player.
### Conditions:
 - The bobber must despawn if it hits a solid block
 - The bobber must stop moving if it hits a liquid
 - The bobber must despawn if it gets too far away from the player
## 2. Fishing State
    One second after the bobber lands in a liquid, the player enters the Fishing State
    that limits his movement and flags him as fishing
### In the fishing state:
- The player has limited movement (slow, can't jump or crawl)
- The player can despawn the bobber by holding left-click, leaving the fishing state
- A random amount of time passes before the fish bites
## 3. The Bite
    A few seconds after the player enters the fishing state and doesn't change states,
    an exclamation mark apperas over the bobber that signals a bite, the player can
    hold left-click withing a short time window to start the fishing minigame
### Considerations:
 - The time frame for the bite is always the same
## 4. Minigame Start
    When the player pulls the bobber withing the Bite time frame, the fishing minigame
    Starts. The system gathers the context information and gets a pool of possible fish
    Types that could exists in that context, then selects one from that pool randomly
    based on every type's rarity, the type of fish dictates various things within the
    minigame
## 5. Tug-o-War
    The minigame tends to a more realistic approach in game fishing systems, the player
    is in a constant fight to bring the fish closer while the fish is trying to get away.
    The player has to manage the rod's tension so the wire doesn't snap, and the fish
    manages it's stamina by resting and pulling a little lighter.
## 6. The Catch
    If the player manages to bring the fish close enough, the minigame stops and registers
    A catch, giving the player the fish item and some XP

# Minigame Mechanics

## Fish Movements
    Every fish has different stats, these stats being Strength, Max Stamina, Stamina Regen,
    Speed and behaviour, these stats change how the fish behaves in the minigame

 - The fish generally moves away from the player, but can also change its direction
 - 