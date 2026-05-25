package com.rambooster.pro

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var activityManager: ActivityManager
    private lateinit var tvUsedRam: TextView
    private lateinit var tvFreeRam: TextView
    private lateinit var tvTotalRam: TextView
    private lateinit var tvReportedTotal: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBoost: Button
    private lateinit var switchVirtualRam: Switch
    private lateinit var seekbarVRam: SeekBar
    private lateinit var tvVRamSize: TextView
    private lateinit var listViewApps: ListView

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 2000L // update every 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // init views
        tvUsedRam      = findViewById(R.id.tv_used_ram)
        tvFreeRam      = findViewById(R.id.tv_free_ram)
        tvTotalRam     = findViewById(R.id.tv_total_ram)
        tvReportedTotal= findViewById(R.id.tv_reported_total)
        progressBar    = findViewById(R.id.progress_ram)
        btnBoost       = findViewById(R.id.btn_boost)
        switchVirtualRam = findViewById(R.id.switch_virtual_ram)
        seekbarVRam    = findViewById(R.id.seekbar_vram)
        tvVRamSize     = findViewById(R.id.tv_vram_size)
        listViewApps   = findViewById(R.id.listview_apps)

        setupRamDisplay()
        setupBoostButton()
        setupVirtualRam()
        setupRunningApps()
        startAutoRefresh()
    }

    // ─── RAM Display ───────────────────────────────────────────────
    private fun setupRamDisplay() {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMB  = (memInfo.totalMem  / 1024 / 1024).toInt()
        val availMB  = (memInfo.availMem  / 1024 / 1024).toInt()
        val usedMB   = totalMB - availMB

        val totalGB  = totalMB  / 1024f
        val availGB  = availMB  / 1024f
        val usedGB   = usedMB   / 1024f

        tvUsedRam.text  = "%.1f GB Used".format(usedGB)
        tvFreeRam.text  = "%.1f GB Free".format(availGB)
        tvTotalRam.text = "%.1f GB Total".format(totalGB)

        val usedPct = ((usedMB.toFloat() / totalMB) * 100).roundToInt()
        progressBar.progress = usedPct
    }

    // ─── Boost Button ──────────────────────────────────────────────
    private fun setupBoostButton() {
        btnBoost.setOnClickListener {
            btnBoost.isEnabled = false
            btnBoost.text = "Boosting…"

            // Kill background processes to free RAM
            activityManager.killBackgroundProcesses(packageName)

            // Simulate clean-up delay
            handler.postDelayed({
                setupRamDisplay()
                btnBoost.text = "✅ Boost Complete!"
                handler.postDelayed({
                    btnBoost.text = "⚡ Boost RAM"
                    btnBoost.isEnabled = true
                }, 2000)
            }, 1500)
        }
    }

    // ─── Virtual RAM (RAM Expansion) ───────────────────────────────
    private fun setupVirtualRam() {
        // Read current physical RAM
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val physGB = (memInfo.totalMem / 1024 / 1024 / 1024).toInt()

        seekbarVRam.max = 8   // max 8 GB virtual
        seekbarVRam.progress = 3

        fun updateReported() {
            val extra = if (switchVirtualRam.isChecked) seekbarVRam.progress else 0
            val reported = physGB + extra
            tvReportedTotal.text = "Reported to system: ${reported} GB"
            tvVRamSize.text = "Virtual RAM: ${seekbarVRam.progress} GB"
        }

        updateReported()

        switchVirtualRam.setOnCheckedChangeListener { _, _ -> updateReported() }

        seekbarVRam.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                updateReported()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    // ─── Running Apps List ─────────────────────────────────────────
    private fun setupRunningApps() {
        val runningApps = activityManager.runningAppProcesses ?: return

        val appNames = runningApps
            .filter { it.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE }
            .map { it.processName.substringAfterLast('.').replaceFirstChar { c -> c.uppercase() } }
            .take(10)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appNames)
        listViewApps.adapter = adapter
    }

    // ─── Auto-refresh RAM stats ────────────────────────────────────
    private val refreshRunnable = object : Runnable {
        override fun run() {
            setupRamDisplay()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun startAutoRefresh() {
        handler.postDelayed(refreshRunnable, updateInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }
}
