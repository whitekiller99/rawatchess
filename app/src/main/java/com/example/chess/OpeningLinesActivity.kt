package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess.adapter.OpeningLineAdapter
import com.example.chess.data.OpeningStore

class OpeningLinesActivity : AppCompatActivity() {

    private lateinit var openingId: String
    private lateinit var adapter: OpeningLineAdapter

    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_opening_lines
        )

        // ========================================================
        // OPENING ID
        // ========================================================

        openingId =
            intent.getStringExtra(
                "opening_id"
            )
                ?: run {

                    Toast.makeText(
                        this,
                        "Opening not found",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                    return
                }

        // ========================================================
        // RECYCLER VIEW
        // ========================================================

        val rv =
            findViewById<RecyclerView>(
                R.id.rvLines
            )

        rv.layoutManager =
            LinearLayoutManager(this)

        // ========================================================
        // ADAPTER
        // ========================================================

        adapter =
            OpeningLineAdapter(

                emptyList(),

                // =================================================
                // NORMAL CLICK = LEARN
                // =================================================

                onLearn = { position ->

                    val opening =
                        OpeningStore.getAll(this)
                            .find {
                                it.id == openingId
                            }
                            ?: return@OpeningLineAdapter

                    if (
                        position < 0 ||
                        position >= opening.lines.size
                    ) {
                        return@OpeningLineAdapter
                    }

                    startActivity(

                        Intent(
                            this,
                            PracticeActivity::class.java
                        ).apply {

                            putExtra(
                                "mode",
                                "learn"
                            )

                            putExtra(
                                "opening_id",
                                openingId
                            )

                            putExtra(
                                "line_index",
                                position
                            )
                        }
                    )
                },

                // =================================================
                // EDIT
                // =================================================

                onEdit = { position ->

                    val opening =
                        OpeningStore.getAll(this)
                            .find {
                                it.id == openingId
                            }
                            ?: return@OpeningLineAdapter

                    if (
                        position < 0 ||
                        position >= opening.lines.size
                    ) {
                        return@OpeningLineAdapter
                    }

                    val line =
                        opening.lines[position]

                    startActivity(

                        Intent(
                            this,
                            AddOpeningActivity::class.java
                        ).apply {

                            putExtra(
                                "edit_mode",
                                true
                            )

                            putExtra(
                                "opening_id",
                                openingId
                            )

                            putExtra(
                                "line_id",
                                line.id
                            )
                        }
                    )
                },

                // =================================================
                // DELETE
                // =================================================

                onDelete = { position ->

                    val opening =
                        OpeningStore.getAll(this)
                            .find {
                                it.id == openingId
                            }
                            ?: return@OpeningLineAdapter

                    if (
                        position < 0 ||
                        position >= opening.lines.size
                    ) {
                        return@OpeningLineAdapter
                    }

                    val line =
                        opening.lines[position]

                    AlertDialog.Builder(this)

                        .setTitle(
                            "Delete Line?"
                        )

                        .setMessage(
                            "Are you sure you want to delete this line?"
                        )

                        .setNegativeButton(
                            "Cancel",
                            null
                        )

                        .setPositiveButton(
                            "Delete"
                        ) { _, _ ->

                            OpeningStore.deleteLine(
                                this,
                                openingId,
                                line.id
                            )

                            loadLines()
                        }

                        .show()
                },

                // =================================================
                // RENAME
                // =================================================

                onRename = { position ->

                    showRenameLineDialog(
                        position
                    )
                }
            )

        rv.adapter =
            adapter

        // ========================================================
        // PRACTICE RANDOM LINE
        // ========================================================

        findViewById<Button>(
            R.id.btnRandomPractice
        ).setOnClickListener {

            startRandomLinePractice()
        }

        // ========================================================
        // LOAD
        // ========================================================

        loadLines()
    }

    // ============================================================
    // RANDOM LINE OF CURRENT OPENING
    // ============================================================

    private fun startRandomLinePractice() {

        val opening =
            OpeningStore.getAll(this)
                .find {
                    it.id == openingId
                }

        if (opening == null) {

            Toast.makeText(
                this,
                "Opening not found",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // --------------------------------------------------------
        // Sirf isi opening ki lines
        // --------------------------------------------------------

        val availableLines =
            opening.lines.filter {
                it.moves.isNotEmpty()
            }

        if (availableLines.isEmpty()) {

            Toast.makeText(
                this,
                "No lines available",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // --------------------------------------------------------
        // Random line
        // --------------------------------------------------------

        val randomLine =
            availableLines.random()

        val randomIndex =
            opening.lines.indexOfFirst {
                it.id == randomLine.id
            }

        if (randomIndex < 0) {
            return
        }

        // --------------------------------------------------------
        // Open PracticeActivity
        // --------------------------------------------------------

        startActivity(

            Intent(
                this,
                PracticeActivity::class.java
            ).apply {

                putExtra(
                    "mode",
                    "learn"
                )

                putExtra(
                    "opening_id",
                    openingId
                )

                putExtra(
                    "line_index",
                    randomIndex
                )
            }
        )
    }

    // ============================================================
    // RENAME LINE
    // ============================================================

    private fun showRenameLineDialog(
        position: Int
    ) {

        val opening =
            OpeningStore.getAll(this)
                .find {
                    it.id == openingId
                }
                ?: return

        if (
            position < 0 ||
            position >= opening.lines.size
        ) {
            return
        }

        val line =
            opening.lines[position]

        val input =
            EditText(this)

        input.setSingleLine(true)

        input.setText(
            line.name
        )

        input.setSelection(
            input.text.length
        )

        AlertDialog.Builder(this)

            .setTitle(
                "Rename Line"
            )

            .setView(
                input
            )

            .setNegativeButton(
                "Cancel",
                null
            )

            .setPositiveButton(
                "Save"
            ) { _, _ ->

                val newName =
                    input.text
                        .toString()
                        .trim()

                if (
                    newName.isEmpty()
                ) {

                    Toast.makeText(
                        this,
                        "Name cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                line.name =
                    newName

                OpeningStore.saveAll(
                    this,
                    OpeningStore.getAll(this)
                )

                loadLines()

                Toast.makeText(
                    this,
                    "Line renamed",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .show()
    }

    // ============================================================
    // ON RESUME
    // ============================================================

    override fun onResume() {

        super.onResume()

        if (
            ::adapter.isInitialized
        ) {

            loadLines()
        }
    }

    // ============================================================
    // LOAD LINES
    // ============================================================

    private fun loadLines() {

        val opening =
            OpeningStore.getAll(this)
                .find {
                    it.id == openingId
                }

        if (opening == null) {

            Toast.makeText(
                this,
                "Opening not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        findViewById<TextView>(
            R.id.tvOpeningTitle
        ).text =
            opening.name

        adapter.updateData(
            opening.lines.toList()
        )
    }
}