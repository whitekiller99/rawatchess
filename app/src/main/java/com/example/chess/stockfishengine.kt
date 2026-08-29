package com.example.chess

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.concurrent.Executors

class StockfishEngine(
    private val context: Context
) {

    private var process: Process? = null
    private var output: OutputStream? = null
    private var input: BufferedReader? = null

    private val executor =
        Executors.newSingleThreadExecutor()

    // ============================================================
    // START STOCKFISH
    // ============================================================

    fun start(): Boolean {

        return try {

            // ----------------------------------------------------
            // Use noBackupFilesDir for executable
            // ----------------------------------------------------

            val engineFile =
                context.noBackupFilesDir.resolve("stockfish")

            // ----------------------------------------------------
            // Always copy fresh engine
            // ----------------------------------------------------

            if (!engineFile.exists()) {

                context.assets.open(
                    "stockfish-android-armv8"
                ).use { assetInput ->

                    engineFile.outputStream().use { fileOutput ->

                        assetInput.copyTo(fileOutput)
                    }
                }
            }

            // ----------------------------------------------------
            // Make executable
            // ----------------------------------------------------

            if (!engineFile.setExecutable(true, false)) {

                throw RuntimeException(
                    "Cannot make Stockfish executable"
                )
            }

            // ----------------------------------------------------
            // Check executable permission
            // ----------------------------------------------------

            if (!engineFile.canExecute()) {

                throw RuntimeException(
                    "Stockfish is not executable"
                )
            }

            // ----------------------------------------------------
            // Start Stockfish
            // ----------------------------------------------------

            process =
                ProcessBuilder(
                    engineFile.absolutePath
                )
                    .redirectErrorStream(true)
                    .start()

            output =
                process!!.outputStream

            input =
                BufferedReader(
                    InputStreamReader(
                        process!!.inputStream
                    )
                )

            // ----------------------------------------------------
            // UCI initialization
            // ----------------------------------------------------

            sendCommand("uci")

            waitFor("uciok")

            sendCommand("isready")

            waitFor("readyok")

            true

        } catch (e: Exception) {

            e.printStackTrace()

            false
        }
    }

    // ============================================================
    // SEND COMMAND
    // ============================================================

    private fun sendCommand(
        command: String
    ) {

        output?.write(
            "$command\n".toByteArray()
        )

        output?.flush()
    }

    // ============================================================
    // WAIT FOR RESPONSE
    // ============================================================

    private fun waitFor(
        expected: String
    ) {

        while (true) {

            val line =
                input?.readLine()
                    ?: break

            if (line.contains(expected)) {
                break
            }
        }
    }

    // ============================================================
    // GET EVALUATION
    // ============================================================

    fun evaluate(
        fen: String,
        depth: Int = 15,
        callback: (String) -> Unit
    ) {

        executor.execute {

            try {

                sendCommand(
                    "position fen $fen"
                )

                sendCommand(
                    "go depth $depth"
                )

                var evaluation = "0.00"

                while (true) {

                    val line =
                        input?.readLine()
                            ?: break

                    // ------------------------------------------------
                    // SCORE
                    // ------------------------------------------------

                    if (
                        line.startsWith("info") &&
                        line.contains("score cp")
                    ) {

                        val parts =
                            line.split(" ")

                        val scoreIndex =
                            parts.indexOf("cp")

                        if (
                            scoreIndex >= 0 &&
                            scoreIndex + 1 < parts.size
                        ) {

                            val cp =
                                parts[
                                    scoreIndex + 1
                                ].toIntOrNull()

                            if (cp != null) {

                                evaluation =
                                    "%.2f".format(
                                        cp / 100.0
                                    )
                            }
                        }
                    }

                    // ------------------------------------------------
                    // SEARCH COMPLETE
                    // ------------------------------------------------

                    if (
                        line.startsWith("bestmove")
                    ) {

                        break
                    }
                }

                callback(evaluation)

            } catch (e: Exception) {

                e.printStackTrace()

                callback("0.00")
            }
        }
    }

    // ============================================================
    // STOP
    // ============================================================

    fun stop() {

        try {

            sendCommand("stop")

        } catch (_: Exception) {
        }
    }

    // ============================================================
    // CLOSE
    // ============================================================

    fun close() {

        try {

            sendCommand("quit")

        } catch (_: Exception) {
        }

        try {

            process?.destroy()

        } catch (_: Exception) {
        }

        executor.shutdown()
    }
}