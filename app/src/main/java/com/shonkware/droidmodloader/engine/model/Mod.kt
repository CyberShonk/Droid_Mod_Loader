package com.shonkware.droidmodloader.engine.model

enum class ModType {
    LOOSE,
    ARCHIVE,
    MIXED
}

data class Mod(
    val id: String,
    val name: String,
    val installPath: String,
    val enabled: Boolean,
    val priority: Int,
    val modType: ModType
)