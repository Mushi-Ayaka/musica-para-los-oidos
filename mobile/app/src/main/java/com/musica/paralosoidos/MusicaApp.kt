package com.musica.paralosoidos

import android.app.Application
import com.musica.paralosoidos.data.AppContainer

class MusicaApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
