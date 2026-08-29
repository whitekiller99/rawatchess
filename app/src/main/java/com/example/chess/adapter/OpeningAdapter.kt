package com.example.chess.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chess.R
import com.example.chess.data.Opening

class OpeningAdapter(
    private var items: List<Opening>,
    private val onClick: (Opening) -> Unit,
    private val onDelete: (Opening) -> Unit,
    private val onRename: (Opening, String) -> Unit
) : RecyclerView.Adapter<OpeningAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        val name: TextView =
            view.findViewById(R.id.tvName)

        val moves: TextView =
            view.findViewById(R.id.tvMoves)

        val delete: TextView =
            view.findViewById(R.id.btnDelete)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_opening,
                    parent,
                    false
                )

        return VH(view)
    }


    override fun getItemCount(): Int {
        return items.size
    }


    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {

        val opening = items[position]

        holder.name.text =
            opening.name

        holder.moves.text =
            "${opening.lines.size} lines"

        // Delete button hide
        holder.delete.visibility =
            View.GONE


        // ========================================================
        // NORMAL CLICK
        // ========================================================

        holder.itemView.setOnClickListener {

            onClick(opening)
        }


        // ========================================================
        // LONG PRESS
        // ========================================================

        holder.itemView.setOnLongClickListener {

            showOpeningMenu(
                holder.itemView,
                opening
            )

            true
        }
    }


    // ============================================================
    // OPENING MENU
    // ============================================================

    private fun showOpeningMenu(
        view: View,
        opening: Opening
    ) {

        val options =
            arrayOf(
                "Rename Opening",
                "Delete Opening",
                "Cancel"
            )


        AlertDialog.Builder(
            view.context
        )
            .setTitle(
                opening.name
            )
            .setItems(
                options
            ) { dialog, which ->

                when (which) {

                    // =================================================
                    // RENAME
                    // =================================================

                    0 -> {

                        showRenameDialog(
                            view,
                            opening
                        )
                    }


                    // =================================================
                    // DELETE
                    // =================================================

                    1 -> {

                        showDeleteConfirmation(
                            view,
                            opening
                        )
                    }


                    // =================================================
                    // CANCEL
                    // =================================================

                    2 -> {

                        dialog.dismiss()
                    }
                }
            }
            .show()
    }


    // ============================================================
    // RENAME DIALOG
    // ============================================================

    private fun showRenameDialog(
        view: View,
        opening: Opening
    ) {

        val editText =
            EditText(view.context)

        editText.setText(
            opening.name
        )

        editText.setSelection(
            editText.text.length
        )

        editText.hint =
            "Opening name"


        AlertDialog.Builder(
            view.context
        )
            .setTitle(
                "Rename Opening"
            )
            .setView(
                editText
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Rename"
            ) { _, _ ->

                val newName =
                    editText.text
                        .toString()
                        .trim()

                if (
                    newName.isNotEmpty()
                ) {

                    onRename(
                        opening,
                        newName
                    )
                }
            }
            .show()
    }


    // ============================================================
    // DELETE CONFIRMATION
    // ============================================================

    private fun showDeleteConfirmation(
        view: View,
        opening: Opening
    ) {

        AlertDialog.Builder(
            view.context
        )
            .setTitle(
                "Delete Opening?"
            )
            .setMessage(
                "Are you sure you want to delete " +
                        "\"${opening.name}\"?\n\n" +
                        "All lines inside this opening " +
                        "will also be deleted."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                onDelete(opening)
            }
            .show()
    }


    // ============================================================
    // UPDATE DATA
    // ============================================================

    fun updateData(
        newItems: List<Opening>
    ) {

        items =
            newItems

        notifyDataSetChanged()
    }
}