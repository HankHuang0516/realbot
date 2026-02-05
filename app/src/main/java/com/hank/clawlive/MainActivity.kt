package com.hank.clawlive

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hank.clawlive.service.ClawWallpaperService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnSetWallpaper).setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this, ClawWallpaperService::class.java)
                )
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to open wallpaper picker", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
