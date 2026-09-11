package com.anton.voicetombola

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class FamilyActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var bridge: FamilyBridge
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(this, "Permesso microfono negato", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_family)
        webView = findViewById(R.id.webView)
        val adView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        // Necessario per abilitare confirm(), alert(), etc.
        webView.webChromeClient = WebChromeClient()

        bridge = FamilyBridge(this, webView)
        webView.addJavascriptInterface(bridge, "Android")
        webView.loadUrl("https://antonio194509.github.io/VoiceTombola/docs/")

        onBackPressedDispatcher.addCallback(this) {
            if (this@FamilyActivity::webView.isInitialized && this@FamilyActivity.webView.canGoBack()) {
                this@FamilyActivity.webView.goBack()
            } else {
                this@FamilyActivity.finish()
            }
        }
    }

    fun checkAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun startListening() {
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread

            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { Log.d("SPEECH", "Pronto") }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Log.e("SPEECH", "Errore: $error")
                    restartListening()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        webView.loadUrl("javascript:onSpeechResult('$text')")
                    }
                    restartListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
            }
            speechRecognizer?.startListening(intent)
        }
    }

    private fun restartListening() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                startListening()
            }
        }, 300)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::bridge.isInitialized) bridge.onDestroy()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
