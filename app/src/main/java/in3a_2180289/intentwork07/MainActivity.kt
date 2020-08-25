/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.content.ContentValues
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
        lateinit var mRecyclerView: RecyclerView
        const val dbName = "mail"
        const val dbVersion = 1

        // 別スレッド等から処理する用
        val handler = Handler()
    }

    private var adapter = Adapter(arrayOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // view取得
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mRecyclerView = findViewById(R.id.recyclerview)
        mRecyclerView.adapter = adapter
        // レイアウトをセット
        val layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        // 更新イベントリスナー追加
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)

        // アカウント追加アクティビティ
        val sharedPref = getSharedPreferences("mail", MODE_PRIVATE)
        val accountId = sharedPref.getInt("openedAccount", -1)
        if (accountId == -1) {
            // アカウントがない場合追加する
            // view取得
            val constraintLayout = layoutInflater.inflate(R.layout.account_input, null)
            val address: TextInputEditText = constraintLayout.findViewById(R.id.address)
            val password: TextInputEditText = constraintLayout.findViewById(R.id.password)

            val dialog = AlertDialog.Builder(this)
                // ダイアログタイトルをセット
                .setTitle(getString(R.string.account_add_dialog))
                // 入力欄のレイアウトをセット
                .setView(constraintLayout)
                // ダイアログ外タッチで閉じないようにする
                .setCancelable(false)
                .setPositiveButton(getString(R.string.account_add_ok)) { _: DialogInterface, _: Int ->
                    // 「追加」ボタン
                    // 入力された項目を取得
                    val addr = address.text.toString()
                    val user = addr.split('@')[0]
                    val pass = password.text.toString()
                    // データべースへ追加
                    insertUser(addr, user, pass)
                    // タイトル設定
                    title = addr

                    // 初期アカウントの場合、デフォルトアカウントに指定
                    if (sharedPref.getInt("openedAccount", -1) == -1) {
                        val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
                        val database = dbHelper.readableDatabase

                        // 念のため追加したアカウントのuser_idを取得
                        val cursor =
                            database.query(
                                "users",
                                arrayOf("MAX(user_id)"),
                                null,
                                null,
                                null,
                                null,
                                null
                            )
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
                .setNegativeButton(getString(R.string.account_add_cancel)) { _: DialogInterface, _: Int ->
                    // 「後で」ボタン
                    Log.d("Dialog", "Cancelled")
                }
                .create()
            dialog.setOnShowListener {
                // 各種取得
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                // 入力検証用正規表現
                val addrRegix = Regex("[\\w\\-._]+@[\\w\\-._]+\\.[A-Za-z]+")
                // メールアドレス欄にフォーカス
                address.requestFocus()
                positiveButton.isEnabled = false
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        // 入力チェック
                        positiveButton.isEnabled =
                            addrRegix.matches(address.text.toString()) && password.text!!.isNotEmpty()
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                }
                address.addTextChangedListener(textWatcher)
                password.addTextChangedListener(textWatcher)
            }
            dialog.show()
        } else {
            // 保存されているメールを取得する
            val mail = Mail(this, accountId)
            adapter = mail.getStoredMail()
            mRecyclerView.adapter = adapter
            // タイトル設定
            title = mail.getEmailAddress()
        }
    }


    // 引っ張って更新されたら呼ばれる
    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val sharedPref = getSharedPreferences("mail", MODE_PRIVATE)
        val accountId = sharedPref.getInt("openedAccount", -1)
        if (accountId == -1) {
            // アカウントが指定されていない
            Toast.makeText(this, getString(R.string.plz_add_account), Toast.LENGTH_LONG).show()
        } else {
            try {
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                // メールクラス生成
                val mail = Mail(this, accountId)
                coroutineScope.launch {
                    // 受信処理
                    mail.receive(adapter)
                }
            } catch (ex: IllegalArgumentException) {
                Toast.makeText(this, getString(R.string.account_not_found), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    // ユーザをデータベースに追加
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