package com.shonkware.droidmodloader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shonkware.droidmodloader.ui.theme.DmlButtons
import com.shonkware.droidmodloader.ui.theme.DmlColors
import com.shonkware.droidmodloader.ui.theme.DmlDefaults

@Composable
fun RecoveryToolsCard(
    operationInProgress: Boolean,
    deployRecoveryAttentionRequired: Boolean,
    onViewLastDeployJournal: () -> Unit,
    onMarkDeployRecoveryReviewed: () -> Unit,
    onRequestForceFullRedeploy: () -> Unit,
    onBuildFullRedeployPlan: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = DmlDefaults.panelCardColors(),
        border = BorderStroke(1.dp, DmlColors.BorderDim)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Recovery Tools",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tools for reviewing deploy state after a failed or interrupted deploy. Only journal review is active right now.",
                style = MaterialTheme.typography.bodySmall
            )

            DmlButtons.Secondary(
                text = "View Last Deploy Journal",
                enabled = !operationInProgress,
                onClick = onViewLastDeployJournal,
                modifier = Modifier.fillMaxWidth()
            )

            DmlButtons.Secondary(
                text = "Build Full Redeploy Plan",
                enabled = !operationInProgress,
                onClick = onBuildFullRedeployPlan,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Shows what DML would rewrite if you rebuilt the deployed game folder from the current mod list. This does not change files.",
                style = MaterialTheme.typography.bodySmall
            )

            DmlButtons.Secondary(
                text = "Mark Warning Reviewed",
                enabled = !operationInProgress && deployRecoveryAttentionRequired,
                onClick = onMarkDeployRecoveryReviewed,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Coming later:",
                fontWeight = FontWeight.Bold
            )

            DmlButtons.Secondary(
                text = "Resume Interrupted Deploy",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            DmlButtons.Secondary(
                text = "Rollback Last Deploy",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            DmlButtons.Danger(
                text = "Force Full Redeploy",
                enabled = !operationInProgress,
                onClick = onRequestForceFullRedeploy,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Rewrites all current managed files from the active mod list. Use this after an interrupted deploy or if the target folder looks out of sync.",
                style = MaterialTheme.typography.bodySmall
            )

            DmlButtons.Secondary(
                text = "Rebuild Deploy Manifest",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            DmlButtons.Secondary(
                text = "Rebuild Data Baseline",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Disabled tools are planned recovery actions. They are shown here early so testers know where this system is going.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}