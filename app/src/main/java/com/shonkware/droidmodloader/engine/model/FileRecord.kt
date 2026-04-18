package com.shonkware.droidmodloader.engine.model

data class FileRecord(
    val normalizedPath: String,
    val winningModId: String,
    val winningModName: String,
    val sourceFilePath: String,
    val hash: String
)