package com.rambooster.pro

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var activityManager: ActivityManager
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())

    // Shared prefs that TestApp will also read
    companion object {
        const val PREFS_NAME     = "ram_booster_shared"
        const val KEY_BOOSTED_GB = "boosted_ram_gb"
        const val KEY_IS_BOOSTED = "is_boosted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE)

        setupUI()
        startAutoRefresh()
    }

    private fun setupUI() {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val physicalGB = (memInfo.totalMem / 1024 / 1024 / 1024).toInt()
        val physicalMB = (memInfo.totalMem / 1024 / 1024).toInt()
        val freeGB     = (memInfo.availMem / 1024 / 1024 / 1024f)

        val tvPhysical  = findViewById<TextView>(R.id.tv_physical_ram)
        val tvFree      = findViewById<TextView>(R.id.tv_free_ram)
        val tvTotal     = findViewById<TextView>(R.id.tv_total_ram)
        val tvBoosted   = findViewById<TextView>(R.id.tv_boosted_status)
        val seekbar     = findViewById<SeekBar>(R.id.seekbar_vram)
        val tvVramLabel = findViewById<TextView>(R.id.tv_vram_size)
        val btnBoost    = findViewById<Button>(R.id.btn_boost)
        val btnReset    = findViewById<Button>(R.id.btn_reset)
        val progressBar = findViewById<ProgressBar>(R.id.progress_ram)

        // Show current real RAM
        tvPhysical.text = "Physical RAM: ${physicalGB} GB (${physicalMB} MB)"
        tvFree.text     = "Free RAM: %.1f GB".format(freeGB)

        // Check if already boosted
        val isBoosted   = prefs.getBoolean(KEY_IS_BOOSTED, false)
        val boostedGB   = prefs.getInt(KEY_BOOSTED_GB, physicalGB)
        val reportedGB  = if (isBoosted) boostedGB else physicalGB

        tvTotal.text    = "Reported RAM: ${reportedGB} GB"
        progressBar.progress = ((physicalGB.toFloat() / reportedGB) * 100).toInt()

        if (isBoosted) {
            tvBoosted.text = "✅ RAM BOOSTED to ${boostedGB} GB"
            tvBoosted.setTextColor(getColor(android.R.color.holo_green_light))
        } else {
            tvBoosted.text = "❌ Not Boosted — ${physicalGB} GB only"
            tvBoosted.setTextColor(getColor(android.R.color.holo_red_light))
        }

        // Seekbar for virtual RAM (1–12 GB)
        seekbar.max = 12
        seekbar.progress = 10
        tvVramLabel.text = "Boost to: 10 GB"

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val target = maxOf(progress, physicalGB) // can't go below physical
                tvVramLabel.text = "Boost to: ${target} GB"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // BOOST BUTTON
        btnBoost.setOnClickListener {
            val targetGB = maxOf(seekbar.progress, physicalGB)

            btnBoost.isEnabled = false
            btnBoost.text = "Boosting..."

            handler.postDelayed({
                // Save boosted value — TestApp will read this
                prefs.edit()
                    .putBoolean(KEY_IS_BOOSTED, true)
                    .putInt(KEY_BOOSTED_GB, targetGB)
                    .apply()

                tvTotal.text  = "Reported RAM: ${targetGB} GB"
                tvBoosted.text = "✅ RAM BOOSTED to ${targetGB} GB"
                tvBoosted.setTextColor(getColor(android.R.color.holo_green_light))
                progressBar.progress = ((physicalGB.toFloat() / targetGB) * 100).toInt()

                btnBoost.text = "⚡ BOOST RAM"
                btnBoost.isEnabled = true

                Toast.makeText(
                    this,
                    "✅ RAM boosted to ${targetGB} GB! Now open the Test App.",
                    Toast.LENGTH_LONG
                ).show()

            }, 1500)
        }

        // RESET BUTTON
        btnReset.setOnClickListener {
            prefs.edit()
                .putBoolean(KEY_IS_BOOSTED, false)
                .putInt(KEY_BOOSTED_GB, physicalGB)
                .apply()

            tvTotal.text   = "Reported RAM: ${physicalGB} GB"
            tvBoosted.text = "❌ Not Boosted — ${physicalGB} GB only"
            tvBoosted.setTextColor(getColor(android.R.color.holo_red_light))
            progressBar.progress = 100

            Toast.makeText(this, "RAM reset to physical ${physicalGB} GB", Toast.LENGTH_SHORT).show()
        }
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            setupUI()
            handler.postDelayed(this, 3000)
        }
    }

    private fun startAutoRefresh() {
        handler.postDelayed(refreshRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }
}
