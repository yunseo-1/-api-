package com.example.money

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.money.R
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    private val currencyList = arrayOf("KRW", "USD", "EUR", "CAD")
    private lateinit var etFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var btnExchange: Button
    private val fromTo = arrayOfNulls<String>(2)
    private var currencyRate = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinner2 = findViewById<Spinner>(R.id.spinner2)

        etFrom = findViewById(R.id.et_from)
        tvTo = findViewById(R.id.tv_to)
        btnExchange = findViewById(R.id.btn_exchange)

        val adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, currencyList)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                fromTo[0] = currencyList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinner2.adapter = adapter
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                fromTo[1] = currencyList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnExchange.setOnClickListener {
            try {
                currencyRate = Task().execute(*fromTo).get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            val input = etFrom.text.toString().toDouble()
            val result = Math.round(input * currencyRate * 100.0) / 100.0

            tvTo.text = result.toString()
        }
    }

    private inner class Task : AsyncTask<String, Void, Double>() {
        override fun doInBackground(vararg params: String): Double {
            val fromCurrency = params[0]
            val toCurrency = params[1]
            val apiKey = "YOUR_API_KEY"
            val urlString = "https://api.exchangeratesapi.io/latest?base=$fromCurrency&symbols=$toCurrency"

            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                try {
                    val reader = InputStreamReader(urlConnection.inputStream)
                    val data = reader.readText()
                    val jsonObject = JSONObject(data)
                    val rate = jsonObject.getJSONObject("rates").getDouble(toCurrency)
                    return rate
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return 0.0
            }
        }
    }
}
