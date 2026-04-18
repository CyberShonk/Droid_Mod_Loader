package com.shonkware.droidmodloader.engine.model

data class ModFile(
    val modId: String,
    val sourceModName: String,
    val originalPath: String,
    val normalizedPath: String,
    val hash: String
)