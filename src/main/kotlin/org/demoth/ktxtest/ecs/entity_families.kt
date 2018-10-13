package org.demoth.ktxtest.ecs

import ktx.ashley.allOf
import ktx.ashley.oneOf

val playerEntities = allOf(Physical::class, Player::class, HasHealth::class).get()
val playerHealthAndScore = allOf(Player::class, HasHealth::class).get()
val playerPosition = allOf(Physical::class, Player::class).get()

val drawables = oneOf(Textured::class, Animated::class).oneOf(Physical::class, Positioned::class).get()
val drawableNames = allOf(Named::class).oneOf(Physical::class, Positioned::class).get()
val floatingLabels = allOf(FloatingUpLabel::class, Positioned::class, Named::class).get()

val entityCleanup = oneOf(Physical::class, TTL::class).get()

val stationaryMonsters = allOf(MonsterStationaryRanged::class, Physical::class, HasHealth::class).get()

val allSounds = allOf(HasSound::class).get()