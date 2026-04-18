package com.shonkware.droidmodloader.engine.install

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class ModExtractor(
    private val tempDir: File,
    private val modsDir: File
) {

    fun extractArchive(archive: File): File {
        val extractFolder = File(tempDir, System.currentTimeMillis().toString())
        extractFolder.mkdirs()

        extractZip(archive, extractFolder)

        val normalizedRoot = normalizeExtractedStructure(extractFolder)

        val modName = archive.nameWithoutExtension
        val finalDir = File(modsDir, modName)

        if (finalDir.exists()) {
            finalDir.deleteRecursively()
        }

        normalizedRoot.copyRecursively(finalDir, overwrite = true)

        extractFolder.deleteRecursively()

        return finalDir
    }

    private fun extractZip(zipFile: File, outputDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry

            while (entry != null) {
                val newFile = File(outputDir, entry.name)

                val outputCanonicalPath = outputDir.canonicalPath
                val newFileCanonicalPath = newFile.canonicalPath

                if (!newFileCanonicalPath.startsWith(outputCanonicalPath + File.separator)
                    && newFileCanonicalPath != outputCanonicalPath
                ) {
                    throw SecurityException("Unsafe ZIP entry: ${entry.name}")
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()

                    FileOutputStream(newFile).use { fos ->
                        val buffer = ByteArray(8192)
                        var len = zis.read(buffer)

                        while (len > 0) {
                            fos.write(buffer, 0, len)
                            len = zis.read(buffer)
                        }
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun normalizeExtractedStructure(root: File): File {
        val children = root.listFiles() ?: return root

        if (children.size == 1 && children[0].isDirectory) {
            val child = children[0]

            val childDataFolder = child.listFiles()?.firstOrNull {
                it.isDirectory && it.name.equals("Data", ignoreCase = true)
            }

            if (childDataFolder != null) {
                return childDataFolder
            }

            return child
        }

        val rootDataFolder = children.firstOrNull {
            it.isDirectory && it.name.equals("Data", ignoreCase = true)
        }

        if (rootDataFolder != null) {
            return rootDataFolder
        }

        return root
    }
}