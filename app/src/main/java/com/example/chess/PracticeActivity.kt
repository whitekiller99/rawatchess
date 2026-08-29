package com.example.chess

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chess.data.Opening
import com.example.chess.data.OpeningLine
import com.example.chess.data.OpeningStore
import com.example.chess.engine.Board
import com.example.chess.engine.PieceColor

class PracticeActivity : AppCompatActivity() {

    private lateinit var board: Board
    private lateinit var boardView: ChessBoardView

    private lateinit var opening: Opening
    private lateinit var currentLine: OpeningLine

    private var lineIndex = 0
    private var moveIndex = 0

    private var practiceColor = PieceColor.WHITE

    /*
     * learn  = Learn Opening
     * random = Practice Opening
     */
    private var mode = "learn"

    private var completed = false

    private lateinit var tvMode: TextView
    private lateinit var tvLine: TextView

    private lateinit var btnRetry: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private lateinit var btnPlayWhite: Button
    private lateinit var btnPlayBlack: Button

    private val handler =
        Handler(Looper.getMainLooper())


    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_practice
        )

        // --------------------------------------------------------
        // FIND VIEWS
        // --------------------------------------------------------

        tvMode =
            findViewById(
                R.id.tvPracticeMode
            )

        tvLine =
            findViewById(
                R.id.tvCurrentLine
            )

        btnRetry =
            findViewById(
                R.id.btnRetry
            )

        btnPrevious =
            findViewById(
                R.id.btnPrevious
            )

        btnNext =
            findViewById(
                R.id.btnNext
            )

        btnPlayWhite =
            findViewById(
                R.id.btnPlayWhite
            )

        btnPlayBlack =
            findViewById(
                R.id.btnPlayBlack
            )

        boardView =
            findViewById(
                R.id.chessBoardViewPractice
            )


        // --------------------------------------------------------
        // MODE
        // --------------------------------------------------------

        mode =
            intent.getStringExtra("mode")
                ?: "learn"


        // ========================================================
        // RANDOM PRACTICE
        // ========================================================

        if (mode == "random") {

            chooseRandomLine()

            if (!::currentLine.isInitialized) {

                Toast.makeText(
                    this,
                    "No opening lines available",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
                return
            }

            tvMode.text =
                "Practice Opening"
        }

        // ========================================================
        // LEARN OPENING
        // ========================================================

        else {

            val openingId =
                intent.getStringExtra(
                    "opening_id"
                )

            val foundOpening =
                OpeningStore.getAll(this)
                    .find {
                        it.id == openingId
                    }

            if (foundOpening == null) {

                Toast.makeText(
                    this,
                    "Opening not found",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
                return
            }

            opening =
                foundOpening

            if (opening.lines.isEmpty()) {

                Toast.makeText(
                    this,
                    "No lines available",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
                return
            }

            lineIndex =
                intent.getIntExtra(
                    "line_index",
                    0
                ).coerceIn(
                    0,
                    opening.lines.lastIndex
                )

            currentLine =
                opening.lines[lineIndex]

            tvMode.text =
                "Learn Opening"
        }


        // ========================================================
        // MOVE LISTENER
        // ========================================================

        boardView.onMoveListener =
            { move ->

                onUserMove(
                    move.toUci()
                )
            }


        // ========================================================
        // PLAY WHITE
        // ========================================================

        btnPlayWhite.setOnClickListener {

            startPractice(
                PieceColor.WHITE
            )
        }


        // ========================================================
        // PLAY BLACK
        // ========================================================

        btnPlayBlack.setOnClickListener {

            startPractice(
                PieceColor.BLACK
            )
        }


        // ========================================================
        // RETRY
        // ========================================================

        btnRetry.setOnClickListener {

            startPractice(
                practiceColor
            )
        }


        // ========================================================
        // PREVIOUS LINE
        // ========================================================

        btnPrevious.setOnClickListener {

            if (
                mode == "learn" &&
                lineIndex > 0
            ) {

                lineIndex--

                currentLine =
                    opening.lines[lineIndex]

                startPractice(
                    practiceColor
                )
            }
        }


        // ========================================================
        // NEXT LINE
        // ========================================================

        btnNext.setOnClickListener {

            if (mode == "learn") {

                if (
                    lineIndex <
                    opening.lines.lastIndex
                ) {

                    lineIndex++

                    currentLine =
                        opening.lines[lineIndex]

                    startPractice(
                        practiceColor
                    )
                }

            } else {

                // Practice mode:
                // Same opening ki random line
                chooseRandomLineFromSameOpening()

                if (
                    ::currentLine.isInitialized
                ) {

                    startPractice(
                        practiceColor
                    )
                }
            }
        }


        // ========================================================
        // START
        // ========================================================

        startPractice(
            PieceColor.WHITE
        )
    }


    // ============================================================
    // RANDOM LINE
    // ============================================================
    //
    // Practice Opening me kisi bhi opening ki random line.
    //
    // Agar tum specific opening se Practice Opening khologe,
    // to neeche wala function same opening ki random line
    // select kar sakta hai.
    // ============================================================

    private fun chooseRandomLine() {

        val allLines =
            OpeningStore.getAll(this)
                .flatMap { op ->

                    op.lines
                        .filter {
                            it.moves.isNotEmpty()
                        }
                        .map {
                            op to it
                        }
                }


        if (allLines.isEmpty()) {
            return
        }


        val selected =
            allLines.random()


        opening =
            selected.first

        currentLine =
            selected.second


        lineIndex =
            opening.lines.indexOfFirst {

                it.id ==
                        currentLine.id
            }


        tvLine.text =
            "${opening.name} • ${lineName()}"
    }


    // ============================================================
    // RANDOM LINE FROM SAME OPENING
    // ============================================================

    private fun chooseRandomLineFromSameOpening() {

        val availableLines =
            opening.lines.filter {
                it.moves.isNotEmpty()
            }


        if (availableLines.isEmpty()) {
            return
        }


        val selected =
            availableLines.random()


        currentLine =
            selected


        lineIndex =
            opening.lines.indexOfFirst {

                it.id ==
                        currentLine.id
            }


        tvLine.text =
            "${opening.name} • ${lineName()}"
    }


    // ============================================================
    // START PRACTICE
    // ============================================================

    private fun startPractice(
        color: PieceColor
    ) {

        handler.removeCallbacksAndMessages(
            null
        )


        practiceColor =
            color
        boardView.boardFlipped =
            color == PieceColor.BLACK

        moveIndex =
            0

        completed =
            false


        // --------------------------------------------------------
        // NEW BOARD
        // --------------------------------------------------------

        board =
            Board()

        boardView.board =
            board


        // --------------------------------------------------------
        // NORMAL CHESS MOVE MODE
        // --------------------------------------------------------

        boardView.annotationMode =
            "NONE"


        // --------------------------------------------------------
        // START PAR KOI ANNOTATION NAHI
        // --------------------------------------------------------

        boardView.setAnnotations(
            emptyList()
        )


        boardView.interactive =
            true


        tvLine.text =
            "${opening.name} • ${lineName()}"


        hideCompletionButtons()


        maybeAutoMove()
    }


    // ============================================================
    // LINE NAME
    // ============================================================

    private fun lineName(): String {

        return if (
            currentLine.name.isNotBlank()
        ) {

            currentLine.name

        } else {

            "Line %02d".format(
                lineIndex + 1
            )
        }
    }


    // ============================================================
    // AUTO MOVE
    // ============================================================

    private fun maybeAutoMove() {

        // ========================================================
        // LINE COMPLETE
        // ========================================================

        if (
            moveIndex >=
            currentLine.moves.size
        ) {

            completed =
                true

            boardView.interactive =
                false


            // ----------------------------------------------------
            // PRACTICE MODE
            // Sirf LAST move ki annotation
            // ----------------------------------------------------

            if (mode == "random") {

                showLastMoveAnnotations()

            }

            // ----------------------------------------------------
            // LEARN MODE
            // Last/current position ki annotation
            // ----------------------------------------------------

            else {

                showCurrentLearnAnnotations()
            }


            showCompletionButtons()

            return
        }


        // ========================================================
        // OPPONENT MOVE
        // ========================================================

        if (
            board.turn !=
            practiceColor
        ) {

            boardView.interactive =
                false


            // 1.2 SECOND DELAY
            handler.postDelayed({

                if (
                    isFinishing ||
                    isDestroyed ||
                    completed
                ) {

                    return@postDelayed
                }


                if (
                    moveIndex <
                    currentLine.moves.size &&
                    board.turn !=
                    practiceColor
                ) {

                    board.applyUci(
                        currentLine.moves[
                            moveIndex
                        ]
                    )

                    moveIndex++


                    // ------------------------------------------------
                    // IMPORTANT
                    // Opponent move complete hone ke baad
                    // current move ki annotation dikhao.
                    // ------------------------------------------------

                    if (mode == "learn") {

                        showCurrentLearnAnnotations()

                    } else {

                        boardView.setAnnotations(
                            emptyList()
                        )
                    }


                    boardView.refresh()
                }


                maybeAutoMove()

            }, 1200)

        }

        // ========================================================
        // USER TURN
        // ========================================================

        else {

            boardView.interactive =
                true


            if (mode == "learn") {

                /*
                 * Yahan current move ki annotation
                 * already screen par rahegi.
                 *
                 * Isliye yahan annotation clear nahi karenge.
                 */

                showCurrentLearnAnnotations()

            } else {

                boardView.setAnnotations(
                    emptyList()
                )
            }
        }
    }


    // ============================================================
    // USER MOVE
    // ============================================================

    private fun onUserMove(
        playedUci: String
    ) {

        if (
            completed ||
            moveIndex >=
            currentLine.moves.size
        ) {

            return
        }


        val expected =
            currentLine.moves[
                moveIndex
            ]


        // ========================================================
        // CORRECT MOVE
        // ========================================================

        if (
            playedUci.equals(
                expected,
                ignoreCase = true
            )
        ) {

            moveIndex++


            if (mode == "learn") {

                /*
                 * IMPORTANT:
                 *
                 * User ne current move complete kar diya.
                 * Ab turant next move ki annotation dikhegi.
                 *
                 * Agar next move opponent ka hai,
                 * to maybeAutoMove() us move ko 1.2 sec
                 * baad play karega.
                 */

                showCurrentLearnAnnotations()

            } else {

                // Practice ke dauran hidden
                boardView.setAnnotations(
                    emptyList()
                )
            }


            maybeAutoMove()
        }


        // ========================================================
        // WRONG MOVE
        // ========================================================

        else {

            /*
             * Wrong move hone par board ko
             * current correct position par wapas lao.
             *
             * moveIndex change nahi hoga.
             */

            rebuildBoardToMoveIndex()


            if (mode == "learn") {

                /*
                 * Wahi current move ki annotation
                 * dobara dikhao.
                 */

                showCurrentLearnAnnotations()

            } else {

                boardView.setAnnotations(
                    emptyList()
                )
            }


            Toast.makeText(
                this,
                "Wrong move. Try again.",
                Toast.LENGTH_SHORT
            ).show()


            boardView.interactive =
                true
        }
    }


    // ============================================================
    // SHOW LEARN ANNOTATIONS
    // ============================================================

    private fun showCurrentLearnAnnotations() {

        if (mode != "learn") {

            boardView.setAnnotations(
                emptyList()
            )

            return
        }


        /*
         * marks ka structure:
         *
         * marks[0] = Move 1 ki annotation
         * marks[1] = Move 2 ki annotation
         * marks[2] = Move 3 ki annotation
         *
         * moveIndex:
         *
         * 0 = koi move complete nahi
         * 1 = Move 1 complete
         * 2 = Move 2 complete
         *
         * Isliye completed move ki annotation:
         * moveIndex - 1
         */

        val annotationIndex =
            moveIndex - 1


        if (
            annotationIndex < 0
        ) {

            boardView.setAnnotations(
                emptyList()
            )

            return
        }


        val marks =
            currentLine.marks
                ?.getOrNull(
                    annotationIndex
                )
                ?: emptyList()


        boardView.setAnnotations(
            marks
        )
    }


    // ============================================================
    // SHOW LAST MOVE ANNOTATIONS
    // ============================================================

    private fun showLastMoveAnnotations() {

        /*
         * Practice complete hone ke baad
         * sirf LAST move ki annotation show hogi.
         */

        val marks =
            currentLine.marks
                ?.lastOrNull()
                ?: emptyList()


        boardView.setAnnotations(
            marks
        )
    }


    // ============================================================
    // REBUILD BOARD
    // ============================================================

    private fun rebuildBoardToMoveIndex() {

        handler.removeCallbacksAndMessages(
            null
        )


        board =
            Board()


        val safeMoveIndex =
            moveIndex.coerceAtMost(
                currentLine.moves.size
            )


        for (
        i in 0 until safeMoveIndex
        ) {

            board.applyUci(
                currentLine.moves[i]
            )
        }


        boardView.board =
            board


        /*
         * Wrong move ke baad:
         *
         * Learn = current move ki annotation
         * Practice = hidden
         */

        if (mode == "learn") {

            showCurrentLearnAnnotations()

        } else {

            boardView.setAnnotations(
                emptyList()
            )
        }
    }


    // ============================================================
    // COMPLETION BUTTONS
    // ============================================================

    private fun showCompletionButtons() {

        val completionButtons =
            findViewById<View>(
                R.id.completionButtons
            )


        completionButtons.visibility =
            View.VISIBLE


        btnRetry.visibility =
            View.VISIBLE


        btnNext.visibility =
            View.VISIBLE


        btnPrevious.visibility =
            if (
                mode == "learn"
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }


        // --------------------------------------------------------
        // PREVIOUS
        // --------------------------------------------------------

        btnPrevious.isEnabled =
            mode == "learn" &&
                    lineIndex > 0


        // --------------------------------------------------------
        // NEXT
        // --------------------------------------------------------

        btnNext.isEnabled =
            if (mode == "random") {

                true

            } else {

                lineIndex <
                        opening.lines.lastIndex
            }


        // --------------------------------------------------------
        // BUTTON TEXT
        // --------------------------------------------------------

        btnNext.text =
            if (mode == "random") {

                "Next Random"

            } else {

                "Next Line"
            }
    }


    // ============================================================
    // HIDE COMPLETION BUTTONS
    // ============================================================

    private fun hideCompletionButtons() {

        findViewById<View>(
            R.id.completionButtons
        ).visibility =
            View.GONE
    }


    // ============================================================
    // DESTROY
    // ============================================================

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }
}