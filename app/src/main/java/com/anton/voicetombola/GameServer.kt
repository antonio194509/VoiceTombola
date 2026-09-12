package com.anton.voicetombola

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class GameServer(private val port: Int = 8889) {

    private var serverSocket: ServerSocket? = null
    private var running = false
    private val occupiedCards = mutableSetOf<Int>()
    private val playerSessions = mutableMapOf<String, Int>() // clientId -> playerNumber (1 to 6)
    private val cardOwners = mutableMapOf<Int, String>() // cardId -> clientId

    fun start() {
        if (running) return
        running = true
        thread {
            try {
                serverSocket = ServerSocket(port)
                Log.d("GameServer", "Server started on port $port")
                while (running) {
                    val client = serverSocket?.accept() ?: break
                    handleClient(client)
                }
            } catch (e: Exception) {
                Log.e("GameServer", "Server error", e)
            }
        }
    }

    fun stop() {
        running = false
        serverSocket?.close()
        serverSocket = null
        occupiedCards.clear()
        playerSessions.clear()
        cardOwners.clear()
    }

    private fun handleClient(socket: Socket) {
        thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)
                val line = reader.readLine() ?: return@thread

                val response = processRequest(line)
                writer.println(response)

                socket.close()
            } catch (e: Exception) {
                Log.e("GameServer", "Client error", e)
            }
        }
    }

    private fun processRequest(request: String): String {
        return try {
            val json = JSONObject(request)
            val action = json.getString("action")
            val clientId = json.optString("clientId", "")

            // Check player limit (max 6 online players, numbered 1 to 6 in order of entry)
            if (clientId.isNotEmpty()) {
                if (!playerSessions.containsKey(clientId)) {
                    if (playerSessions.size >= 6) {
                        return JSONObject()
                            .put("status", "error")
                            .put("message", "Maximum 6 online players allowed. The 7th player cannot play online.")
                            .toString()
                    }

                    val usedNumbers = playerSessions.values.toSet()
                    var nextPlayerNum = 1
                    while (nextPlayerNum in usedNumbers) {
                        nextPlayerNum++
                    }
                    playerSessions[clientId] = nextPlayerNum
                    Log.d("GameServer", "New player connected: $clientId -> Player #$nextPlayerNum (Total: ${playerSessions.size})")
                }
            }

            when (action) {
                "getOccupied" -> {
                    val arr = JSONArray(occupiedCards)
                    val responseJson = JSONObject()
                        .put("status", "ok")
                        .put("cards", arr)
                        .put("totalPlayers", playerSessions.size)
                    if (clientId.isNotEmpty() && playerSessions.containsKey(clientId)) {
                        responseJson.put("playerNum", playerSessions[clientId]!!)
                    }
                    responseJson.toString()
                }
                "claimCard" -> {
                    val cardId = json.getInt("cardId")
                    if (cardId < 1 || cardId > 72) {
                        return JSONObject().put("status", "error").put("message", "Invalid card ID").toString()
                    }

                    val currentOwner = cardOwners[cardId]
                    if (currentOwner != null && currentOwner != clientId) {
                        return JSONObject()
                            .put("status", "error")
                            .put("message", "Card already taken")
                            .put("cardId", cardId)
                            .toString()
                    }

                    if (currentOwner == clientId || occupiedCards.add(cardId)) {
                        if (clientId.isNotEmpty()) {
                            cardOwners[cardId] = clientId
                        }
                        JSONObject()
                            .put("status", "ok")
                            .put("cardId", cardId)
                            .put("cards", JSONArray(occupiedCards))
                            .toString()
                    } else {
                        JSONObject()
                            .put("status", "error")
                            .put("message", "Card already taken")
                            .put("cardId", cardId)
                            .toString()
                    }
                }
                "releaseCard" -> {
                    val cardId = json.getInt("cardId")
                    if (clientId.isNotEmpty()) {
                        val owner = cardOwners[cardId]
                        if (owner != null && owner != clientId) {
                            return JSONObject()
                                .put("status", "error")
                                .put("message", "Card is owned by another player")
                                .put("cardId", cardId)
                                .toString()
                        }
                    }
                    occupiedCards.remove(cardId)
                    cardOwners.remove(cardId)
                    JSONObject()
                        .put("status", "ok")
                        .put("cards", JSONArray(occupiedCards))
                        .toString()
                }
                "resetGame" -> {
                    occupiedCards.clear()
                    playerSessions.clear()
                    cardOwners.clear()
                    JSONObject()
                        .put("status", "ok")
                        .put("cards", JSONArray(occupiedCards))
                        .toString()
                }
                else -> JSONObject().put("status", "error").put("message", "Unknown action").toString()
            }
        } catch (e: Exception) {
            JSONObject().put("status", "error").put("message", e.message ?: "Unknown error").toString()
        }
    }
}
