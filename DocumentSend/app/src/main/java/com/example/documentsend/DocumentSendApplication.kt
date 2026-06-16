package com.example.documentsend

import android.app.Application
import com.example.documentsend.log.Logger

class DocumentSendApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
    }
}
