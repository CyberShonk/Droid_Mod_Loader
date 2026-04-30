package com.shonkware.droidmodloader.engine.model

data class InstalledModRecord(
    val modId: String,
    val displayName: String,
    val installPath: String,
    val sourceType: String,
    val sourceArchiveName: String?,
    val installedAtEpochMillis: Long
)