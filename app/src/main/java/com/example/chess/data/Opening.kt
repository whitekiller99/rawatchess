package com.example.chess.data

import java.util.UUID

data class Opening(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val lines: MutableList<OpeningLine> = mutableListOf()
)

data class OpeningLine(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    val moves: MutableList<String> = mutableListOf(),

    /*
     * Har move ke baad annotations.
     *
     * marks[0] = first move ke baad ke marks
     * marks[1] = second move ke baad ke marks
     * marks[2] = third move ke baad ke marks
     */
    var marks: MutableList<MutableList<BoardMark>> =
        mutableListOf()
)

data class BoardMark(
    val type: String,          // CIRCLE / ARROW
    val color: String,         // GREEN / RED
    val from: String,          // e4
    val to: String? = null     // arrow ke liye e5
)