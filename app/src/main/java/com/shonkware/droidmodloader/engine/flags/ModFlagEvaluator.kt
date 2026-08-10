package com.shonkware.droidmodloader.engine.flags

import com.shonkware.droidmodloader.engine.index.ModContentIndex
import com.shonkware.droidmodloader.engine.model.Mod
import java.io.File

class ModFlagEvaluator(
    private val installDirectoryExists: (String) -> Boolean = { path ->
        File(path).isDirectory
    }
) {
    fun evaluate(
        mod: Mod,
        contentIndex: ModContentIndex
    ): Set<ModFlag> {
        if (!installDirectoryExists(mod.installPath)) {
            return setOf(ModFlag.MISSING_INSTALL_PATH)
        }

        if (
            contentIndex.deployableFiles.isEmpty() &&
            contentIndex.plugins.isEmpty()
        ) {
            return setOf(ModFlag.NO_VALID_GAME_DATA)
        }

        return emptySet()
    }
}
