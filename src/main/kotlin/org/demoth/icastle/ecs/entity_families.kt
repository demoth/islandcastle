package org.demoth.icastle.ecs

import ktx.ashley.allOf
import ktx.ashley.oneOf

val playerEntities = allOf(Player::class, Physical::class, CharacterAnimation::class, Movement::class).get()
val playerHealthAndScore = allOf(Player::class, HasHealth::class).get()
val playerPosition = allOf(Physical::class, Player::class).get()

val drawables = oneOf(Textured::class, SimpleAnimation::class).oneOf(Physical::class, Positioned::class).get()
val drawableNames = allOf(Named::class).oneOf(Physical::class, Positioned::class).get()
val floatingLabels = allOf(FloatingUpLabel::class, Positioned::class, Named::class).get()

val entityCleanup = oneOf(Physical::class, TTL::class).get()

val monstersFiring = allOf(MonsterFiring::class, Physical::class).get()
val monstersMortal = allOf(Physical::class, HasHealth::class).get()
val monstersWalking = allOf(MonsterWalking::class, Physical::class).get()

val movables = allOf(Movement::class).oneOf(Physical::class, Positioned::class).get()

val allSounds = allOf(HasSound::class).get()