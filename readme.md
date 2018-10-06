Island castle
=============

This is a 2d top down single player action rpg. Work in progress.

Written in kotlin and based on libraries: LibGDX, LibKTX, Box2d

Currently implemented:

 * Tmx map loading
 * Player WASD controls
 * Box2D integration
 * Using fireball missiles
 * Stationary enemies shooting missiles

Required Java (openjdk is fine)

to build distribution:
./gradlew distZip

to run, either run from gradle:
./gradlew run
or unpack distribution and execute main script from bin folder