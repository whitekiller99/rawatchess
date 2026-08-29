package com.example.chess.engine

// ============================================================
// GAME STATUS
// ============================================================

enum class GameStatus {
    ONGOING,
    CHECK,
    CHECKMATE,
    STALEMATE
}


// ============================================================
// BOARD
// ============================================================

class Board {

    val squares: Array<Array<Piece?>> =
        Array(8) {
            arrayOfNulls<Piece>(8)
        }

    var turn: PieceColor =
        PieceColor.WHITE
        private set


    // ========================================================
    // CASTLING RIGHTS
    // ========================================================

    private var whiteKingMoved = false
    private var blackKingMoved = false

    private var whiteRookAMoved = false
    private var whiteRookHMoved = false

    private var blackRookAMoved = false
    private var blackRookHMoved = false


    // ========================================================
    // EN PASSANT
    // ========================================================

    private var enPassantTarget:
            Pair<Int, Int>? = null


    // ========================================================
    // INITIAL BOARD
    // ========================================================

    init {
        setupStandard()
    }


    // ========================================================
    // SETUP STANDARD POSITION
    // ========================================================

    fun setupStandard() {

        for (r in 0..7) {
            for (c in 0..7) {
                squares[r][c] = null
            }
        }


        val backRank =
            listOf(
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
            )


        for (c in 0..7) {

            squares[0][c] =
                Piece(
                    backRank[c],
                    PieceColor.BLACK
                )

            squares[1][c] =
                Piece(
                    PieceType.PAWN,
                    PieceColor.BLACK
                )

            squares[6][c] =
                Piece(
                    PieceType.PAWN,
                    PieceColor.WHITE
                )

            squares[7][c] =
                Piece(
                    backRank[c],
                    PieceColor.WHITE
                )
        }


        turn =
            PieceColor.WHITE


        whiteKingMoved = false
        blackKingMoved = false

        whiteRookAMoved = false
        whiteRookHMoved = false

        blackRookAMoved = false
        blackRookHMoved = false

        enPassantTarget = null
    }


    // ========================================================
    // PIECE AT
    // ========================================================

    fun pieceAt(
        r: Int,
        c: Int
    ): Piece? {

        return if (
            r in 0..7 &&
            c in 0..7
        ) {
            squares[r][c]
        } else {
            null
        }
    }


    // ========================================================
    // OPPOSITE COLOR
    // ========================================================

    private fun opposite(
        color: PieceColor
    ): PieceColor {

        return if (
            color == PieceColor.WHITE
        ) {
            PieceColor.BLACK
        } else {
            PieceColor.WHITE
        }
    }


    // ========================================================
    // PSEUDO LEGAL MOVES
    // ========================================================

