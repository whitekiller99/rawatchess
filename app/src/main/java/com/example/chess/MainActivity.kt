package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chess.adapter.OpeningAdapter
import com.example.chess.data.BuiltInOpeningStore
import com.example.chess.data.OpeningStore

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: OpeningAdapter


    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_main
        )


        // ========================================================
        // IMPORT BUILT-IN OPENINGS
        // ========================================================

        BuiltInOpeningStore.importIfNeeded(
            this
        )


        // ========================================================
        // WINDOW INSETS
        // ========================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }


        // ========================================================
        // RECYCLER VIEW
        // ========================================================

        val rv =
            findViewById<RecyclerView>(
                R.id.rvOpenings
            )

        rv.layoutManager =
            LinearLayoutManager(this)


        // ========================================================
        // OPENING ADAPTER
        // ========================================================

        adapter =
            OpeningAdapter(

                emptyList(),

                // ------------------------------------------------
                // OPEN OPENING
                // ------------------------------------------------

                onClick = { opening ->

                    val intent =
                        Intent(
                            this,
                            OpeningLinesActivity::class.java
                        )

                    intent.putExtra(
                        "opening_id",
                        opening.id
                    )

                    startActivity(
                        intent
                    )
                },


                // ------------------------------------------------
                // DELETE OPENING
                // ------------------------------------------------

                onDelete = { opening ->

                    OpeningStore.delete(
                        this,
                        opening.id
                    )

                    loadData()

                    Toast.makeText(
                        this,
                        "Opening deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                },


                // ------------------------------------------------
                // RENAME OPENING
                // ------------------------------------------------

                onRename = { opening, newName ->

                    val list =
                        OpeningStore.getAll(
                            this
                        )


                    val foundOpening =
                        list.find {
                            it.id == opening.id
                        }


                    if (
                        foundOpening == null
                    ) {

                        Toast.makeText(
                            this,
                            "Opening not found",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@OpeningAdapter
                    }


                    // ------------------------------------------------
                    // CHANGE NAME
                    // ------------------------------------------------

                    foundOpening.name =
                        newName


                    // ------------------------------------------------
                    // SAVE
                    // ------------------------------------------------

                    OpeningStore.saveAll(
                        this,
                        list
                    )


                    // ------------------------------------------------
                    // REFRESH
                    // ------------------------------------------------

                    loadData()


                    Toast.makeText(
                        this,
                        "Opening renamed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )


        // ========================================================
        // SET ADAPTER
        // ========================================================

        rv.adapter =
            adapter


        // ========================================================
        // CREATE OPENING
        // ========================================================

        findViewById<Button>(
            R.id.btnAdd
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AddOpeningActivity::class.java
                )
            )
        }


        // ========================================================
        // LOAD DATA
        // ========================================================

        loadData()
    }


    // ============================================================
    // ON RESUME
    // ============================================================

    override fun onResume() {

        super.onResume()

        if (
            ::adapter.isInitialized
        ) {

            loadData()
        }
    }


    // ============================================================
    // LOAD OPENINGS
    // ============================================================

    private fun loadData() {

        adapter.updateData(
            OpeningStore.getAll(
                this
            )
        )
    }
}