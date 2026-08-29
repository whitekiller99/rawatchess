package com.example.chess.engine

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

data class Piece(val type: PieceType, val color: PieceColor)