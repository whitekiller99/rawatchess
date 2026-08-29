package com.example.chess.engine

data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val piece: Piece,
    val captured: Piece? = null,
    val isCastle: Boolean = false,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false
) {
    // Board coordinate ko chess notation (jaise "e2e4" ya promotion ke sath "e7e8q") me convert karta hai
    fun toUci(): String {
        val fromFile = ('a' + fromCol)
        val fromRank = 8 - fromRow
        val toFile = ('a' + toCol)
        val toRank = 8 - toRow
        val promoSuffix = when (promotion) {
            PieceType.QUEEN -> "q"
            PieceType.ROOK -> "r"
            PieceType.BISHOP -> "b"
            PieceType.KNIGHT -> "n"
            else -> ""
        }
        return "$fromFile$fromRank$toFile$toRank$promoSuffix"
    }
}