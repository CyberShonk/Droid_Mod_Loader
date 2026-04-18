package com.shonkware.droidmodloader.engine.io

import com.shonkware.droidmodloader.engine.util.PathUtils
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class FileInfo(
    val sourceMod: String,
    val archiveFile: File?, // null if loose file
    val originalPath: String,
    val normalizedPath: String,
    val hash: String
)

class FileScanner {

    private val fileMap = ConcurrentHashMap<String, MutableList<FileInfo>>()

    fun scanDirectory(currentDir: File, modRoot: File, modName: String) {
        val entries = currentDir.listFiles() ?: return

        for (entry in entries) {
            if (entry.isDirectory) {
                scanDirectory(entry, modRoot, modName)
            } else if (entry.isFile) {
                val relativePath = entry.relativeTo(modRoot).path
                val normalizedPath = PathUtils.normalize(relativePath) ?: continue
                val hash = computeHash(entry)

                val info = FileInfo(
                    sourceMod = modName,
                    archiveFile = null,
                    originalPath = relativePath,
                    normalizedPath = normalizedPath,
                    hash = hash
                )

                addFileToMap(normalizedPath, info)
            }
        }
    }

    fun getFileMap(): Map<String, List<FileInfo>> = fileMap

    private fun addFileToMap(normalizedPath: String, info: FileInfo) {
        fileMap.compute(normalizedPath) { _, list ->
            val newList = list ?: mutableListOf()
            newList.add(info)
            newList
        }
    }

    private fun computeHash(file: File): String {
        val buffer = ByteArray(1024 * 8)
        val digest = MessageDigest.getInstance("SHA-256")

        FileInputStream(file).use { fis ->
            var read = fis.read(buffer)
            while (read != -1) {
                digest.update(buffer, 0, read)
                read = fis.read(buffer)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}