    private fun pseudoLegalMoves(
        r: Int,
        c: Int,
        includeCastle: Boolean = true
    ): List<Pair<Int, Int>> {

        val piece =
            squares[r][c]
                ?: return emptyList()


        val moves =
            mutableListOf<Pair<Int, Int>>()


        when (piece.type) {


            // ==================================================
            // PAWN
            // ==================================================

            PieceType.PAWN -> {

                val dir =
                    if (
                        piece.color ==
                        PieceColor.WHITE
                    ) {
                        -1
                    } else {
                        1
                    }


                val startRow =
                    if (
                        piece.color ==
                        PieceColor.WHITE
                    ) {
                        6
                    } else {
                        1
                    }


                if (
                    pieceAt(
                        r + dir,
                        c
                    ) == null
                ) {

                    moves.add(
                        r + dir to c
                    )


                    if (
                        r == startRow &&
                        pieceAt(
                            r + 2 * dir,
                            c
                        ) == null
                    ) {

                        moves.add(
                            r + 2 * dir to c
                        )
                    }
                }


                for (
                dc in intArrayOf(-1, 1)
                ) {

                    val nr =
                        r + dir

                    val nc =
                        c + dc


                    val target =
                        pieceAt(
                            nr,
                            nc
                        )


                    if (
                        target != null &&
                        target.color != piece.color
                    ) {

                        moves.add(
                            nr to nc
                        )

                    } else if (
                        target == null &&
                        enPassantTarget ==
                        (nr to nc)
                    ) {

                        moves.add(
                            nr to nc
                        )
                    }
                }
            }


            // ==================================================
            // KNIGHT
            // ==================================================

            PieceType.KNIGHT -> {

                val deltas =
                    arrayOf(
                        -2 to -1,
                        -2 to 1,
                        -1 to -2,
                        -1 to 2,
                        1 to -2,
                        1 to 2,
                        2 to -1,
                        2 to 1
                    )


                for (
                (dr, dc) in deltas
                ) {

                    val nr =
                        r + dr

                    val nc =
                        c + dc


                    if (
                        nr in 0..7 &&
                        nc in 0..7
                    ) {

                        val target =
                            pieceAt(
                                nr,
                                nc
                            )


                        if (
                            target == null ||
                            target.color != piece.color
                        ) {

                            moves.add(
                                nr to nc
                            )
                        }
                    }
                }
            }


            // ==================================================
            // BISHOP
            // ==================================================

            PieceType.BISHOP -> {

                slide(
                    r,
                    c,
                    piece,
                    moves,
                    arrayOf(
                        -1 to -1,
                        -1 to 1,
                        1 to -1,
                        1 to 1
                    )
                )
            }


            // ==================================================
            // ROOK
            // ==================================================

            PieceType.ROOK -> {

                slide(
                    r,
                    c,
                    piece,
                    moves,
                    arrayOf(
                        -1 to 0,
                        1 to 0,
                        0 to -1,
                        0 to 1
                    )
                )
            }


            // ==================================================
            // QUEEN
            // ==================================================

            PieceType.QUEEN -> {

                slide(
                    r,
                    c,
                    piece,
                    moves,
                    arrayOf(
                        -1 to -1,
                        -1 to 1,
                        1 to -1,
                        1 to 1,
                        -1 to 0,
                        1 to 0,
                        0 to -1,
                        0 to 1
                    )
                )
            }


            // ==================================================
            // KING
            // ==================================================

            PieceType.KING -> {

                for (
                dr in -1..1
                ) {

                    for (
                    dc in -1..1
                    ) {

                        if (
                            dr == 0 &&
                            dc == 0
                        ) {
                            continue
                        }


                        val nr =
                            r + dr

                        val nc =
                            c + dc


                        if (
                            nr in 0..7 &&
                            nc in 0..7
                        ) {

                            val target =
                                pieceAt(
                                    nr,
                                    nc
                                )


                            if (
                                target == null ||
                                target.color != piece.color
                            ) {

                                moves.add(
                                    nr to nc
                                )
                            }
                        }
                    }
                }


                // ==================================================
                // CASTLING
                // ==================================================

                if (includeCastle) {

                    // WHITE
                    if (
                        piece.color ==
                        PieceColor.WHITE &&
                        r == 7 &&
                        c == 4 &&
                        !whiteKingMoved
                    ) {

                        // KING SIDE
                        if (
                            !whiteRookHMoved &&
                            pieceAt(7, 5) == null &&
                            pieceAt(7, 6) == null &&
                            squares[7][7]?.type ==
                            PieceType.ROOK &&
                            squares[7][7]?.color ==
                            PieceColor.WHITE
                        ) {

                            moves.add(
                                7 to 6
                            )
                        }


                        // QUEEN SIDE
                        if (
                            !whiteRookAMoved &&
                            pieceAt(7, 1) == null &&
                            pieceAt(7, 2) == null &&
                            pieceAt(7, 3) == null &&
                            squares[7][0]?.type ==
                            PieceType.ROOK &&
                            squares[7][0]?.color ==
                            PieceColor.WHITE
                        ) {

                            moves.add(
                                7 to 2
                            )
                        }
                    }


                    // BLACK
                    if (
                        piece.color ==
                        PieceColor.BLACK &&
                        r == 0 &&
                        c == 4 &&
                        !blackKingMoved
                    ) {

                        // KING SIDE
                        if (
                            !blackRookHMoved &&
                            pieceAt(0, 5) == null &&
                            pieceAt(0, 6) == null &&
                            squares[0][7]?.type ==
                            PieceType.ROOK &&
                            squares[0][7]?.color ==
                            PieceColor.BLACK
                        ) {

                            moves.add(
                                0 to 6
                            )
                        }


                        // QUEEN SIDE
                        if (
                            !blackRookAMoved &&
                            pieceAt(0, 1) == null &&
                            pieceAt(0, 2) == null &&
                            pieceAt(0, 3) == null &&
                            squares[0][0]?.type ==
                            PieceType.ROOK &&
                            squares[0][0]?.color ==
                            PieceColor.BLACK
                        ) {

                            moves.add(
                                0 to 2
                            )
                        }
                    }
                }
            }
        }


        return moves
    }


    // ========================================================
    // SLIDING PIECES
    // ========================================================

