package com.ncom.myapplication2.app

import android.support.v7.app.ActionBarActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import android.widget.TextView


public class MainActivity : ActionBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        request()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    fun request() {
        val http = HTTP()
        http.Post("http://mnp.tele2.ru/gateway.php?9273193358", "",  { (res: String) ->
            val v = findViewById(R.id.text) as TextView
            v.setText(res)
        } )
    }

}
