/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.account_input.*
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

        val constraintLayout = layoutInflater.inflate(R.layout.account_input, null)

        AlertDialog.Builder(this)
            // ダイアログタイトルをセット
            .setTitle(getString(R.string.account_add_dialog))
            // 入力欄のレイアウトをセット
            .setView(constraintLayout)
            // ダイアログ外タッチで閉じないようにする
            .setCancelable(false)
            // 「追加」ボタン
            .setPositiveButton(getString(R.string.account_add_ok)) { _: DialogInterface, _: Int ->
                // 入力された項目を取得
                Log.d("Dialog", "OK")
                val addr: TextInputEditText = constraintLayout.findViewById(R.id.address)
                val password: TextInputEditText = constraintLayout.findViewById(R.id.password)
                Log.d("Dialog", addr.text.toString())
                Log.d("Dialog", password.text.toString())
                host = addr.text.toString().split('@')[1]
                user = addr.text.toString().split('@')[0]
                pass = password.text.toString()
            // 「後で」ボタン
            }.setNegativeButton(getString(R.string.account_add_cancel)) { _: DialogInterface, _: Int ->
                Log.d("Dialog", "Cancelled")
            }
            .show()
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