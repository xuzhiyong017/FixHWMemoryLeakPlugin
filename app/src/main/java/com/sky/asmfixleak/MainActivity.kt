package com.sky.asmfixleak

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.engine.ImageEngine

open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun jumpToPage(view: View) {
        startActivity(Intent(this,MainActivity2::class.java))
        EasyPhotos.startPuzzleWithPhotos(this, arrayListOf(),"","",1,false,object:ImageEngine{
            override fun loadPhoto(p0: Context, p1: Uri, p2: ImageView) {
                TODO("Not yet implemented")
            }

            override fun loadGifAsBitmap(p0: Context, p1: Uri, p2: ImageView) {
                TODO("Not yet implemented")
            }

            override fun loadGif(p0: Context, p1: Uri, p2: ImageView) {
                TODO("Not yet implemented")
            }

            override fun getCacheBitmap(p0: Context, p1: Uri, p2: Int, p3: Int): Bitmap {
                TODO("Not yet implemented")
            }
        })
    }

}