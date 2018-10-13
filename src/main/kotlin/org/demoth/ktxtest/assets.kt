package org.demoth.ktxtest


enum class Sounds(val filename: String) {
    FIREBALL("MAGIC_SPELL_Flame_03_mono.wav"),
    HURT1("GRUNT_Male_Hurt_01_mono.wav"),
    HURT2("GRUNT_Male_Hurt_02_mono.wav"),
    HURT3("GRUNT_Male_Hurt_03_mono.wav"),
    HURT4("GRUNT_Male_Hurt_04_mono.wav"),
    PLAYER_DIE("SCREAM_Male_B_06_mono.wav"),
    VICTORY("EXCLAMATION_Male_B_Woooho_mono.wav"),
    GAME_OVER("VOICE_ROBOTIC_MALE_Game_Over_1_Aggressive_stereo.wav"),
    MONSTER_DIE("VOICE_Male_41yo_Scream_01_Shot_mono.wav"),
    EXPLOSION("EXPLOSION_Short_Smooth_Crackle_stereo.wav")
}

val HURT = listOf(Sounds.HURT1, Sounds.HURT2, Sounds.HURT3, Sounds.HURT4)

enum class Sprites(val filename: String) {
    FIREBALL("Sprite_FX_Explosion_0041_ball.png"),
    EYE_BOT("eye_monsters/eyebot.png"),
    EYE_LANDER("eye_monsters/eyelander.png"),
    KNIGHT("knight32.png")
}

enum class SpriteSheets(val filename: String, val rows: Int, val cols: Int) {
    FIRE_SPIRALS("Sprite_FX_Fire_0004_FIX.png", 1, 4),
    FIRE_EXPLOSION("Sprite_FX_Explosion_0041_FIX.png", 1, 5)
}