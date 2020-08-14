/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
        lateinit var mRecyclerView: RecyclerView

        // 別スレッド等から処理する用
        val handler = Handler()
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

        // view取得
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mRecyclerView = findViewById(R.id.recyclerview)
        mRecyclerView.setHasFixedSize(true)
        // 更新イベントリスナー追加
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        // メールクラス生成
        val mail = Mail(host, user, port, pass)
        coroutineScope.launch {
            // 受信処理
            mail.receive()
        }
    }
}