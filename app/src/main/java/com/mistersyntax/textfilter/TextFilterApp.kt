package com.mistersyntax.textfilter

import android.app.Application
import com.mistersyntax.textfilter.db.SpamDatabase

class TextFilterApp : Application() {
    val database by lazy { SpamDatabase.get(this) }
}
