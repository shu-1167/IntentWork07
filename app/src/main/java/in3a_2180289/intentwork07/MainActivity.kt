package in3a_2180289.intentwork07

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeUtility


class MainActivity : AppCompatActivity() {

    private val host = "example.com"
    // mail address
    private val user = "hogehoge@example.com"
    // IMAPS port
    private val port = 993
    // password
    private val pass = "xxxxxx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            mail()
        }
    }


    private fun mail() {
        //        try {

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)
// http://connector.sourceforge.net/doc-files/Properties.html
        val props: Properties = System.getProperties()
        // セッション
        val session: Session = Session.getInstance(props, null)
        // IMAP で SSL を使用する
        val imap4: Store = session.getStore("imaps")

// 接続
        imap4.connect(host, port, user, pass)
        // println(imap4.urlName.toString())
        Log.d(this.localClassName, imap4.urlName.toString())

// https://stackoverflow.com/questions/11435947/how-do-i-uniquely-identify-a-java-mail-message-using-imap
        val folder = imap4.getFolder("INBOX")
        val uf = folder as UIDFolder
        folder.open(Folder.READ_ONLY)

// 全てのメッセージ
        val totalMessages = folder.messageCount
        // println("Total messages = $totalMessages")
        Log.d(this.localClassName, "Total messages = $totalMessages")

// 新しいメッセージ
        val newMessages = folder.newMessageCount
        // println("New messages = $newMessages")
        Log.d(this.localClassName, "New messages = $newMessages")

// メッセージの一覧を取得
        val msgs: Array<Message> = folder.messages

// メッセージがコピー・移動されていると日付順とは限らない
        for (i in msgs.indices.reversed()) {

// UID を取得
            val messageId = uf.getUID(msgs[i])
            // println("UID = $messageId")
            Log.d(this.localClassName, "UID = $messageId")

// From
            val address: Array<Address> = msgs[i].getFrom()
            var addressText = ""
            if (address != null) {
// address[0].toString() のままでは正しく表示されない
                addressText = MimeUtility.decodeText(address[0].toString())
            }
            // println("Address = $addressText")
            Log.d(this.localClassName, "Address = $addressText")

// Subject
            val subjectText: String = MimeUtility.decodeText(msgs[i].getSubject())
            // println("Subject = $subjectText")
            Log.d(this.localClassName, "Subject = $subjectText")

// 受信日時
            val date: Date = msgs[i].getSentDate()
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val dateText: String = sdf.format(date)
            // println("Date = $dateText")
            Log.d(this.localClassName, "Date = $dateText")

// 本文(  UID より取得 )
            val msg: Message = uf.getMessageByUID(messageId)
            val part: Part = msg
            var bodyText = ""
            if (part.isMimeType("text/plain")) {
                bodyText = part.getContent().toString()
//                } else if (part.isMimeType("multipart/*")) {
//                    val mp = part.getContent()
//                    // マルチパートの先頭
//                    bodyText = mp.getBodyPart(0).content.toString()
//                    if (mp.getBodyPart(1).isMimeType("text/html")) {
//                        val htmlText = mp.getBodyPart(1).content.toString()
//                        println("HtmlText = $htmlText")
//                    }
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
            Log.d(this.localClassName, "Body = $bodyText")
        }
//                println("Body = $bodyText")
//            }
        folder.close()
        imap4.close()
//        } catch (ex: Exception) {
// handle any errors
        // println("Exception: " + ex.message)
//            Log.d(this.localClassName, "Exception: " + ex.message)
//        }
    }
}