package in3a_2180289.intentwork07

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val host = "example.com"

    // mail address
    private val user = "hogehoge"

    // IMAPS port
    private val port = 993

    // password
    private val pass = "xxxxxx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val mail = Mail(host, user, port, pass)
            val thread = mail.thread
            thread.start()
        }
    }
}