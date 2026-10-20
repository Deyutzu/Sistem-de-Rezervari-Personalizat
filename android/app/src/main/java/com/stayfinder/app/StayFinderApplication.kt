package com.stayfinder.app

import android.app.Application
import com.stayfinder.app.data.AppContainer

/**
 * Clasa Application traieste cat tot procesul aplicatiei (mai mult decat orice ecran).
 * De aceea e locul potrivit pentru AppContainer: repository-urile supravietuiesc
 * rotirii ecranului si navigarii intre ecrane.
 * Este inregistrata in AndroidManifest.xml (android:name=".StayFinderApplication").
 */
class StayFinderApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
