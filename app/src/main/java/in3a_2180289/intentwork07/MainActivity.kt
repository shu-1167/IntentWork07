/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.content.Intent
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

    private val requestCode = 1

    // hostname
    private var host = String()

    // username
    private var user = String()

    // IMAPS port
    private var port = 993

    // password
    private var pass = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // view取得
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mRecyclerView = findViewById(R.id.recyclerview)
        mRecyclerView.setHasFixedSize(true)
        // 更新イベントリスナー追加
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)

        // アカウント追加アクティビティ
        val intent = Intent(this, AccountAddActivity::class.java)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == this.requestCode && data != null) {
            // メールアドレス、パスワードを取得
            val addr = data.getStringExtra("address")!!
            host = addr.split('@')[1]
            user = addr.split('@')[0]
            pass = data.getStringExtra("password")!!
        }
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