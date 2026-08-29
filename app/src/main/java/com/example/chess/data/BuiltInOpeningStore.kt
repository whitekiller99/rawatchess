package com.example.chess.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object BuiltInOpeningStore {
    private val gson = Gson()
    private const val PREF = "built_in_openings"
    private const val KEY_IMPORTED = "imported"

    fun importIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_IMPORTED, false)) return

        val existing = OpeningStore.getAll(context)
        val existingNames = existing.map { it.name.lowercase() }.toMutableSet()

        try {
            val files = context.assets.list("openings") ?: emptyArray()
            for (file in files.filter { it.endsWith(".json", ignoreCase = true) }) {
                val json = context.assets.open("openings/$file").bufferedReader().use { it.readText() }
                val type = object : TypeToken<Opening>() {}.type
                val opening = gson.fromJson<Opening>(json, type) ?: continue
                if (opening.name.isNotBlank() && opening.name.lowercase() !in existingNames) {
                    OpeningStore.add(context, opening)
                    existingNames.add(opening.name.lowercase())
                }
            }
        } finally {
            prefs.edit().putBoolean(KEY_IMPORTED, true).apply()
        }
    }
}
