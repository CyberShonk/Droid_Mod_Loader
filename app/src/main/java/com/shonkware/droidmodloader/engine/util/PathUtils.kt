package com.shonkware.droidmodloader.engine.util

object PathUtils {

    fun normalize(path: String): String? {
        var result = path.replace("\\", "/").lowercase()

        // Remove leading ./ first
        if (result.startsWith("./")) {
            result = result.removePrefix("./")
        }

        // Remove leading slashes
        while (result.startsWith("/")) {
            result = result.removePrefix("/")
        }

        // Collapse duplicate slashes
        result = result.replace(Regex("/+"), "/")

        // Remove leading Data/
        if (result.startsWith("data/")) {
            result = result.removePrefix("data/")
        }

        // Remove trailing slash
        if (result.endsWith("/")) {
            result = result.removeSuffix("/")
        }

        // Ignore empty paths
        if (result.isEmpty()) {
            return null
        }

        // Ignore __MACOSX and hidden path segments
        val parts = result.split("/")
        for (part in parts) {
            if (part == "__macosx" || (part.startsWith(".") && part.isNotEmpty())) {
                return null
            }
        }

        return result
    }
}