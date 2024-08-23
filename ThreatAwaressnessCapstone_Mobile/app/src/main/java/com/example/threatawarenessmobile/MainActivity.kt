package com.example.threatawarenessmobile

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.awaitResponse

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var callingAPI: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStop : Button = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {
            clickSafeButton()
        }

        val btnActivate : Button = findViewById(R.id.btnActivate)
        btnActivate.setOnClickListener {
            Toast.makeText(this, "Start checking traffic", Toast.LENGTH_SHORT).show()
            callingAPI = clickActivateButton()
        }
    }

    private fun clickSafeButton(){
        val circle : ImageView = findViewById(R.id.imageView)
        val textView : TextView = findViewById(R.id.textView)
        circle.setImageResource(R.drawable.green_circle)
        textView.text = "App Stop"
        callingAPI?.cancel()
        vibratePhone(200)
    }

    private fun clickActivateButton(): Job{
        return GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity,"API working", Toast.LENGTH_SHORT).show()
            while (true) {
                val response = dataService.getWarning().awaitResponse()

                if (response.isSuccessful) {
                    val data = response.body()
                    val textView : TextView = findViewById(R.id.textView)
                    val circle : ImageView = findViewById(R.id.imageView)

                    if (data?.safe_to_cross == true) {
                        circle.setImageResource(R.drawable.green_circle)
                        textView.textSize = 20f
                        textView.text = "PROCEED WITH CAUTION"
                        delay(20) // Delay for 20 milliseconds if the return value is true
                    } else {
                        circle.setImageResource(R.drawable.red_circle)
                        textView.textSize = 40f
                        textView.text = "DANGER!!"
                        vibratePhone(500)
                        delay(5000) // Delay for 2 seconds if the return value is false
                    }
                } else {
                    // Handle unsuccessful response
                }
            }
        }
    }

    private fun handlerPost()
    {
        handler.post(object : Runnable{
            override fun run() {
                handler.postDelayed(this, fetchDataFromAPI())
            }
        })
    }
    private fun fetchDataFromAPI(): Long {
        var delayTime : Long = 20
        dataService.getWarning().enqueue(object : Callback<WarningResponse> {
            override fun onResponse(
                call: Call<WarningResponse>,
                response: retrofit2.Response<WarningResponse>
            ) {
                if (response.isSuccessful) {
                    val dataResponse = response.body()
                    // update UI
                    val textView : TextView = findViewById(R.id.textView)
                    val circle : ImageView = findViewById(R.id.imageView)
                    //val imageView: ImageView = findViewById(R.id.imageIntersection)

                    val message = dataResponse?.safe_to_cross
                    Log.d("MSG", message.toString())
                    if (message == true) {
                        circle.setImageResource(R.drawable.green_circle)
                        textView.textSize = 20f
                        textView.text = "PROCEED WITH CAUTION"
                    }
                    else {
                        circle.setImageResource(R.drawable.red_circle)
                        textView.text = "DANGER!!"
                        vibratePhone(500)
                        // change delay time. New API will be made after 1 second
                        delayTime = 2000
                    }
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<WarningResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity,"API not working", Toast.LENGTH_SHORT).show()
            }
        })

        return delayTime
    }

    private fun vibratePhone(time: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Deprecated in API 26
            vibrator.vibrate(time)
        }
    }
}