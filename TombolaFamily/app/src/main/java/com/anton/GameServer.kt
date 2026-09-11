package com.anton.tombolafamily

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
            
            when (action) {
                "getOccupied" -> {
                    val arr = JSONArray(occupiedCards)
                    JSONObject().put("status", "ok").put("cards", arr).toString()
                }
                "claimCard" -> {
                    val cardId = json.getInt("cardId")
                    if (occupiedCards.contains(cardId)) {
                        JSONObject().put("status", "error").put("message", "Card already taken").put("cardId", cardId).toString()
                    } else {
                        occupiedCards.add(cardId)
                        JSONObject().put("status", "ok").put("cardId", cardId).toString()
                    }
                }
                "releaseCard" -> {
                    val cardId = json.getInt("cardId")
                    occupiedCards.remove(cardId)
                    JSONObject().put("status", "ok").toString()
                }
                else -> JSONObject().put("status", "error").put("message", "Unknown action").toString()
            }
        } catch (e: Exception) {
            JSONObject().put("status", "error").put("message", e.message).toString()
        }
    }
}
