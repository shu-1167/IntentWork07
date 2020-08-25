/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 * メール処理用クラス
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import in3a_2180289.intentwork07.MainActivity.Companion.handler
import java.nio.charset.Charset
import java.text.DateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility

class Mail constructor(_context: Context, _accountId: Int) {
    private val accountId = _accountId
    private val context = _context
    private val host: String
    private val user: String
    private val port: Int = 993
    private val pass: String
    private val dbName = MainActivity.dbName
    private val dbVersion = MainActivity.dbVersion

    init {
        // コンストラクタ
        // アカウント情報取得
        val dbHelper = MailDBHelper(context, dbName, null, dbVersion)
        val database = dbHelper.readableDatabase

        val cursor = database.query(
            "users",
            arrayOf("email", "username", "password"),
            "user_id = ?",
            arrayOf(accountId.toString()),
            null,
            null,
            null
        )
        cursor.moveToFirst()
        if (cursor.count == 1) {
            // 各種情報取得
            val addr = cursor.getString(0)
            host = addr.split('@')[1]
            user = cursor.getString(1)
            // 入ってるパスワードはbase64なのでデコード
            val b64Password = cursor.getString(2)
            pass = Base64.decode(b64Password.toByteArray(), Base64.DEFAULT)
                .toString(Charset.defaultCharset())
        } else {
            throw IllegalArgumentException("id: $accountId is not found")
        }
        cursor.close()
    }

    fun receive(adapter: Adapter) {
        // OnItemClickListenerをセット
        adapter.setOnItemClickListener(mOnItemClickListener)
        val recyclerView = MainActivity.mRecyclerView
        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
        handler.post {
            kotlin.run {
                // アダプターをセット
                recyclerView.adapter = adapter
            }
        }
        // データベース取得
        val dbHelper = MailDBHelper(context, dbName, null, dbVersion)
        val database = dbHelper.writableDatabase
        // 取得済みメール一覧の取得
        val cursor = database.query(
            "mails",
            arrayOf("uid"),
            "user_id = ?",
            arrayOf(accountId.toString()),
            null,
            null,
            null
        )
        cursor.moveToFirst()
        val mailList = mutableListOf<Long>()
        if (cursor.count > 0) {
            while (!cursor.isAfterLast) {
                mailList.add(cursor.getLong(0))
                cursor.moveToNext()
            }
        }
        cursor.close()

        // https://logicalerror.seesaa.net/article/462358077.html
        // http://connector.sourceforge.net/doc-files/Properties.html
        val props = Properties()
        props.setProperty("mail.imaps.connectiontimeout", "10000")
        // props.setProperty("mail.debug", "true")
        // セッション
        val session: Session = Session.getInstance(props, null)
        // IMAP で SSL を使用する
        val imap4: Store = session.getStore("imaps")

        // 接続
        imap4.connect(host, port, user, pass)

        // https://stackoverflow.com/questions/11435947/how-do-i-uniquely-identify-a-java-mail-message-using-imap
        val folder = imap4.getFolder("INBOX")
        val uf = folder as UIDFolder
        folder.open(Folder.READ_ONLY)

        // メッセージの一覧を取得
        val msgs: Array<Message> = folder.messages

        // メッセージがコピー・移動されていると日付順とは限らない
        for (i in msgs.indices.reversed()) {

            // UID を取得
            val messageId = uf.getUID(msgs[i])
            // Log.d(this.javaClass.simpleName, "UID = $messageId")

            // From
            val address: Array<Address> = msgs[i].from
            // address[0].toString() のままでは正しく表示されない
            var addressString = address[0].toString()

            // https://oshiete.goo.ne.jp/qa/519669.html
            // =?で始まってるか調べる、?=(空白)=?(文字コード)?(エンコード)?がなければそのまま、あれば何もなしに全て置き換え
            if (Regex("^=\\?.+?\\?.\\?.+").containsMatchIn(addressString)) {
                addressString = addressString.replace(Regex("\\?=\\s=\\?.+?\\?.\\?"), "")
            }
            val addressText = MimeUtility.decodeText(addressString)
            // Log.d(this.javaClass.simpleName, "Address = $addressText")

            // Subject
            val subjectText = if (msgs[i].subject != null) {
                var subjectString = msgs[i].getHeader("Subject")[0].toString()
                if (Regex("^=\\?.+?\\?.\\?.+").containsMatchIn(subjectString)) {
                    // 1行ずつエンコードされている場合もある、最後のパディング用=があれば処理しない
                    val base64Regex = Regex("^=\\?.+?\\?B\\?.+")
                    val base64OneLineRegex = Regex("\\?B\\?.+?=\\?=[\\n\\s]+=\\?")
                    val quotedRegex = Regex("^=\\?.+?\\?Q\\?.+")
                    if (base64Regex.containsMatchIn(subjectString) &&
                        !base64OneLineRegex.containsMatchIn(subjectString) ||
                        quotedRegex.containsMatchIn(subjectString)
                    ) {
                        subjectString =
                            subjectString.replace(Regex("\\?=[\\n\\s]+=\\?.+?\\?.\\?"), "")
                    }
                }
                MimeUtility.decodeText(subjectString)
            } else {
                // 件名がない場合、"(件名なし)"を代入
                context.getString(R.string.null_subject)
            }
            Log.d(this.javaClass.simpleName, "Subject = $subjectText")

            val date: Date = if (msgs[i].sentDate != null) {
                // Dateヘッダ(送信日時)がある場合、そちらを使用
                msgs[i].sentDate
            } else {
                // Dateヘッダがない場合、受信日時を使用
                msgs[i].receivedDate
            }
            val dateText = DateFormat.getDateTimeInstance().format(date)
            // Log.d(this.javaClass.simpleName, "Date = $dateText")

            // 本文(  UID より取得 )
            val part: Part = msgs[i]
            var bodyText = ""
            if (part.isMimeType("text/plain")) {
                bodyText = part.content.toString()
            } else if (part.isMimeType("multipart/*")) {
                // マルチパートの先頭
                val mp = part.content as MimeMultipart
                bodyText = mp.getBodyPart(0).content.toString()
                for (j in 1..mp.count.dec()) {
                    if (mp.getBodyPart(j).isMimeType("text/html")) {
                        val htmlText = mp.getBodyPart(j).content.toString()
                        // Log.d(this.javaClass.simpleName, "HtmlText = $htmlText")
                    }
                }
//                    var filename: String = (mp.getBodyPart(1) as Part).getFileName()
//                    // ファイル名があったら保存
//                    if (filename != null) {
//                        println("ファイル名 = $filename")
//                        val file = File(filename)
//                        (mp.getBodyPart(1) as MimeBodyPart).saveFile(file)
            }

// 3つ目がある場合
//                    if (mp.count > 2) {
//                        filename = (mp.getBodyPart(2) as Part).getFileName()
//                        // ファイル名があったら保存
//                        if (filename != null) {
//                            println("ファイル名 = $filename")
//                            val file = File(filename)
//                            (mp.getBodyPart(2) as MimeBodyPart).saveFile(file)
//                        }
//                    }
            // Log.d(this.javaClass.simpleName, "Body = $bodyText")

            if (!mailList.contains(messageId)) {
                // 新規メール
                // Adapterに追加
                val item = arrayOf(messageId.toString(), addressText, subjectText)
                adapter.addItem(item)
                adapter.sort()
                // view更新処理はMainActivityに委託
                handler.post {
                    kotlin.run {
                        adapter.notifyDataSetChanged()
                    }
                }

                // データベースへの追加処理
                val values = ContentValues()
                values.put("user_id", accountId)
                values.put("uid", messageId)
                values.put("subject", subjectText)
                values.put("sender", addressText)
                values.put("date", dateText)
                values.put("body", bodyText)

                try {
                    database.insertOrThrow("mails", null, values)
                } catch (ex: Exception) {
                    Log.e("insertMail", ex.toString())
                }

                mailList.add(messageId)
            }
        }
        // 閉じる
        folder.close()
        imap4.close()

        // くるくる回ってるのを止める
        MainActivity.mSwipeRefreshLayout.isRefreshing = false
    }

