package com.ncom.myapplication2.app

import org.apache.http.protocol.BasicHttpContext
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import android.util.Log
import android.os.Handler
import android.os.Message
import android.os.Bundle

public class HTTP(
        val socketTimeout: Int = 5000,
        val requestTimeout: Int = 25000,
        val localContext : BasicHttpContext = BasicHttpContext()) {

    val MSG_DONE = 0

    val cbHandler = object : Handler() {
        override fun handleMessage(m: Message) {
            val r = m.obj as Param
            Log("RES: " + r.result)
            r.callback(r.result)
        }
    }

    fun Log(s: String) {
        Log.d("HTTP", s)
    }

    inner class Param(val callback: (String) -> Unit, val result: String)

    inner class TimeOutTask(val t: Thread, val url: String): TimerTask() {
        override fun run() {
            if (t.isAlive()) {
                Log("Interrupt")
                t.interrupt()
            }
        }
    }

    fun convertInputStreamToString(inputStream: InputStream) : String {
        val builder = StringBuilder()
        BufferedReader(InputStreamReader(inputStream)).forEachLine {
            builder.append(it)
        }
        return builder.toString()
    }

    fun Post(url: String, body: String, callback: (String) -> Unit) {
        Log("REQ: " + url)
        val r = Thread() {
            run() {
                val client = DefaultHttpClient()
                val param = client.getParams()

                HttpConnectionParams.setConnectionTimeout(param, socketTimeout)
                HttpConnectionParams.setSoTimeout(param, socketTimeout)

                val req = HttpPost(url)

                if (body.length() > 0) {
                    req.setEntity(StringEntity(body))
                    req.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                }

                val inputStream = client.execute(req, BasicHttpContext()).getEntity().getContent()

                val result = convertInputStreamToString(inputStream)

                cbHandler.obtainMessage(MSG_DONE, Param(callback, result)).sendToTarget()
            }
        }

        Timer(true).schedule(TimeOutTask(r, url), requestTimeout.toLong())
        r.start()
    }
}