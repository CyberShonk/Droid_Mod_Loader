package com.shonkware.droidmodloader.engine.rules

import com.shonkware.droidmodloader.engine.model.DeployScope

class DeployFileClassifier {

    fun classify(normalizedPath: String): DeployScope {
        val path = normalizedPath.lowercase()
        val fileName = path.substringAfterLast('/')

        return when {
            path.startsWith("fomod/") -> DeployScope.MANAGER_ONLY

            path == "plugins.txt" || path == "loadorder.txt" -> DeployScope.PROFILE_ONLY

            fileName == "readme.txt" ||
                    fileName == "readme.md" ||
                    fileName == "changelog.txt" ||
                    fileName == "license.txt" ||
                    fileName == "credits.txt" ||
                    fileName == "screenshot.jpg" ||
                    fileName == "screenshot.png" ||
                    fileName.endsWith(".pdf") -> DeployScope.MANAGER_ONLY

            isProbableGameRootFile(path) -> DeployScope.GAME_ROOT

            else -> DeployScope.DATA
        }
    }

    fun isDeployableToCurrentStaging(scope: DeployScope): Boolean {
        return scope == DeployScope.DATA
    }

    private fun isProbableGameRootFile(path: String): Boolean {
        if (path.contains("/")) return false

        return path.endsWith(".dll") ||
                path.endsWith(".exe") ||
                path.endsWith(".asi") ||
                path == "d3d11.dll" ||
                path == "dxgi.dll" ||
                path == "enblocal.ini" ||
                path == "enbseries.ini"
    }
}