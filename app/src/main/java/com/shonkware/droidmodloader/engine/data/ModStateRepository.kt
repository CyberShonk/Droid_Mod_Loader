package com.shonkware.droidmodloader.engine.data

import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ModStateRepository(
    private val stateFile: File
) {

    fun saveMods(mods: List<Mod>) {
        val jsonArray = JSONArray()

        for (mod in mods) {
            val jsonObject = JSONObject()
            jsonObject.put("id", mod.id)
            jsonObject.put("name", mod.name)
            jsonObject.put("installPath", mod.installPath)
            jsonObject.put("enabled", mod.enabled)
            jsonObject.put("priority", mod.priority)
            jsonObject.put("modType", mod.modType.name)

            jsonArray.put(jsonObject)
        }

        stateFile.parentFile?.mkdirs()
        stateFile.writeText(jsonArray.toString(2))
    }

    fun loadMods(): List<Mod> {
        if (!stateFile.exists()) {
            return emptyList()
        }

        val text = stateFile.readText()
        if (text.isBlank()) {
            return emptyList()
        }

        val jsonArray = JSONArray(text)
        val mods = mutableListOf<Mod>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val modTypeName = jsonObject.optString("modType", "ARCHIVE")
            val modType = try {
                ModType.valueOf(modTypeName)
            } catch (e: Exception) {
                ModType.ARCHIVE
            }

            mods.add(
                Mod(
                    id = jsonObject.optString("id", ""),
                    name = jsonObject.optString("name", ""),
                    installPath = jsonObject.optString("installPath", ""),
                    enabled = jsonObject.optBoolean("enabled", true),
                    priority = jsonObject.optInt("priority", 0),
                    modType = modType
                )
            )
        }

        return mods
    }
}