    private fun slide(
        r: Int,
        c: Int,
        piece: Piece,
        moves: MutableList<Pair<Int, Int>>,
        dirs: Array<Pair<Int, Int>>
    ) {

        for (
        (dr, dc) in dirs
        ) {

            var nr =
                r + dr

            var nc =
                c + dc


            while (
                nr in 0..7 &&
                nc in 0..7
            ) {

                val target =
                    pieceAt(
                        nr,
                        nc
                    )


                if (target == null) {

                    moves.add(
                        nr to nc
                    )

                } else {

                    if (
                        target.color !=
                        piece.color
                    ) {

                        moves.add(
                            nr to nc
                        )
                    }

                    break
                }


                nr += dr
                nc += dc
            }
        }
    }


    // ========================================================
    // ATTACK DETECTION
    // ========================================================

    fun isSquareAttacked(
        r: Int,
        c: Int,
        byColor: PieceColor
    ): Boolean {

        for (rr in 0..7) {

            for (cc in 0..7) {

                val piece =
                    squares[rr][cc]
                        ?: continue


                if (
                    piece.color != byColor
                ) {
                    continue
                }


                if (
                    piece.type ==
                    PieceType.PAWN
                ) {

                    val dir =
                        if (
                            byColor ==
                            PieceColor.WHITE
                        ) {
                            -1
                        } else {
                            1
                        }


                    if (
                        rr + dir == r &&
                        (
                                cc - 1 == c ||
                                        cc + 1 == c
                                )
                    ) {

                        return true
                    }

                } else {

                    val moves =
                        pseudoLegalMoves(
                            rr,
                            cc,
                            false
                        )


                    if (
                        moves.any {
                            it.first == r &&
                                    it.second == c
                        }
                    ) {

                        return true
                    }
                }
            }
        }


        return false
    }


    // ========================================================
    // KING POSITION
    // ========================================================

    fun kingPosition(
        color: PieceColor
    ): Pair<Int, Int>? {

        for (r in 0..7) {

            for (c in 0..7) {

                val piece =
                    squares[r][c]


                if (
                    piece != null &&
                    piece.type ==
                    PieceType.KING &&
                    piece.color == color
                ) {

                    return r to c
                }
            }
        }


        return null
    }


    // ========================================================
    // CHECK
    // ========================================================

    fun isInCheck(
        color: PieceColor
    ): Boolean {

        val king =
            kingPosition(color)
                ?: return false


        return isSquareAttacked(
            king.first,
            king.second,
            opposite(color)
        )
    }


    // ========================================================
    // LEAVES KING SAFE
    // ========================================================

    private fun leavesKingSafe(
        fr: Int,
        fc: Int,
        tr: Int,
        tc: Int,
        piece: Piece
    ): Boolean {

        val backupFrom =
            squares[fr][fc]

        val backupTo =
            squares[tr][tc]


        var epRow = -1
        var epCol = -1

        var epPiece: Piece? = null


        val isEnPassantMove =
            piece.type ==
                    PieceType.PAWN &&
                    fc != tc &&
                    squares[tr][tc] == null


        if (isEnPassantMove) {

            epRow = fr
            epCol = tc

            epPiece =
                squares[epRow][epCol]

            squares[epRow][epCol] =
                null
        }


        squares[tr][tc] =
            piece

        squares[fr][fc] =
            null


        val safe =
            !isInCheck(
                piece.color
            )


        // UNDO
        squares[fr][fc] =
            backupFrom

        squares[tr][tc] =
            backupTo


        if (isEnPassantMove) {

            squares[epRow][epCol] =
                epPiece
        }


        return safe
    }


    // ========================================================
    // LEGAL MOVES
    // ========================================================

    fun legalMoves(
        r: Int,
        c: Int
    ): List<Pair<Int, Int>> {

        val piece =
            squares[r][c]
                ?: return emptyList()


        if (
            piece.color != turn
        ) {
            return emptyList()
        }


        val pseudo =
            pseudoLegalMoves(
                r,
                c,
                true
            )


        val result =
            mutableListOf<Pair<Int, Int>>()


        val enemy =
            opposite(piece.color)


        for (
        (tr, tc) in pseudo
        ) {

            // CASTLING CHECK
            if (
                piece.type ==
                PieceType.KING &&
                kotlin.math.abs(tc - c) == 2
            ) {

                if (
                    isInCheck(
                        piece.color
                    )
                ) {
                    continue
                }


                val step =
                    if (tc > c) {
                        1
                    } else {
                        -1
                    }


                if (
                    isSquareAttacked(
                        r,
                        c + step,
                        enemy
                    )
                ) {
                    continue
                }


                if (
                    isSquareAttacked(
                        r,
                        tc,
                        enemy
                    )
                ) {
                    continue
                }
            }


            if (
                leavesKingSafe(
                    r,
                    c,
                    tr,
                    tc,
                    piece
                )
            ) {

                result.add(
                    tr to tc
                )
            }
        }


        return result
    }


