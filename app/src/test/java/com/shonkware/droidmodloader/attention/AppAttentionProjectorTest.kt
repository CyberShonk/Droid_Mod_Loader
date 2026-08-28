package com.shonkware.droidmodloader.attention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppAttentionProjectorTest {
    @Test
    fun `inactive profile session has no attention`() {
        val result = project(
            profileSessionReady = false,
            activeProfileId = null,
            gameInstallationReselectionRequired = true,
            deployRecoveryRequired = true
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `game installation reselection produces one scoped attention item`() {
        val result = project(gameInstallationReselectionRequired = true)

        assertEquals(1, result.size)
        assertEquals(AppAttentionKind.GAME_INSTALLATION_RESELECTION, result.single().kind)
        assertEquals(
            "game_installation_reselection:profile-a:fallout_nv",
            result.single().id.value
        )
        assertEquals(
            listOf(AppAttentionAction.SELECT_GAME_FOLDER),
            result.single().actions
        )
    }

    @Test
    fun `deployment recovery and game reselection coexist in deterministic order`() {
        val first = project(
            gameInstallationReselectionRequired = true,
            deployRecoveryRequired = true
        )
        val second = project(
            gameInstallationReselectionRequired = true,
            deployRecoveryRequired = true
        )

        assertEquals(first, second)
        assertEquals(
            listOf(
                AppAttentionKind.DEPLOY_RECOVERY,
                AppAttentionKind.GAME_INSTALLATION_RESELECTION
            ),
            first.map { it.kind }
        )
        assertEquals(
            listOf(
                AppAttentionAction.VIEW_DEPLOY_RECOVERY,
                AppAttentionAction.MARK_DEPLOY_RECOVERY_REVIEWED
            ),
            first.first().actions
        )
    }

    @Test
    fun `attention identity is scoped to profile and game`() {
        val first = project(
            activeProfileId = "profile-a",
            selectedGameId = "fallout_nv",
            gameInstallationReselectionRequired = true
        ).single()
        val second = project(
            activeProfileId = "profile-b",
            selectedGameId = "fallout_nv",
            gameInstallationReselectionRequired = true
        ).single()
        val third = project(
            activeProfileId = "profile-a",
            selectedGameId = "skyrim_le",
            gameInstallationReselectionRequired = true
        ).single()

        assertTrue(first.id != second.id)
        assertTrue(first.id != third.id)
    }

    private fun project(
        profileSessionReady: Boolean = true,
        activeProfileId: String? = "profile-a",
        selectedGameId: String = "fallout_nv",
        gameInstallationReselectionRequired: Boolean = false,
        deployRecoveryRequired: Boolean = false
    ): List<AppAttention> {
        return AppAttentionProjector.project(
            AppAttentionProjectionInput(
                profileSessionReady = profileSessionReady,
                activeProfileId = activeProfileId,
                selectedGameId = selectedGameId,
                gameInstallationReselectionRequired = gameInstallationReselectionRequired,
                deployRecoveryRequired = deployRecoveryRequired
            )
        )
    }
}
