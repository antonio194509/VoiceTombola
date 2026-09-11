package com.anton.voicetombola

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.json.JSONArray
import java.io.File

class ModSmorfiaActivity : AppCompatActivity() {

    private lateinit var ivSmorfia: ImageView
    private lateinit var etNumero: EditText
    private lateinit var etNome: EditText
    private lateinit var tvSignificato: TextView
    private lateinit var btnMicrofono: ImageView
    private lateinit var btnAscolta: ImageView
    private lateinit var btnSave: ImageView

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val stopRecordingRunnable = Runnable { if (isRecording) stopRecording() }

    // Launcher per il permesso del microfono
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startRecording()
        else Toast.makeText(this, "Permesso microfono negato", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modsmorfia)

        ivSmorfia = findViewById(R.id.ivSmorfia)
        etNumero = findViewById(R.id.inputNumero)
        etNome = findViewById(R.id.NomeSmorfia)
        tvSignificato = findViewById(R.id.tvSignificato)
        btnMicrofono = findViewById(R.id.btnMicrofono)
        btnAscolta = findViewById(R.id.btnAscolta)
        btnSave = findViewById(R.id.btnSave)

        val adView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        etNumero.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val num = s.toString()
                if (num.isNotEmpty()) loadSmorfiaData(num)
            }
        })

        btnMicrofono.setOnClickListener {
            if (isRecording) stopRecording() else checkAndRecord()
        }

        btnAscolta.setOnClickListener { playCurrentAudio() }
        btnSave.setOnClickListener { saveChanges() }
    }

    private fun loadSmorfiaData(numStr: String) {
        val n = numStr.toIntOrNull() ?: return
        if (n == 0) {
            finish()
            return
        }
        if (n !in 1..90) return

        try {
            val imgFileName = String.format("%02d.png", n)
            val inputStream = assets.open("smorfia/$imgFileName")
            val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, null)
            ivSmorfia.setImageDrawable(drawable)
            inputStream.close()
        } catch (e: Exception) {
            ivSmorfia.setImageResource(R.drawable.tombola_splash)
        }

        // Carica titolo personalizzato o quello di default dal JSON
        val prefs = getSharedPreferences("CustomSmorfia", Context.MODE_PRIVATE)
        val customTitle = prefs.getString("title_$n", null)

        if (customTitle != null) {
            tvSignificato.text = customTitle
            etNome.setText(customTitle)
        } else {
            try {
                val jsonStr = assets.open("smorfia.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getInt("number") == n) {
                        val title = obj.getString("title")
                        tvSignificato.text = title
                        etNome.setText(title)
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("ModSmorfia", "Errore caricamento JSON", e)
            }
        }
    }

    private fun checkAndRecord() {
        if (etNumero.text.isEmpty()) {
            Toast.makeText(this, "Inserisci un numero", Toast.LENGTH_SHORT).show()
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        val n = etNumero.text.toString().toIntOrNull() ?: return
        val fileName = String.format("%02d.mp3", n)
        val dir = File(filesDir, "audio")
        if (!dir.exists()) dir.mkdirs()
        val audioFile = File(dir, fileName)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                btnMicrofono.setImageResource(R.drawable.ic_stop)
                Toast.makeText(this@ModSmorfiaActivity, "Registrazione n. $n (max 4s)...", Toast.LENGTH_SHORT).show()
                
                // Avvia timer di 4 secondi
                handler.postDelayed(stopRecordingRunnable, 5000)
            } catch (e: Exception) {
                Log.e("ModSmorfia", "Errore MediaRecorder", e)
            }
        }
    }

    private fun stopRecording() {
        handler.removeCallbacks(stopRecordingRunnable)
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) { }
        mediaRecorder = null
        isRecording = false
        btnMicrofono.setImageResource(R.drawable.ic_microfono)
        Toast.makeText(this, "Audio salvato", Toast.LENGTH_SHORT).show()
    }

    private fun playCurrentAudio() {
        val n = etNumero.text.toString().toIntOrNull() ?: return
        val fileName = String.format("%02d.mp3", n)
        val customFile = File(File(filesDir, "audio"), fileName)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        try {
            if (customFile.exists()) {
                mediaPlayer?.setDataSource(customFile.absolutePath)
            } else {
                val descriptor = assets.openFd("audio/$fileName")
                mediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
            }
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Audio non disponibile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges() {
        val n = etNumero.text.toString().toIntOrNull() ?: return
        val newTitle = etNome.text.toString()
        if (newTitle.isEmpty()) return

        getSharedPreferences("CustomSmorfia", Context.MODE_PRIVATE)
            .edit().putString("title_$n", newTitle).apply()
        tvSignificato.text = newTitle
        Toast.makeText(this, "Significato aggiornato con successo!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        if (isRecording) {
            try { mediaRecorder?.stop() } catch (e: Exception) {}
        }
        mediaRecorder?.release()
    }
}
