package com.shonkware.droidmodloader.engine.flags

import com.shonkware.droidmodloader.engine.index.ModContentCategory
import com.shonkware.droidmodloader.engine.index.ModContentEntry
import com.shonkware.droidmodloader.engine.index.ModContentIndex
import com.shonkware.droidmodloader.engine.model.DeployScope
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModFlagEvaluatorTest {
    private val mod = Mod(
        id = "test-mod",
        name = "Test Mod",
        installPath = "/mods/test-mod",
        enabled = true,
        priority = 1,
        modType = ModType.LOOSE
    )

    @Test
    fun `missing install directory reports only missing path`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { false }
        ).evaluate(
            mod = mod,
            contentIndex = emptyIndex()
        )

        assertEquals(
            setOf(ModFlag.MISSING_INSTALL_PATH),
            flags
        )
    }

    @Test
    fun `existing directory with no recognized game data is flagged`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { true }
        ).evaluate(
            mod = mod,
            contentIndex = emptyIndex()
        )

        assertEquals(
            setOf(ModFlag.NO_VALID_GAME_DATA),
            flags
        )
    }

    @Test
    fun `deployable game content has no general flags`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { true }
        ).evaluate(
            mod = mod,
            contentIndex = indexWith(
                ModContentEntry(
                    originalPath = "textures/example.dds",
                    normalizedPath = "textures/example.dds",
                    category = ModContentCategory.GAME_FILE,
                    reason = "Game content",
                    isDeployable = true,
                    deployScope = DeployScope.DATA
                )
            )
        )

        assertTrue(flags.isEmpty())
    }

    @Test
    fun `plugin content has no general flags`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { true }
        ).evaluate(
            mod = mod,
            contentIndex = indexWith(
                ModContentEntry(
                    originalPath = "Example.esp",
                    normalizedPath = "example.esp",
                    category = ModContentCategory.PLUGIN,
                    reason = "Plugin",
                    isDeployable = true,
                    deployScope = DeployScope.DATA
                )
            )
        )

        assertTrue(flags.isEmpty())
    }

    @Test
    fun `unknown content is valid when physical deployment scope is data`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { true }
        ).evaluate(
            mod = mod,
            contentIndex = indexWith(
                ModContentEntry(
                    originalPath = "uio/supported.txt",
                    normalizedPath = "uio/supported.txt",
                    category = ModContentCategory.UNKNOWN,
                    reason = "Unknown file type/location",
                    isDeployable = true,
                    deployScope = DeployScope.DATA
                )
            )
        )

        assertTrue(flags.isEmpty())
    }

    @Test
    fun `unknown content remains no valid game data`() {
        val flags = ModFlagEvaluator(
            installDirectoryExists = { true }
        ).evaluate(
            mod = mod,
            contentIndex = indexWith(
                ModContentEntry(
                    originalPath = "custom/thing.bin",
                    normalizedPath = "custom/thing.bin",
                    category = ModContentCategory.UNKNOWN,
                    reason = "Unknown",
                    isDeployable = false,
                    deployScope = DeployScope.MANAGER_ONLY
                )
            )
        )

        assertEquals(
            setOf(ModFlag.NO_VALID_GAME_DATA),
            flags
        )
    }

    private fun emptyIndex(): ModContentIndex =
        ModContentIndex(
            modId = mod.id,
            modName = mod.name,
            entries = emptyList()
        )

    private fun indexWith(
        vararg entries: ModContentEntry
    ): ModContentIndex =
        ModContentIndex(
            modId = mod.id,
            modName = mod.name,
            entries = entries.toList()
        )
}