    // ========================================================
    // ANY LEGAL MOVE
    // ========================================================

    private fun hasAnyLegalMove(
        color: PieceColor
    ): Boolean {

        for (r in 0..7) {

            for (c in 0..7) {

                val piece =
                    squares[r][c]
                        ?: continue


                if (
                    piece.color == color &&
                    legalMoves(
                        r,
                        c
                    ).isNotEmpty()
                ) {

                    return true
                }
            }
        }


        return false
    }


    // ========================================================
    // GAME STATUS
    // ========================================================

    fun gameStatus(): GameStatus {

        val inCheck =
            isInCheck(turn)

        val hasMove =
            hasAnyLegalMove(turn)


        return when {

            inCheck &&
                    !hasMove ->
                GameStatus.CHECKMATE

            !inCheck &&
                    !hasMove ->
                GameStatus.STALEMATE

            inCheck ->
                GameStatus.CHECK

            else ->
                GameStatus.ONGOING
        }
    }


    // ========================================================
    // PROMOTION
    // ========================================================

    fun isPromotionMove(
        fr: Int,
        fc: Int,
        tr: Int,
        tc: Int
    ): Boolean {

        val piece =
            squares[fr][fc]
                ?: return false


        return (
                piece.type ==
                        PieceType.PAWN &&
                        (
                                tr == 0 ||
                                        tr == 7
                                )
                )
    }


    // ========================================================
    // MOVE PIECE
    // ========================================================

    fun movePiece(
        fr: Int,
        fc: Int,
        tr: Int,
        tc: Int,
        promotionChoice: PieceType? = null
    ): Move? {

        val piece =
            squares[fr][fc]
                ?: return null


        var captured =
            squares[tr][tc]


        var isCastle =
            false

        var isEnPassant =
            false


        // ====================================================
        // EN PASSANT
        // ====================================================

        if (
            piece.type ==
            PieceType.PAWN &&
            fc != tc &&
            squares[tr][tc] == null
        ) {

            isEnPassant = true

            captured =
                squares[fr][tc]

            squares[fr][tc] =
                null
        }


        // ====================================================
        // CASTLING
        // ====================================================

        if (
            piece.type ==
            PieceType.KING &&
            kotlin.math.abs(
                tc - fc
            ) == 2
        ) {

            isCastle = true


            if (tc == 6) {

                squares[fr][5] =
                    squares[fr][7]

                squares[fr][7] =
                    null

            } else if (tc == 2) {

                squares[fr][3] =
                    squares[fr][0]

                squares[fr][0] =
                    null
            }
        }


        // ====================================================
        // MOVE
        // ====================================================

        squares[tr][tc] =
            piece

        squares[fr][fc] =
            null


        // ====================================================
        // PROMOTION
        // ====================================================

        var promotion:
                PieceType? = null


        if (
            piece.type ==
            PieceType.PAWN &&
            (
                    tr == 0 ||
                            tr == 7
                    )
        ) {

            promotion =
                promotionChoice
                    ?: PieceType.QUEEN


            squares[tr][tc] =
                Piece(
                    promotion,
                    piece.color
                )
        }


        // ====================================================
        // KING MOVED
        // ====================================================

        if (
            piece.type ==
            PieceType.KING
        ) {

            if (
                piece.color ==
                PieceColor.WHITE
            ) {

                whiteKingMoved =
                    true

            } else {

                blackKingMoved =
                    true
            }
        }


        // ====================================================
        // ROOK MOVED
        // ====================================================

        if (
            piece.type ==
            PieceType.ROOK
        ) {

            if (
                fr == 7 &&
                fc == 0
            ) {
                whiteRookAMoved =
                    true
            }

            if (
                fr == 7 &&
                fc == 7
            ) {
                whiteRookHMoved =
                    true
            }

            if (
                fr == 0 &&
                fc == 0
            ) {
                blackRookAMoved =
                    true
            }

            if (
                fr == 0 &&
                fc == 7
            ) {
                blackRookHMoved =
                    true
            }
        }


        // ====================================================
        // ROOK CAPTURED
        // ====================================================

        if (
            tr == 7 &&
            tc == 0
        ) {
            whiteRookAMoved =
                true
        }

        if (
            tr == 7 &&
            tc == 7
        ) {
            whiteRookHMoved =
                true
        }

        if (
            tr == 0 &&
            tc == 0
        ) {
            blackRookAMoved =
                true
        }

        if (
            tr == 0 &&
            tc == 7
        ) {
            blackRookHMoved =
                true
        }


        // ====================================================
        // EN PASSANT TARGET
        // ====================================================

        enPassantTarget =
            if (
                piece.type ==
                PieceType.PAWN &&
                kotlin.math.abs(
                    tr - fr
                ) == 2
            ) {

                ((fr + tr) / 2) to fc

            } else {

                null
            }


        // ====================================================
        // CHANGE TURN
        // ====================================================

        turn =
            opposite(turn)


        return Move(
            fr,
            fc,
            tr,
            tc,
            piece,
            captured,
            isCastle,
            promotion,
            isEnPassant
        )
    }


