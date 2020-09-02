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
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
        lateinit var mRecyclerView: RecyclerView
        const val dbName = "mail"
        const val dbVersion = 2

        // 別スレッド等から処理する用
        val handler = Handler()
    }


    private lateinit var sharedPref: SharedPreferences
    private var adapter = Adapter(arrayOf())
    private var mail: Mail? = null
    private lateinit var navMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // view取得
        sharedPref = getSharedPreferences("mail", MODE_PRIVATE)
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh)
        mRecyclerView = findViewById(R.id.recyclerview)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navMenu = navigationView.menu
        // 既存アカウント一覧を取得
        val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
        val database = dbHelper.readableDatabase

        val cursor =
            database.query(
                "users",
                arrayOf("email"),
                null,
                null,
                null,
                null,
                null
            )
        cursor.moveToFirst()
        if (cursor.count > 0) {
            while (!cursor.isAfterLast) {
                navMenu.add(cursor.getString(0))
                cursor.moveToNext()
            }
        }
        cursor.close()
        // NavigationViewのアイテム選択リスナー
        navigationView.setNavigationItemSelectedListener {
            Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
            true
        }

        // レイアウトをセット
        val layoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        // 更新イベントリスナー追加
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)

        // アカウント追加アクティビティ
        val accountId = sharedPref.getInt("openedAccount", -1)
        if (accountId == -1) {
            // アカウントがない場合追加する
            addAccount()
        } else {
            // 保存されているメールを取得する
            mail = Mail(this, accountId)
            adapter = mail!!.getStoredMail()
            mRecyclerView.adapter = adapter
            // タイトル設定
            title = mail!!.getEmailAddress()
        }
    }


    // 引っ張って更新されたら呼ばれる
    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val accountId = sharedPref.getInt("openedAccount", -1)
        if (accountId == -1) {
            // アカウントが指定されていない
            Toast.makeText(this, getString(R.string.plz_add_account), Toast.LENGTH_LONG).show()
        } else {
            try {
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                // メールクラス生成
                if (mail == null) {
                    mail = Mail(this, accountId)
                }
                // 受信処理
                coroutineScope.launch {
                    mail!!.receive(adapter)
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

    // オプションメニュー作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    // オプションメニュー内の項目が押されたら呼ばれる
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.account_add -> {
                // アカウント追加
                addAccount()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // アカウント追加処理
    private fun addAccount() {
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
                // 追加したアドレスのuser_idをDBから取得
                val dbHelper = MailDBHelper(this, dbName, null, dbVersion)
                val database = dbHelper.readableDatabase

                val cursor =
                    database.query(
                        "users",
                        arrayOf("user_id"),
                        "email = ?",
                        arrayOf(addr),
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
                // ドロワーメニューに追加
                navMenu.add(addr)
                // 初期アダプターをセット
                adapter = Adapter(arrayOf())
                mRecyclerView.adapter = adapter
                mail = null
            }
            // 「後で」ボタン
            .setNegativeButton(getString(R.string.account_add_cancel)) { _: DialogInterface, _: Int -> }
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
    }
}