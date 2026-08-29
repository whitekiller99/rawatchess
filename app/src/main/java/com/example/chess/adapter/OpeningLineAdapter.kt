package com.example.chess.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chess.R
import com.example.chess.data.OpeningLine

class OpeningLineAdapter(
    private var items: List<OpeningLine>,
    private val onLearn: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onRename: (Int) -> Unit
) : RecyclerView.Adapter<OpeningLineAdapter.VH>() {

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        val name: TextView =
            view.findViewById(R.id.tvLineName)

        val moves: TextView =
            view.findViewById(R.id.tvLineMoves)

        val learn: Button =
            view.findViewById(R.id.btnLearnLine)

        val edit: Button =
            view.findViewById(R.id.btnEditLine)

        val delete: Button =
            view.findViewById(R.id.btnDeleteLine)
    }

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_opening_line,
                    parent,
                    false
                )

        return VH(view)
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    override fun getItemCount(): Int {

        return items.size
    }

    // ============================================================
    // BIND VIEW
    // ============================================================

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {

        val line =
            items[position]

        // ========================================================
        // LINE NUMBER
        // ========================================================

        holder.name.text =
            "%02d".format(position + 1)

        // ========================================================
        // MOVE COUNT
        // ========================================================

        holder.moves.text =
            "${line.moves.size} moves"

        // ========================================================
        // HIDE OLD BUTTONS
        // ========================================================

        holder.learn.visibility =
            View.GONE

        holder.edit.visibility =
            View.GONE

        holder.delete.visibility =
            View.GONE

        // ========================================================
        // NORMAL CLICK = LEARN
        // ========================================================

        holder.itemView.setOnClickListener {

            val currentPosition =
                holder.bindingAdapterPosition

            if (
                currentPosition !=
                RecyclerView.NO_POSITION
            ) {

                onLearn(
                    currentPosition
                )
            }
        }

        // ========================================================
        // LONG PRESS
        // ========================================================

        holder.itemView.setOnLongClickListener {

            val currentPosition =
                holder.bindingAdapterPosition

            if (
                currentPosition !=
                RecyclerView.NO_POSITION
            ) {

                showLineMenu(
                    holder.itemView,
                    currentPosition
                )
            }

            true
        }
    }

    // ============================================================
    // LONG PRESS MENU
    // ============================================================

    private fun showLineMenu(
        view: View,
        position: Int
    ) {

        if (
            position < 0 ||
            position >= items.size
        ) {
            return
        }

        val line =
            items[position]

        val displayName =
            "%02d".format(
                position + 1
            )

        // ========================================================
        // MENU OPTIONS
        // ========================================================

        val options =
            arrayOf(
                "Rename Line",
                "Edit Line",
                "Delete Line",
                "Cancel"
            )

        AlertDialog.Builder(
            view.context
        )
            .setTitle(
                "Line $displayName"
            )
            .setItems(
                options
            ) { dialog, which ->

                when (which) {

                    // =================================================
                    // RENAME
                    // =================================================

                    0 -> {

                        onRename(
                            position
                        )
                    }

                    // =================================================
                    // EDIT
                    // =================================================

                    1 -> {

                        onEdit(
                            position
                        )
                    }

                    // =================================================
                    // DELETE
                    // =================================================

                    2 -> {

                        showDeleteConfirmation(
                            view,
                            position
                        )
                    }

                    // =================================================
                    // CANCEL
                    // =================================================

                    3 -> {

                        dialog.dismiss()
                    }
                }
            }
            .show()
    }

    // ============================================================
    // DELETE CONFIRMATION
    // ============================================================

    private fun showDeleteConfirmation(
        view: View,
        position: Int
    ) {

        if (
            position < 0 ||
            position >= items.size
        ) {
            return
        }

        val line =
            items[position]

        val displayName =
            "%02d".format(
                position + 1
            )

        AlertDialog.Builder(
            view.context
        )
            .setTitle(
                "Delete Line?"
            )
            .setMessage(
                "Are you sure you want to delete " +
                        "\"$displayName\"?\n\n" +
                        "This line will be permanently deleted."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                onDelete(
                    position
                )
            }
            .show()
    }

    // ============================================================
    // UPDATE DATA
    // ============================================================

    fun updateData(
        newItems: List<OpeningLine>
    ) {

        items =
            newItems

        notifyDataSetChanged()
    }
}