/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    }

    // hostname
    private val host = "example.com"

    // username
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