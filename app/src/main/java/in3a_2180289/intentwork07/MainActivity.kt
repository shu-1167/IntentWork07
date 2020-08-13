package in3a_2180289.intentwork07

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    }

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

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val mail = Mail(host, user, port, pass)
        val thread = mail.thread
        thread.start()
    }
}