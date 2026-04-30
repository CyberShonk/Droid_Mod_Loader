package com.shonkware.droidmodloader.engine.model

data class DeploymentRecord(
    val normalizedPath: String,
    val winningModId: String,
    val sourceFilePath: String,
    val hash: String,
    val deployedRelativePath: String
)