    // ========================================================
    // APPLY UCI MOVE
    // ========================================================

    fun applyUci(
        uci: String
    ) {

        if (uci.length < 4) {
            return
        }


        val fc =
            uci[0] - 'a'

        val fr =
            8 - (
                    uci[1] - '0'
                    )


        val tc =
            uci[2] - 'a'

        val tr =
            8 - (
                    uci[3] - '0'
                    )


        val promo =
            if (
                uci.length >= 5
            ) {

                when (
                    uci[4].lowercaseChar()
                ) {

                    'q' ->
                        PieceType.QUEEN

                    'r' ->
                        PieceType.ROOK

                    'b' ->
                        PieceType.BISHOP

                    'n' ->
                        PieceType.KNIGHT

                    else ->
                        null
                }

            } else {

                null
            }


        movePiece(
            fr,
            fc,
            tr,
            tc,
            promo
        )
    }


    // ========================================================
    // FEN FOR STOCKFISH
    // ========================================================

    fun toFen(): String {

        val fen =
            StringBuilder()


        // ====================================================
        // BOARD POSITION
        // ====================================================

        for (r in 0..7) {

            var emptySquares = 0


            for (c in 0..7) {

                val piece =
                    squares[r][c]


                if (piece == null) {

                    emptySquares++

                } else {

                    if (
                        emptySquares > 0
                    ) {

                        fen.append(
                            emptySquares
                        )

                        emptySquares = 0
                    }


                    val symbol =
                        when (piece.type) {

                            PieceType.PAWN ->
                                'p'

                            PieceType.KNIGHT ->
                                'n'

                            PieceType.BISHOP ->
                                'b'

                            PieceType.ROOK ->
                                'r'

                            PieceType.QUEEN ->
                                'q'

                            PieceType.KING ->
                                'k'
                        }


                    if (
                        piece.color ==
                        PieceColor.WHITE
                    ) {

                        fen.append(
                            symbol.uppercaseChar()
                        )

                    } else {

                        fen.append(
                            symbol
                        )
                    }
                }
            }


            if (
                emptySquares > 0
            ) {

                fen.append(
                    emptySquares
                )
            }


            if (r != 7) {

                fen.append("/")
            }
        }


        // ====================================================
        // SIDE TO MOVE
        // ====================================================

        fen.append(" ")


        fen.append(
            if (
                turn ==
                PieceColor.WHITE
            ) {
                "w"
            } else {
                "b"
            }
        )


        // ====================================================
        // CASTLING RIGHTS
        // ====================================================

        fen.append(" ")


        val castling =
            StringBuilder()


        if (!whiteKingMoved) {

            if (!whiteRookHMoved) {
                castling.append("K")
            }

            if (!whiteRookAMoved) {
                castling.append("Q")
            }
        }


        if (!blackKingMoved) {

            if (!blackRookHMoved) {
                castling.append("k")
            }

            if (!blackRookAMoved) {
                castling.append("q")
            }
        }


        if (
            castling.isEmpty()
        ) {

            fen.append("-")

        } else {

            fen.append(
                castling.toString()
            )
        }


        // ====================================================
        // EN PASSANT TARGET
        // ====================================================

        fen.append(" ")


        val ep =
            enPassantTarget


        if (ep != null) {

            val row =
                ep.first

            val col =
                ep.second


            val file =
                ('a'.code + col)
                    .toChar()


            val rank =
                8 - row


            fen.append(
                "$file$rank"
            )

        } else {

            fen.append("-")
        }


        // ====================================================
        // HALF MOVE CLOCK
        // FULL MOVE NUMBER
        //
        // Abhi Board class in dono ko track nahi karta,
        // isliye Stockfish ke liye 0 1 use kar rahe hain.
        // Position/evaluation ke liye ye sufficient hai.
        // ====================================================

        fen.append(" 0 1")


        return fen.toString()
    }
}