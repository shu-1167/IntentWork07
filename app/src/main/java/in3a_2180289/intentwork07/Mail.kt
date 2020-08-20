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
import android.util.Log
import in3a_2180289.intentwork07.MainActivity.Companion.handler
import java.text.DateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility

class Mail(
    private val host: String,
    private val user: String,
    private val port: Int,
    private val pass: String
) {

    fun receive(accountId: Int) {
        // 配列を引数にAdapterを生成
        val adapter = Adapter(arrayOf())
        val recyclerView = MainActivity.mRecyclerView
        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
        handler.post {
            kotlin.run {
                // アダプターをセット
                recyclerView.adapter = adapter
            }
        }
        // データベース取得
        val dbHelper =
            MailDBHelper(recyclerView.context, MainActivity.dbName, null, MainActivity.dbVersion)
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
            while (cursor.isAfterLast) {
                mailList.add(cursor.getLong(0))
                cursor.moveToNext()
            }
        }
        cursor.close()

        // https://logicalerror.seesaa.net/article/462358077.html
        // http://connector.sourceforge.net/doc-files/Properties.html
        val props = Properties()
        props.setProperty("mail.imaps.connectiontimeout", "10000")
        props.setProperty("mail.debug", "true")
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
            Log.d(this.javaClass.simpleName, "UID = $messageId")

            // From
            val address: Array<Address> = msgs[i].from
            // address[0].toString() のままでは正しく表示されない
            val addressText = MimeUtility.decodeText(address[0].toString())
            // Log.d(this.javaClass.simpleName, "Address = $addressText")

            // Subject
            val subjectText = if (msgs[i].subject != null) {
                MimeUtility.decodeText(msgs[i].subject)
            } else {
                // 件名がない場合、"(件名なし)"を代入
                recyclerView.context.getString(R.string.null_subject)
            }
            // Log.d(this.javaClass.simpleName, "Subject = $subjectText")

            val date: Date = if (msgs[i].sentDate != null) {
                // Dateヘッダ(送信日時)がある場合、そちらを使用
                msgs[i].sentDate
            } else {
                // Dateヘッダがない場合、受信日時を使用
                msgs[i].receivedDate
            }
            val dateText = DateFormat.getDateTimeInstance().format(date)
            Log.d(this.javaClass.simpleName, "Date = $dateText")

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
                        Log.d(this.javaClass.simpleName, "HtmlText = $htmlText")
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
            Log.d(this.javaClass.simpleName, "Body = $bodyText")

            val item = arrayOf(addressText, subjectText)
            adapter.addItem(item)
            // view更新処理はMainActivityに委託
            handler.post {
                kotlin.run {
                    adapter.notifyDataSetChanged()
                }
            }

            // データベースへの追加処理
            if (!mailList.contains(messageId)) {
                // 新規メール
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
}