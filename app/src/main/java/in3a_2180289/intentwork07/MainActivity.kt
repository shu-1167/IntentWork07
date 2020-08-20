/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
        lateinit var mRecyclerView: RecyclerView

        // 別スレッド等から処理する用
        val handler = Handler()
    }

    private val requestCode = 1
    private var host = String()
    private var user = String()
    private var port = 993
    private var pass = String()
    private val dbName = "mail"
    private val dbVersion = 1

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
        val sharedPref = getSharedPreferences("mail", MODE_PRIVATE)
        val defAccount = sharedPref.getInt("openedAccount", -1)
        if (defAccount == -1) {
            // アカウントがない場合追加する
            val intent = Intent(this, AccountAddActivity::class.java)
            startActivityForResult(intent, requestCode)
        } else {
            // デフォルトアカウント取得
            val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
            val database = dbHelper.readableDatabase

            val cursor = database.query(
                "users",
                arrayOf("email", "username", "password"),
                "user_id = ?",
                arrayOf(defAccount.toString()),
                null,
                null,
                null
            )
            cursor.moveToFirst()
            if (cursor.count == 1) {
                val addr = cursor.getString(0)
                host = addr.split('@')[1]
                user = cursor.getString(1)
                // 入ってるパスワードはbase64なのでデコード
                val b64Password = cursor.getString(2)
                pass = Base64.decode(b64Password.toByteArray(), Base64.DEFAULT)
                    .toString(Charset.defaultCharset())
            }
            cursor.close()
        }
    }

    // 他のアクティビティから帰ってきたら呼ばれる
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == this.requestCode && data != null) {
            // メールアドレス、パスワードを取得
            val addr = data.getStringExtra("address")!!
            host = addr.split('@')[1]
            user = addr.split('@')[0]
            pass = data.getStringExtra("password")!!
            // データべースへ追加
            insertUser(addr, user, pass)

            // 初期アカウントの場合、デフォルトアカウントに指定
            val sharedPref = getSharedPreferences("mail", MODE_PRIVATE)
            if (sharedPref.getInt("openedAccount", -1) == -1) {
                val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
                val database = dbHelper.readableDatabase

                // 念のため追加したアカウントのuser_idを取得
                val cursor =
                    database.query("users", arrayOf("MAX(user_id)"), null, null, null, null, null)
                cursor.moveToFirst()
                val userId = if (cursor.count == 1) {
                    cursor.getInt(0)
                } else {
                    // データがない
                    -1
                }
                cursor.close()

                // デフォルトアカウントをセット
                val editor = sharedPref.edit()
                editor.putInt("openedAccount", userId)
                editor.apply()
            }
        }
    }

    // 引っ張って更新されたら呼ばれる
    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        // メールクラス生成
        val mail = Mail(host, user, port, pass)
        coroutineScope.launch {
            // 受信処理
            mail.receive()
        }
    }

    private fun insertUser(email: String, username: String, password: String) {
        val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
        val database = dbHelper.writableDatabase

        val values = ContentValues()
        values.put("email", email)
        values.put("username", username)
        // パスワードはbase64エンコード
        val b64Password = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
        values.put("password", b64Password)

        try {
            database.insertOrThrow("users", null, values)
        } catch (ex: Exception) {
            Toast.makeText(this, getString(R.string.failed_add_account), Toast.LENGTH_LONG).show()
        }
    }
}