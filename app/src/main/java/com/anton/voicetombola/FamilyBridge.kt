package com.anton.voicetombola

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.File
import java.net.Socket
import java.util.Locale
import kotlin.concurrent.thread

class FamilyBridge(private val context: Context, private val webView: WebView) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var mediaPlayer: MediaPlayer? = null
    private val nsdHelper = NsdHelper(context)

    companion object {
        private var activeServer: GameServer? = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.ITALIAN
        } else {
            Log.e("FamilyBridge", "Inizializzazione TTS fallita")
        }
    }

    @JavascriptInterface
    fun checkLocalCaller(codeOrIp: String) {
        // Se l'utente ha inserito un indirizzo IP diretto (es. 192.168...)
        if (codeOrIp.contains(".")) {
            thread {
                var success = false
                try {
                    val socket = Socket()
                    socket.connect(java.net.InetSocketAddress(codeOrIp, 8889), 1500)
                    socket.close()
                    success = true
                } catch (e: Exception) {
                    success = false
                }
                webView.post {
                    val ipArg = if (success) codeOrIp else ""
                    webView.loadUrl("javascript:onLocalCallerChecked($success, '$ipArg')")
                }
            }
            return
        }

        // Altrimenti prova NSD
        nsdHelper.isCallerActive(codeOrIp) { hostIp ->
            webView.post {
                val isActive = hostIp != null
                val ipArg = hostIp ?: ""
                webView.loadUrl("javascript:onLocalCallerChecked($isActive, '$ipArg')")
            }
        }
    }

    @JavascriptInterface
    fun claimLocalCaller(code: String) {
        try {
            if (activeServer == null) {
                activeServer = GameServer(8889).apply { start() }
            }
            nsdHelper.registerService(code)
            Log.d("FamilyBridge", "Caller claimed for code: $code")
        } catch (e: Exception) {
            Log.e("FamilyBridge", "Error claiming local caller", e)
        }
    }

    @JavascriptInterface
    fun releaseLocalCaller() {
        try {
            activeServer?.stop()
            activeServer = null
            nsdHelper.unregisterService()
            Log.d("FamilyBridge", "Local caller released")
        } catch (e: Exception) {
            Log.e("FamilyBridge", "Error releasing local caller", e)
        }
    }

    @JavascriptInterface
    fun sendGameRequest(hostIp: String, requestJson: String) {
        thread {
            try {
                val socket = Socket(hostIp, 8889)
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                writer.println(requestJson)
                val response = reader.readLine() ?: "{}"
                socket.close()

                webView.post {
                    val escaped = response.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "")
                    webView.loadUrl("javascript:onGameResponse('$escaped')")
                }
            } catch (e: Exception) {
                Log.e("FamilyBridge", "Error sending game request to $hostIp", e)
                webView.post {
                    val errJson = JSONObject().put("status", "error").put("message", e.message ?: "Connection error").toString()
                    webView.loadUrl("javascript:onGameResponse('$errJson')")
                }
            }
        }
    }

    @JavascriptInterface
    fun speak(text: String, lang: String) {
        val locale = if (lang == "en-US") Locale.US else Locale.ITALIAN
        tts.language = locale
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    @JavascriptInterface
    fun playAudio(fileName: String) {
        try {
            stopAudio()
            
            // fileName arriva come "audio/01.mp3"
            val pureName = fileName.substringAfterLast("/")
            val customFile = File(File(context.filesDir, "audio"), pureName)
            
            mediaPlayer = MediaPlayer()
            if (customFile.exists()) {
                mediaPlayer?.setDataSource(customFile.absolutePath)
            } else {
                val descriptor = context.assets.openFd(fileName)
                mediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
            }
            
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("FamilyBridge", "Errore riproduzione audio: $fileName", e)
        }
    }

    @JavascriptInterface
    fun getCustomTitle(n: Int): String? {
        val prefs = context.getSharedPreferences("CustomSmorfia", Context.MODE_PRIVATE)
        return prefs.getString("title_$n", null)
    }

    @JavascriptInterface
    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("FamilyBridge", "Errore stopAudio", e)
        }
    }

    @JavascriptInterface
    fun startListening() {
        (context as? FamilyActivity)?.checkAndStartListening()
    }

    @JavascriptInterface
    fun openModSmorfia() {
        val intent = Intent(context, ModSmorfiaActivity::class.java)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun goBack() {
        webView.post {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    @JavascriptInterface
    fun restoreSmorfiaFromBackup() {
        try {
            // 1. Cancella i file audio personalizzati per tornare agli originali (assets)
            val audioDir = File(context.filesDir, "audio")
            if (audioDir.exists()) {
                audioDir.deleteRecursively()
            }

            // 2. Cancella i significati personalizzati
            val prefs = context.getSharedPreferences("CustomSmorfia", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 3. Feedback e ricarica pagina
            (context as? android.app.Activity)?.runOnUiThread {
                android.widget.Toast.makeText(context, "Smorfia Originale Ripristinata!", android.widget.Toast.LENGTH_SHORT).show()
                webView.reload()
            }
        } catch (e: Exception) {
            Log.e("FamilyBridge", "Errore nel ripristino: ${e.message}")
        }
    }

    fun onDestroy() {
        tts.stop()
        tts.shutdown()
        stopAudio()
    }
}
