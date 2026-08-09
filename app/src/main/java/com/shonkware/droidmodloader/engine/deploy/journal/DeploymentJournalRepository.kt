package com.shonkware.droidmodloader.engine.deploy.journal

import org.json.JSONObject
import java.io.File

class DeploymentJournalRepository(
    private val journalFile: File
) {
    fun saveStarted(record: DeploymentJournalRecord) {
        val existing = load()
        check(existing?.status != DeploymentJournalStatus.STARTED) {
            buildString {
                append("Refusing to overwrite unfinished deployment journal")
                existing?.let {
                    append(" ${it.operationId}")
                    it.dataTarget?.let { target ->
                        append(" for ${target.mode}:${target.target}")
                    }
                }
                append(". Review the previous deployment warning before starting another deploy.")
            }
        }
        save(record)
    }

    fun markCompleted(
        record: DeploymentJournalRecord,
        resultSummary: DeploymentJournalResultSummary
    ) {
        save(
            record.copy(
                status = DeploymentJournalStatus.COMPLETED,
                completedAtEpochMillis = System.currentTimeMillis(),
                resultSummary = resultSummary,
                failureMessage = null
            )
        )
    }

    fun markFailed(
        record: DeploymentJournalRecord,
        message: String
    ) {
        save(
            record.copy(
                status = DeploymentJournalStatus.FAILED,
                completedAtEpochMillis = System.currentTimeMillis(),
                failureMessage = message
            )
        )
    }

    fun load(): DeploymentJournalRecord? {
        if (!journalFile.exists()) return null

        val text = journalFile.readText()
        if (text.isBlank()) return null

        val json = JSONObject(text)
        val planJson = json.getJSONObject("planSummary")
        val resultJson = json.optJSONObject("resultSummary")

        return DeploymentJournalRecord(
            schemaVersion = json.optInt("schemaVersion", 1),
            operationId = json.optString("operationId"),
            gameId = json.optString("gameId"),
            profileId = json.optString("profileId"),
            status = DeploymentJournalStatus.valueOf(
                json.optString("status", DeploymentJournalStatus.FAILED.name)
            ),
            startedAtEpochMillis = json.optLong("startedAtEpochMillis"),
            completedAtEpochMillis = json.optNullableLong("completedAtEpochMillis"),
            planSummary = DeploymentJournalPlanSummary(
                dataOperationCount = planJson.optInt("dataOperationCount"),
                rootOperationCount = planJson.optInt("rootOperationCount"),
                totalOperationCount = planJson.optInt("totalOperationCount"),
                dataEstimatedCopyBytes = planJson.optNullableLong("dataEstimatedCopyBytes"),
                rootEstimatedCopyBytes = planJson.optNullableLong("rootEstimatedCopyBytes"),
                preflightCanDeploy = planJson.optBoolean("preflightCanDeploy"),
                preflightErrorCount = planJson.optInt("preflightErrorCount"),
                preflightWarningCount = planJson.optInt("preflightWarningCount")
            ),
            resultSummary = resultJson?.let {
                DeploymentJournalResultSummary(
                    addCount = it.optInt("addCount"),
                    updateCount = it.optInt("updateCount"),
                    removeCount = it.optInt("removeCount"),
                    backupCount = it.optInt("backupCount"),
                    restoreCount = it.optInt("restoreCount"),
                    protectedConflictCount = it.optInt("protectedConflictCount"),
                    finalRecordCount = it.optInt("finalRecordCount")
                )
            },
            failureMessage = json.optString("failureMessage")
                .takeIf { it.isNotBlank() && it != "null" },
            dataTarget = json.optJSONObject("dataTarget")?.toTargetState(),
            rootTarget = json.optJSONObject("rootTarget")?.toTargetState()
        )
    }

    private fun save(record: DeploymentJournalRecord) {
        journalFile.parentFile?.mkdirs()
        journalFile.writeText(toJson(record).toString(2))
    }

    private fun toJson(record: DeploymentJournalRecord): JSONObject {
        return JSONObject().apply {
            put("schemaVersion", record.schemaVersion)
            put("operationId", record.operationId)
            put("gameId", record.gameId)
            put("profileId", record.profileId)
            put("status", record.status.name)
            put("startedAtEpochMillis", record.startedAtEpochMillis)
            put("completedAtEpochMillis", record.completedAtEpochMillis ?: JSONObject.NULL)
            put("dataTarget", record.dataTarget?.toJson() ?: JSONObject.NULL)
            put("rootTarget", record.rootTarget?.toJson() ?: JSONObject.NULL)

            put(
                "planSummary",
                JSONObject().apply {
                    put("dataOperationCount", record.planSummary.dataOperationCount)
                    put("rootOperationCount", record.planSummary.rootOperationCount)
                    put("totalOperationCount", record.planSummary.totalOperationCount)
                    put("dataEstimatedCopyBytes", record.planSummary.dataEstimatedCopyBytes ?: JSONObject.NULL)
                    put("rootEstimatedCopyBytes", record.planSummary.rootEstimatedCopyBytes ?: JSONObject.NULL)
                    put("preflightCanDeploy", record.planSummary.preflightCanDeploy)
                    put("preflightErrorCount", record.planSummary.preflightErrorCount)
                    put("preflightWarningCount", record.planSummary.preflightWarningCount)
                }
            )

            val result = record.resultSummary
            put(
                "resultSummary",
                if (result == null) {
                    JSONObject.NULL
                } else {
                    JSONObject().apply {
                        put("addCount", result.addCount)
                        put("updateCount", result.updateCount)
                        put("removeCount", result.removeCount)
                        put("backupCount", result.backupCount)
                        put("restoreCount", result.restoreCount)
                        put("protectedConflictCount", result.protectedConflictCount)
                        put("finalRecordCount", result.finalRecordCount)
                    }
                }
            )

            put("failureMessage", record.failureMessage ?: JSONObject.NULL)
        }
    }

    private fun DeploymentJournalTargetState.toJson(): JSONObject {
        return JSONObject().apply {
            put("targetType", targetType)
            put("gameId", gameId)
            put("mode", mode)
            put("target", target)
            put("identityKey", identityKey)
            put("manifestFilePath", manifestFilePath)
            put("baselineFilePath", baselineFilePath ?: JSONObject.NULL)
            put("backupDirectoryPath", backupDirectoryPath)
        }
    }

    private fun JSONObject.toTargetState(): DeploymentJournalTargetState {
        return DeploymentJournalTargetState(
            targetType = optString("targetType"),
            gameId = optString("gameId"),
            mode = optString("mode"),
            target = optString("target"),
            identityKey = optString("identityKey"),
            manifestFilePath = optString("manifestFilePath"),
            baselineFilePath = optString("baselineFilePath")
                .takeIf { it.isNotBlank() && it != "null" },
            backupDirectoryPath = optString("backupDirectoryPath")
        )
    }

    private fun JSONObject.optNullableLong(name: String): Long? {
        if (!has(name) || isNull(name)) return null
        return optLong(name)
    }

    fun markReviewed(record: DeploymentJournalRecord) {
        save(
            record.copy(
                status = DeploymentJournalStatus.REVIEWED,
                completedAtEpochMillis = System.currentTimeMillis(),
                failureMessage = "User reviewed unfinished deploy warning. No recovery action was performed."
            )
        )
    }
}
