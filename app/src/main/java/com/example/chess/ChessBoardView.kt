package com.example.chess

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.chess.data.BoardMark
import com.example.chess.engine.Board
import com.example.chess.engine.Move
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ============================================================
    // BOARD
    // ============================================================

    var board: Board = Board()
        set(value) {
            field = value
            selected = null
            legalDest = emptyList()
            invalidate()
        }

    var onMoveListener: ((Move) -> Unit)? = null

    var interactive: Boolean = true

    // ============================================================
    // BOARD ORIENTATION
    // ============================================================

    /*
     * false = White bottom / normal board
     * true  = Black bottom / flipped board
     */
    var boardFlipped: Boolean = false
        set(value) {
            field = value
            selected = null
            legalDest = emptyList()
            invalidate()
        }

    // ============================================================
    // ANNOTATION SYSTEM
    // ============================================================

    var annotationMode: String = "NONE"

    var annotationColor: String = "GREEN"

    private var annotations =
        mutableListOf<BoardMark>()

    private var annotationStart: Pair<Int, Int>? = null

    var onAnnotationsChanged:
            ((List<BoardMark>) -> Unit)? = null

    // ============================================================
    // CHESS MOVE VARIABLES
    // ============================================================

    private var selected: Pair<Int, Int>? = null

    private var legalDest:
            List<Pair<Int, Int>> = emptyList()

    private var squareSize = 0f

    // ============================================================
    // BOARD COLORS
    // ============================================================

    private val lightColor =
        Color.parseColor("#EEEED2")

    private val darkColor =
        Color.parseColor("#769656")

    private val selectColor =
        Color.parseColor("#F6F669")

    private val dotColor =
        Color.parseColor("#646F40")

    // ============================================================
    // PAINT
    // ============================================================

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }

    // ============================================================
    // CHESS PIECES
    // ============================================================

    private val whiteKing: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_king
        )

    private val whiteQueen: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_queen
        )

    private val whiteRook: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_rook
        )

    private val whiteBishop: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_bishop
        )

    private val whiteKnight: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_knight
        )

    private val whitePawn: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.white_pawn
        )

    private val blackKing: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_king
        )

    private val blackQueen: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_queen
        )

    private val blackRook: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_rook
        )

    private val blackBishop: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_bishop
        )

    private val blackKnight: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_knight
        )

    private val blackPawn: Bitmap =
        BitmapFactory.decodeResource(
            resources,
            R.drawable.black_pawn
        )

    // ============================================================
    // MEASURE
    // ============================================================

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {

        val width =
            MeasureSpec.getSize(
                widthMeasureSpec
            )

        val height =
            MeasureSpec.getSize(
                heightMeasureSpec
            )

        val size =
            minOf(width, height)

        setMeasuredDimension(
            size,
            size
        )

        squareSize =
            size / 8f
    }

    // ============================================================
    // DISPLAY COORDINATES
    // ============================================================

    /*
     * Internal chess coordinates ko screen coordinates
     * me convert karta hai.
     *
     * Normal:
     * a8 = top-left
     *
     * Flipped:
     * h1 = top-left
     */
    private fun displayRow(
        row: Int
    ): Int {

        return if (boardFlipped) {
            7 - row
        } else {
            row
        }
    }

    private fun displayCol(
        col: Int
    ): Int {

        return if (boardFlipped) {
            7 - col
        } else {
            col
        }
    }

    /*
     * Screen coordinate ko internal chess coordinate
     * me convert karta hai.
     */
    private fun boardRow(
        screenRow: Int
    ): Int {

        return if (boardFlipped) {
            7 - screenRow
        } else {
            screenRow
        }
    }

    private fun boardCol(
        screenCol: Int
    ): Int {

        return if (boardFlipped) {
            7 - screenCol
        } else {
            screenCol
        }
    }

    // ============================================================
    // DRAW
    // ============================================================

    override fun onDraw(
        canvas: Canvas
    ) {

        super.onDraw(canvas)

        // ========================================================
        // 1. BOARD
        // ========================================================

        for (screenRow in 0..7) {

            for (screenCol in 0..7) {

                /*
                 * Board colors bhi flip orientation ke according
                 * calculate honge.
                 */

                val actualRow =
                    boardRow(screenRow)

                val actualCol =
                    boardCol(screenCol)

                paint.color =
                    if (
                        (actualRow + actualCol) % 2 == 0
                    ) {
                        lightColor
                    } else {
                        darkColor
                    }

                canvas.drawRect(
                    screenCol * squareSize,
                    screenRow * squareSize,
                    (screenCol + 1) * squareSize,
                    (screenRow + 1) * squareSize,
                    paint
                )
            }
        }

        // ========================================================
        // 2. SELECTED SQUARE
        // ========================================================

        selected?.let { (sr, sc) ->

            val screenRow =
                displayRow(sr)

            val screenCol =
                displayCol(sc)

            paint.color =
                selectColor

            canvas.drawRect(
                screenCol * squareSize,
                screenRow * squareSize,
                (screenCol + 1) * squareSize,
                (screenRow + 1) * squareSize,
                paint
            )
        }

        // ========================================================
        // 3. LEGAL MOVE DOTS
        // ========================================================

        for ((dr, dc) in legalDest) {

            val screenRow =
                displayRow(dr)

            val screenCol =
                displayCol(dc)

            paint.color =
                dotColor

            canvas.drawCircle(
                screenCol * squareSize +
                        squareSize / 2f,

                screenRow * squareSize +
                        squareSize / 2f,

                squareSize * 0.13f,

                paint
            )
        }

        // ========================================================
        // 4. CHESS PIECES
        // ========================================================

        for (r in 0..7) {

            for (c in 0..7) {

                val piece =
                    board.pieceAt(r, c)
                        ?: continue

                val bitmap =
                    getPieceBitmap(piece)
                        ?: continue

                val screenRow =
                    displayRow(r)

                val screenCol =
                    displayCol(c)

                val padding =
                    squareSize * 0.04f

                val left =
                    screenCol * squareSize +
                            padding

                val top =
                    screenRow * squareSize +
                            padding

                val right =
                    (screenCol + 1) * squareSize -
                            padding

                val bottom =
                    (screenRow + 1) * squareSize -
                            padding

                val destination =
                    RectF(
                        left,
                        top,
                        right,
                        bottom
                    )

                canvas.drawBitmap(
                    bitmap,
                    null,
                    destination,
                    paint
                )
            }
        }

        // ========================================================
        // 5. ANNOTATIONS
        // ========================================================

        drawAnnotations(canvas)
    }

    // ============================================================
    // DRAW ANNOTATIONS
    // ============================================================

    private fun drawAnnotations(
        canvas: Canvas
    ) {

        for (mark in annotations) {

            paint.style =
                Paint.Style.STROKE

            // MOTA ARROW / CIRCLE
            paint.strokeWidth =
                squareSize * 0.11f

            // TRANSPARENT
            paint.alpha =
                120

            paint.strokeCap =
                Paint.Cap.ROUND

            paint.color =
                if (
                    mark.color.equals(
                        "RED",
                        ignoreCase = true
                    )
                ) {

                    Color.rgb(
                        220,
                        50,
                        50
                    )

                } else {

                    Color.rgb(
                        40,
                        180,
                        70
                    )
                }

            // ====================================================
            // CIRCLE
            // ====================================================

            if (
                mark.type.equals(
                    "CIRCLE",
                    ignoreCase = true
                )
            ) {

                val col =
                    mark.from[0] - 'a'

                val rank =
                    mark.from[1].digitToInt()

                val row =
                    8 - rank

                val screenCol =
                    displayCol(col)

                val screenRow =
                    displayRow(row)

                canvas.drawCircle(
                    screenCol * squareSize +
                            squareSize / 2f,

                    screenRow * squareSize +
                            squareSize / 2f,

                    // Circle square ke almost barabar
                    squareSize * 0.46f,

                    paint
                )
            }

            // ====================================================
            // ARROW
            // ====================================================

            else if (
                mark.type.equals(
                    "ARROW",
                    ignoreCase = true
                ) &&
                mark.to != null
            ) {

                val fromCol =
                    mark.from[0] - 'a'

                val fromRow =
                    8 -
                            mark.from[1]
                                .digitToInt()

                val toCol =
                    mark.to[0] - 'a'

                val toRow =
                    8 -
                            mark.to[1]
                                .digitToInt()

                val screenFromCol =
                    displayCol(fromCol)

                val screenFromRow =
                    displayRow(fromRow)

                val screenToCol =
                    displayCol(toCol)

                val screenToRow =
                    displayRow(toRow)

                val startX =
                    screenFromCol * squareSize +
                            squareSize / 2f

                val startY =
                    screenFromRow * squareSize +
                            squareSize / 2f

                val endX =
                    screenToCol * squareSize +
                            squareSize / 2f

                val endY =
                    screenToRow * squareSize +
                            squareSize / 2f

                // Arrow line
                canvas.drawLine(
                    startX,
                    startY,
                    endX,
                    endY,
                    paint
                )

                // =================================================
                // ARROW HEAD
                // =================================================

                val angle =
                    atan2(
                        endY - startY,
                        endX - startX
                    )

                val headSize =
                    squareSize * 0.18f

                val angle1 =
                    angle +
                            Math.PI * 0.75

                val angle2 =
                    angle -
                            Math.PI * 0.75

                canvas.drawLine(
                    endX,
                    endY,

                    endX +
                            cos(angle1)
                                .toFloat() *
                            headSize,

                    endY +
                            sin(angle1)
                                .toFloat() *
                            headSize,

                    paint
                )

                canvas.drawLine(
                    endX,
                    endY,

                    endX +
                            cos(angle2)
                                .toFloat() *
                            headSize,

                    endY +
                            sin(angle2)
                                .toFloat() *
                            headSize,

                    paint
                )
            }
        }

        // Paint normal
        paint.style =
            Paint.Style.FILL

        paint.strokeCap =
            Paint.Cap.BUTT

        paint.alpha =
            255
    }

    // ============================================================
    // GET PIECE IMAGE
    // ============================================================

    private fun getPieceBitmap(
        piece: Piece
    ): Bitmap? {

        return when (piece.color) {

            PieceColor.WHITE -> {

                when (piece.type) {

                    PieceType.KING ->
                        whiteKing

                    PieceType.QUEEN ->
                        whiteQueen

                    PieceType.ROOK ->
                        whiteRook

                    PieceType.BISHOP ->
                        whiteBishop

                    PieceType.KNIGHT ->
                        whiteKnight

                    PieceType.PAWN ->
                        whitePawn
                }
            }

            PieceColor.BLACK -> {

                when (piece.type) {

                    PieceType.KING ->
                        blackKing

                    PieceType.QUEEN ->
                        blackQueen

                    PieceType.ROOK ->
                        blackRook

                    PieceType.BISHOP ->
                        blackBishop

                    PieceType.KNIGHT ->
                        blackKnight

                    PieceType.PAWN ->
                        blackPawn
                }
            }
        }
    }

    // ============================================================
    // ANNOTATION TOUCH
    // ============================================================

    private fun handleAnnotationTouch(
        event: MotionEvent
    ): Boolean {

        if (squareSize <= 0f) {
            return true
        }

        val screenCol =
            (event.x / squareSize)
                .toInt()
                .coerceIn(0, 7)

        val screenRow =
            (event.y / squareSize)
                .toInt()
                .coerceIn(0, 7)

        val col =
            boardCol(screenCol)

        val row =
            boardRow(screenRow)

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                if (
                    annotationMode.equals(
                        "CIRCLE",
                        ignoreCase = true
                    )
                ) {

                    addCircle(
                        row,
                        col
                    )

                } else if (
                    annotationMode.equals(
                        "ARROW",
                        ignoreCase = true
                    )
                ) {

                    annotationStart =
                        row to col
                }

                return true
            }

            MotionEvent.ACTION_UP -> {

                if (
                    annotationMode.equals(
                        "ARROW",
                        ignoreCase = true
                    ) &&
                    annotationStart != null
                ) {

                    val start =
                        annotationStart!!

                    addArrow(
                        start.first,
                        start.second,
                        row,
                        col
                    )

                    annotationStart =
                        null
                }

                invalidate()

                return true
            }
        }

        return true
    }

    // ============================================================
    // ADD / REMOVE CIRCLE
    // ============================================================

    private fun addCircle(
        row: Int,
        col: Int
    ) {

        val square =
            squareName(
                row,
                col
            )

        val existing =
            annotations.indexOfFirst {

                it.type.equals(
                    "CIRCLE",
                    ignoreCase = true
                ) &&
                        it.from.equals(
                            square,
                            ignoreCase = true
                        )
            }

        if (existing >= 0) {

            annotations.removeAt(
                existing
            )

        } else {

            annotations.add(
                BoardMark(
                    type = "CIRCLE",
                    color = annotationColor,
                    from = square
                )
            )
        }

        notifyAnnotationsChanged()

        invalidate()
    }

    // ============================================================
    // ADD ARROW
    // ============================================================

    private fun addArrow(
        fromRow: Int,
        fromCol: Int,
        toRow: Int,
        toCol: Int
    ) {

        val from =
            squareName(
                fromRow,
                fromCol
            )

        val to =
            squareName(
                toRow,
                toCol
            )

        if (from == to) {
            return
        }

        val existing =
            annotations.indexOfFirst {

                it.type.equals(
                    "ARROW",
                    ignoreCase = true
                ) &&

                        it.from.equals(
                            from,
                            ignoreCase = true
                        ) &&

                        it.to.equals(
                            to,
                            ignoreCase = true
                        )
            }

        if (existing >= 0) {

            annotations.removeAt(
                existing
            )

        } else {

            annotations.add(
                BoardMark(
                    type = "ARROW",
                    color = annotationColor,
                    from = from,
                    to = to
                )
            )
        }

        notifyAnnotationsChanged()

        invalidate()
    }

    // ============================================================
    // SQUARE NAME
    // ============================================================

    private fun squareName(
        row: Int,
        col: Int
    ): String {

        val file =
            ('a'.code + col)
                .toChar()

        val rank =
            8 - row

        return "$file$rank"
    }

    // ============================================================
    // SET ANNOTATIONS
    // ============================================================

    fun setAnnotations(
        marks: List<BoardMark>
    ) {

        annotations =
            marks.toMutableList()

        invalidate()
    }

    // ============================================================
    // CLEAR ANNOTATIONS
    // ============================================================

    fun clearAnnotations() {

        annotations.clear()

        annotationStart = null

        notifyAnnotationsChanged()

        invalidate()
    }

    // ============================================================
    // NOTIFY
    // ============================================================

    private fun notifyAnnotationsChanged() {

        onAnnotationsChanged?.invoke(
            annotations.toList()
        )
    }

    // ============================================================
    // TOUCH / CHESS MOVE
    // ============================================================

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        // ========================================================
        // ANNOTATION MODE
        // ========================================================

        if (
            annotationMode != "NONE"
        ) {

            return handleAnnotationTouch(
                event
            )
        }

        // ========================================================
        // NORMAL CHESS MOVE
        // ========================================================

        if (
            event.action !=
            MotionEvent.ACTION_DOWN
        ) {

            return true
        }

        if (!interactive) {
            return true
        }

        if (squareSize <= 0f) {
            return true
        }

        // Screen coordinate
        val screenCol =
            (event.x / squareSize)
                .toInt()
                .coerceIn(0, 7)

        val screenRow =
            (event.y / squareSize)
                .toInt()
                .coerceIn(0, 7)

        // Screen -> actual chess coordinate
        val col =
            boardCol(screenCol)

        val row =
            boardRow(screenRow)

        val sel =
            selected

        // ========================================================
        // NO PIECE SELECTED
        // ========================================================

        if (sel == null) {

            val piece =
                board.pieceAt(
                    row,
                    col
                )

            if (
                piece != null &&
                piece.color == board.turn
            ) {

                selected =
                    row to col

                legalDest =
                    board.legalMoves(
                        row,
                        col
                    )

                invalidate()
            }
        }

        // ========================================================
        // PIECE ALREADY SELECTED
        // ========================================================

        else {

            // ----------------------------------------------------
            // LEGAL DESTINATION
            // ----------------------------------------------------

            if (
                legalDest.any {

                    it.first == row &&
                            it.second == col
                }
            ) {

                val move =
                    board.movePiece(
                        sel.first,
                        sel.second,
                        row,
                        col
                    )

                selected = null

                legalDest =
                    emptyList()

                invalidate()

                if (move != null) {

                    onMoveListener?.invoke(
                        move
                    )
                }
            }

            // ----------------------------------------------------
            // ANOTHER OWN PIECE
            // ----------------------------------------------------

            else {

                val piece =
                    board.pieceAt(
                        row,
                        col
                    )

                if (
                    piece != null &&
                    piece.color == board.turn
                ) {

                    selected =
                        row to col

                    legalDest =
                        board.legalMoves(
                            row,
                            col
                        )
                }

                // ------------------------------------------------
                // EMPTY / ILLEGAL
                // ------------------------------------------------

                else {

                    selected = null

                    legalDest =
                        emptyList()
                }

                invalidate()
            }
        }

        return true
    }

    // ============================================================
    // REFRESH
    // ============================================================

    fun refresh() {

        invalidate()
    }
}