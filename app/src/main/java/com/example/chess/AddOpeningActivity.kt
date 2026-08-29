package com.example.chess

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chess.data.BoardMark
import com.example.chess.data.Opening
import com.example.chess.data.OpeningLine
import com.example.chess.data.OpeningStore
import com.example.chess.engine.Board
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType

class AddOpeningActivity : AppCompatActivity() {

    // ============================================================
    // MOVES
    // ============================================================

    private val moveList =
        mutableListOf<String>()

    private val moveMarks =
        mutableListOf<MutableList<BoardMark>>()

    // ============================================================
    // BOARD
    // ============================================================

    private lateinit var board: Board
    private lateinit var boardView: ChessBoardView

    // ============================================================
    // TEXT
    // ============================================================

    private lateinit var tvMoves: TextView
    private lateinit var tvEvaluation: TextView

    // ============================================================
    // STOCKFISH
    // ============================================================

    private var stockfish: StockfishEngine? = null

    private lateinit var evalBar: EvalBarView

    private val mainHandler =
        Handler(Looper.getMainLooper())

    // ============================================================
    // EDIT MODE
    // ============================================================

    private var editMode = false
    private var editingOpeningId: String? = null
    private var editingLineId: String? = null

    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_add_opening
        )

        // ========================================================
        // EDIT MODE
        // ========================================================

        editMode =
            intent.getBooleanExtra(
                "edit_mode",
                false
            )

        editingOpeningId =
            intent.getStringExtra(
                "opening_id"
            )

        editingLineId =
            intent.getStringExtra(
                "line_id"
            )

        // ========================================================
        // BOARD
        // ========================================================

        board =
            Board()

        boardView =
            findViewById(
                R.id.chessBoardView
            )

        boardView.board =
            board

        // ========================================================
        // TEXT
        // ========================================================

        tvMoves =
            findViewById(
                R.id.tvMoveList
            )

        tvEvaluation =
            findViewById(
                R.id.tvEvaluation
            )

        // ========================================================
        // EVALUATION BAR
        // ========================================================

        evalBar =
            findViewById(
                R.id.evalBar
            )

        // ========================================================
        // STOCKFISH
        // ========================================================

        stockfish =
            StockfishEngine(this)

        tvEvaluation.text =
            "Starting engine..."

        Thread {

            val engine =
                stockfish

            val started =
                engine?.start() == true

            mainHandler.post {

                if (started) {

                    tvEvaluation.text =
                        "0.00"

                    evalBar.setEvaluation(
                        0.0
                    )

                    // Starting position evaluate
                    evaluatePosition()

                } else {

                    tvEvaluation.text =
                        "Stockfish error"

                    evalBar.setEvaluation(
                        0.0
                    )
                }
            }

        }.start()

        // ========================================================
        // MOVE MODE
        // ========================================================

        findViewById<Button>(
            R.id.btnMoveMode
        ).setOnClickListener {

            boardView.annotationMode =
                "NONE"

            Toast.makeText(
                this,
                "Move mode",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ========================================================
        // CIRCLE
        // ========================================================

        findViewById<Button>(
            R.id.btnCircle
        ).setOnClickListener {

            boardView.annotationMode =
                "CIRCLE"
        }

        // ========================================================
        // ARROW
        // ========================================================

        findViewById<Button>(
            R.id.btnArrow
        ).setOnClickListener {

            boardView.annotationMode =
                "ARROW"
        }

        // ========================================================
        // GREEN
        // ========================================================

        findViewById<Button>(
            R.id.btnGreen
        ).setOnClickListener {

            boardView.annotationColor =
                "GREEN"
        }

        // ========================================================
        // RED
        // ========================================================

        findViewById<Button>(
            R.id.btnRed
        ).setOnClickListener {

            boardView.annotationColor =
                "RED"
        }

        // ========================================================
        // CLEAR MARKS
        // ========================================================

        findViewById<Button>(
            R.id.btnClearMarks
        ).setOnClickListener {

            if (moveMarks.isNotEmpty()) {

                moveMarks.last().clear()

                boardView.setAnnotations(
                    moveMarks.last()
                )

                boardView.onAnnotationsChanged?.invoke(
                    moveMarks.last()
                )
            }
        }

        // ========================================================
        // ANNOTATION CHANGED
        // ========================================================

        boardView.onAnnotationsChanged = {

                marks ->

            if (moveMarks.isNotEmpty()) {

                moveMarks[
                    moveMarks.lastIndex
                ] =
                    marks.toMutableList()
            }
        }

        // ========================================================
        // MOVE PLAYED
        // ========================================================

        boardView.onMoveListener = {

                move ->

            // ----------------------------------------------------
            // SAVE MOVE
            // ----------------------------------------------------

            moveList.add(
                move.toUci()
            )

            // ----------------------------------------------------
            // NEW MARKS
            // ----------------------------------------------------

            moveMarks.add(
                mutableListOf()
            )

            // ----------------------------------------------------
            // SHOW MOVES
            // ----------------------------------------------------

            tvMoves.text =
                moveList.joinToString(" ")

            // ----------------------------------------------------
            // CLEAR MARKS
            // ----------------------------------------------------

            boardView.setAnnotations(
                emptyList()
            )

            boardView.annotationMode =
                "NONE"

            // ----------------------------------------------------
            // EVALUATE POSITION
            // ----------------------------------------------------

            evaluatePosition()
        }

        // ========================================================
        // UNDO
        // ========================================================

        findViewById<Button>(
            R.id.btnUndo
        ).setOnClickListener {

            if (moveList.isNotEmpty()) {

                moveList.removeAt(
                    moveList.lastIndex
                )

                if (moveMarks.isNotEmpty()) {

                    moveMarks.removeAt(
                        moveMarks.lastIndex
                    )
                }

                rebuildBoard()

                tvMoves.text =
                    moveList.joinToString(" ")

                if (moveMarks.isNotEmpty()) {

                    boardView.setAnnotations(
                        moveMarks.last()
                    )

                } else {

                    boardView.setAnnotations(
                        emptyList()
                    )
                }

                // ------------------------------------------------
                // UPDATE EVALUATION AFTER UNDO
                // ------------------------------------------------

                evaluatePosition()

            } else {

                Toast.makeText(
                    this,
                    "No move to undo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ========================================================
        // RESET
        // ========================================================

        findViewById<Button>(
            R.id.btnReset
        ).setOnClickListener {

            // ----------------------------------------------------
            // CLEAR MOVES
            // ----------------------------------------------------

            moveList.clear()

            moveMarks.clear()

            // ----------------------------------------------------
            // NEW BOARD
            // ----------------------------------------------------

            board =
                Board()

            boardView.board =
                board

            // ----------------------------------------------------
            // CLEAR ANNOTATIONS
            // ----------------------------------------------------

            boardView.setAnnotations(
                emptyList()
            )

            boardView.annotationMode =
                "NONE"

            // ----------------------------------------------------
            // CLEAR MOVE TEXT
            // ----------------------------------------------------

            tvMoves.text =
                ""

            // ----------------------------------------------------
            // RESET EVALUATION
            // ----------------------------------------------------

            tvEvaluation.text =
                "0.00"

            evalBar.setEvaluation(
                0.0
            )

            // ----------------------------------------------------
            // EVALUATE STARTING POSITION
            // ----------------------------------------------------

            evaluatePosition()
        }

        // ========================================================
        // SAVE
        // ========================================================

        findViewById<Button>(
            R.id.btnSave
        ).setOnClickListener {

            if (editMode) {

                updateExistingLine()

            } else {

                saveOpeningLine()
            }
        }

        // ========================================================
        // LOAD EDITING LINE
        // ========================================================

        if (editMode) {

            loadLineForEditing()
        }
    }

    // ============================================================
    // STOCKFISH EVALUATION
    // ============================================================

    private fun evaluatePosition() {

        val engine =
            stockfish
                ?: return

        val fen =
            createFen()

        engine.evaluate(
            fen,
            15
        ) { evaluation ->

            mainHandler.post {

                val value =
                    evaluation
                        .toDoubleOrNull()
                        ?: 0.0

                tvEvaluation.text =
                    if (value > 0) {

                        "+%.2f".format(
                            value
                        )

                    } else {

                        "%.2f".format(
                            value
                        )
                    }

                evalBar.setEvaluation(
                    value
                )
            }
        }
    }

    // ============================================================
    // CREATE FEN
    // ============================================================

    private fun createFen(): String {

        val fen =
            StringBuilder()

        // ========================================================
        // PIECES
        // ========================================================

        for (r in 0..7) {

            var empty =
                0

            for (c in 0..7) {

                val piece =
                    board.pieceAt(
                        r,
                        c
                    )

                if (piece == null) {

                    empty++

                } else {

                    if (empty > 0) {

                        fen.append(
                            empty
                        )

                        empty = 0
                    }

                    val char =
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
                            char.uppercaseChar()
                        )

                    } else {

                        fen.append(
                            char
                        )
                    }
                }
            }

            if (empty > 0) {

                fen.append(
                    empty
                )
            }

            if (r != 7) {

                fen.append("/")
            }
        }

        // ========================================================
        // SIDE TO MOVE
        // ========================================================

        fen.append(" ")

        fen.append(
            if (
                board.turn ==
                PieceColor.WHITE
            ) {

                "w"

            } else {

                "b"
            }
        )

        // ========================================================
        // CASTLING
        // ========================================================

        fen.append(
            " -"
        )

        // ========================================================
        // EN PASSANT
        // ========================================================

        fen.append(
            " -"
        )

        // ========================================================
        // HALF MOVE / FULL MOVE
        // ========================================================

        fen.append(
            " 0 1"
        )

        return fen.toString()
    }

    // ============================================================
    // LOAD EXISTING LINE
    // ============================================================

    private fun loadLineForEditing() {

        val opening =
            OpeningStore.getAll(this)
                .find {
                    it.id ==
                            editingOpeningId
                }
                ?: run {

                    finish()

                    return
                }

        val line =
            opening.lines.find {
                it.id ==
                        editingLineId
            }
                ?: run {

                    finish()

                    return
                }

        findViewById<EditText>(
            R.id.etOpeningName
        ).setText(
            opening.name
        )

        // --------------------------------------------------------
        // LOAD MOVES
        // --------------------------------------------------------

        moveList.clear()

        moveList.addAll(
            line.moves
        )

        // --------------------------------------------------------
        // LOAD MARKS
        // --------------------------------------------------------

        moveMarks.clear()

        val savedMarks =
            line.marks
                ?: mutableListOf()

        for (i in moveList.indices) {

            moveMarks.add(

                savedMarks
                    .getOrNull(i)
                    ?.toMutableList()
                    ?: mutableListOf()
            )
        }

        // --------------------------------------------------------
        // SHOW MOVES
        // --------------------------------------------------------

        tvMoves.text =
            moveList.joinToString(" ")

        // --------------------------------------------------------
        // REBUILD BOARD
        // --------------------------------------------------------

        rebuildBoard()

        // --------------------------------------------------------
        // SHOW LAST MARKS
        // --------------------------------------------------------

        if (moveMarks.isNotEmpty()) {

            boardView.setAnnotations(
                moveMarks.last()
            )

        } else {

            boardView.setAnnotations(
                emptyList()
            )
        }

        // --------------------------------------------------------
        // CHANGE BUTTON
        // --------------------------------------------------------

        findViewById<Button>(
            R.id.btnSave
        ).text =
            "Update Line"

        // --------------------------------------------------------
        // EVALUATE LOADED POSITION
        // --------------------------------------------------------

        evaluatePosition()
    }

    // ============================================================
    // UPDATE EXISTING LINE
    // ============================================================

    private fun updateExistingLine() {

        if (moveList.isEmpty()) {

            Toast.makeText(
                this,
                "Please keep at least one move",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val openingId =
            editingOpeningId
                ?: return

        val lineId =
            editingLineId
                ?: return

        OpeningStore.updateLine(
            this,
            openingId,
            lineId,
            moveList,
            moveMarks
        )

        Toast.makeText(
            this,
            "Line updated",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    // ============================================================
    // SAVE NEW OPENING / LINE
    // ============================================================

    private fun saveOpeningLine() {

        val etName =
            findViewById<EditText>(
                R.id.etOpeningName
            )

        val name =
            etName.text
                .toString()
                .trim()

        if (name.isEmpty()) {

            Toast.makeText(
                this,
                "Please enter an opening name",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (moveList.isEmpty()) {

            Toast.makeText(
                this,
                "Please play at least one move",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val openings =
            OpeningStore.getAll(this)

        val existing =
            openings.find {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            }

        if (existing != null) {

            OpeningStore.addLine(
                this,
                existing.id,
                moveList,
                moveMarks
            )

            Toast.makeText(
                this,
                "New line added",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val opening =
                Opening(
                    name = name
                )

            opening.lines.add(

                OpeningLine(

                    name =
                        "${name.replace(
                            "\\s+".toRegex(),
                            ""
                        )}01",

                    moves =
                        moveList.toMutableList(),

                    marks =
                        moveMarks.map {
                            it.toMutableList()
                        }.toMutableList()
                )
            )

            OpeningStore.add(
                this,
                opening
            )

            Toast.makeText(
                this,
                "Opening saved",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    // ============================================================
    // REBUILD BOARD
    // ============================================================

    private fun rebuildBoard() {

        board =
            Board()

        for (uci in moveList) {

            board.applyUci(
                uci
            )
        }

        boardView.board =
            board
    }

    // ============================================================
    // ACTIVITY DESTROY
    // ============================================================

    override fun onDestroy() {

        // --------------------------------------------------------
        // STOP STOCKFISH
        // --------------------------------------------------------

        try {

            stockfish?.stop()

        } catch (_: Exception) {
        }

        // --------------------------------------------------------
        // CLOSE STOCKFISH
        // --------------------------------------------------------

        try {

            stockfish?.close()

        } catch (_: Exception) {
        }

        stockfish =
            null

        // --------------------------------------------------------
        // REMOVE PENDING CALLBACKS
        // --------------------------------------------------------

        mainHandler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }
}