    // RecyclerViewのItemClickListener設定
    private val mOnItemClickListener = object : Adapter.OnItemClickListener {
        override fun onItemClickListener(view: View, position: Int, uid: Long) {
            // 現在のアカウントを取得
            val sharedPref =
                context.getSharedPreferences("mail", AppCompatActivity.MODE_PRIVATE)
            val accountId = sharedPref.getInt("openedAccount", -1)
            if (accountId == -1) {
                // アカウントが指定されていない
                Toast.makeText(
                    context,
                    context.getString(R.string.plz_add_account),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // メール本文を取得し、渡す
                val intent = Intent(context, MailViewActivity::class.java)
                val dbHelper = MailDBHelper(context, dbName, null, dbVersion)
                val database = dbHelper.readableDatabase
                val cursor = database.query(
                    "mails",
                    arrayOf("body", "subject", "mime_type"),
                    "user_id = ? and uid = ?",
                    arrayOf(accountId.toString(), uid.toString()),
                    null,
                    null,
                    null
                )
                cursor.moveToFirst()
                if (cursor.count == 1) {
                    intent.putExtra("body", cursor.getString(0))
                    intent.putExtra("subject", cursor.getString(1))
                    intent.putExtra("mimeType", cursor.getString(2))
                }
                cursor.close()
                context.startActivity(intent)
            }
        }
    }

    fun getStoredMail(): Adapter {
        // DBに保存されているメールを取得し、返す
        val adapter = Adapter(arrayOf())
        val dbHelper = MailDBHelper(context, MainActivity.dbName, null, MainActivity.dbVersion)
        val database = dbHelper.readableDatabase

        val cursor = database.query(
            "mails",
            arrayOf("uid", "sender", "subject"),
            "user_id = ?",
            arrayOf(accountId.toString()),
            null,
            null,
            "uid DESC"
        )
        cursor.moveToFirst()
        if (cursor.count > 0) {
            while (!cursor.isAfterLast) {
                val messageId = cursor.getLong(0)
                val from = cursor.getString(1)
                val subject = cursor.getString(2)
                val item = arrayOf(messageId.toString(), from, subject)
                adapter.addItem(item)
                cursor.moveToNext()
            }
        }
        cursor.close()
        adapter.setOnItemClickListener(mOnItemClickListener)
        return adapter
    }

    fun getEmailAddress(): String {
        // 現在のユーザのメールアドレスを返す
        val dbHelper = MailDBHelper(context, MainActivity.dbName, null, MainActivity.dbVersion)
        val database = dbHelper.readableDatabase

        val cursor = database.query(
            "users",
            arrayOf("email"),
            "user_id = ?",
            arrayOf(accountId.toString()),
            null,
            null,
            null
        )
        cursor.moveToFirst()
        if (cursor.count == 1) {
            val email = cursor.getString(0)
            cursor.close()
            return email
        } else {
            cursor.close()
            throw IllegalArgumentException("id: $accountId is not found")
        }
    }
}