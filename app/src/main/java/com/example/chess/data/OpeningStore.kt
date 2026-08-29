package com.example.chess.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object OpeningStore {

    private const val PREF = "chess_openings"
    private const val KEY = "openings_json"

    private val gson = Gson()

    // ============================================================
    // GET ALL OPENINGS
    // ============================================================

    fun getAll(
        context: Context
    ): MutableList<Opening> {

        val prefs =
            context.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
            )

        val json =
            prefs.getString(
                KEY,
                null
            )

        if (json.isNullOrBlank()) {
            return mutableListOf()
        }

        return try {

            val type =
                object :
                    TypeToken<MutableList<Opening>>() {}.type

            val list =
                gson.fromJson<MutableList<Opening>>(
                    json,
                    type
                ) ?: mutableListOf()

            // ----------------------------------------------------
            // Old data compatibility
            // ----------------------------------------------------

            list.forEach { opening ->

                if (opening.lines == null) {
                    return@forEach
                }

                opening.lines.forEach { line ->

                    /*
                     * Gson purane data me marks ko null
                     * de sakta hai.
                     */
                    if (line.marks == null) {
                        line.marks =
                            mutableListOf()
                    }

                    /*
                     * Har move ke liye ek marks list.
                     */
                    while (
                        line.marks.size <
                        line.moves.size
                    ) {

                        line.marks.add(
                            mutableListOf()
                        )
                    }

                    /*
                     * Agar extra marks hain to remove karo.
                     */
                    while (
                        line.marks.size >
                        line.moves.size
                    ) {

                        line.marks.removeAt(
                            line.marks.lastIndex
                        )
                    }
                }
            }

            list

        } catch (
            e: Exception
        ) {

            mutableListOf()
        }
    }

    // ============================================================
    // SAVE ALL
    // ============================================================

    fun saveAll(
        context: Context,
        list: List<Opening>
    ) {

        val prefs =
            context.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .putString(
                KEY,
                gson.toJson(list)
            )
            .apply()
    }

    // ============================================================
    // ADD OPENING
    // ============================================================

    fun add(
        context: Context,
        opening: Opening
    ) {

        val list =
            getAll(context)

        list.add(opening)

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // RENAME OPENING
    // ============================================================

    fun renameOpening(
        context: Context,
        openingId: String,
        newName: String
    ) {

        val cleanName =
            newName.trim()

        if (cleanName.isBlank()) {
            return
        }

        val list =
            getAll(context)

        val opening =
            list.find {
                it.id == openingId
            } ?: return

        opening.name =
            cleanName

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // DELETE OPENING
    // ============================================================

    fun delete(
        context: Context,
        id: String
    ) {

        val list =
            getAll(context)

        list.removeAll {
            it.id == id
        }

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // ADD LINE
    // ============================================================

    fun addLine(
        context: Context,
        openingId: String,
        moves: List<String>,
        marks: List<List<BoardMark>> = emptyList()
    ) {

        val list =
            getAll(context)

        val opening =
            list.find {
                it.id == openingId
            } ?: return

        // --------------------------------------------------------
        // New line number
        // --------------------------------------------------------

        val number =
            opening.lines.size + 1

        // --------------------------------------------------------
        // Create marks list
        // --------------------------------------------------------

        val lineMarks =
            MutableList(
                moves.size
            ) { index ->

                marks
                    .getOrNull(index)
                    ?.toMutableList()
                    ?: mutableListOf()
            }

        // --------------------------------------------------------
        // Create line
        // --------------------------------------------------------

        val newLine =
            OpeningLine(
                name =
                    "%02d".format(number),

                moves =
                    moves.toMutableList(),

                marks =
                    lineMarks
            )

        opening.lines.add(
            newLine
        )

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // RENAME LINE
    // ============================================================

    fun renameLine(
        context: Context,
        openingId: String,
        lineId: String,
        newName: String
    ) {

        val cleanName =
            newName.trim()

        if (cleanName.isBlank()) {
            return
        }

        val list =
            getAll(context)

        val opening =
            list.find {
                it.id == openingId
            } ?: return

        val line =
            opening.lines.find {
                it.id == lineId
            } ?: return

        line.name =
            cleanName

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // DELETE LINE
    // ============================================================

    fun deleteLine(
        context: Context,
        openingId: String,
        lineId: String
    ) {

        val list =
            getAll(context)

        val opening =
            list.find {
                it.id == openingId
            } ?: return

        opening.lines.removeAll {
            it.id == lineId
        }

        // --------------------------------------------------------
        // Re-number lines
        // --------------------------------------------------------

        opening.lines.forEachIndexed { index, line ->

            line.name =
                "%02d".format(
                    index + 1
                )
        }

        saveAll(
            context,
            list
        )
    }

    // ============================================================
    // UPDATE LINE
    // ============================================================

    fun updateLine(
        context: Context,
        openingId: String,
        lineId: String,
        moves: List<String>,
        marks: List<List<BoardMark>> = emptyList()
    ) {

        val list =
            getAll(context)

        val opening =
            list.find {
                it.id == openingId
            } ?: return

        val line =
            opening.lines.find {
                it.id == lineId
            } ?: return

        // --------------------------------------------------------
        // Update moves
        // --------------------------------------------------------

        line.moves.clear()

        line.moves.addAll(
            moves
        )

        // --------------------------------------------------------
        // Update marks
        // --------------------------------------------------------

        line.marks =
            MutableList(
                moves.size
            ) { index ->

                marks
                    .getOrNull(index)
                    ?.toMutableList()
                    ?: mutableListOf()
            }

        saveAll(
            context,
            list
        )
    }
}