package in3a_2180289.intentwork07

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility


class MainActivity : AppCompatActivity() {

    private val host = "example.com"

    // mail address
    private val user = "hogehoge"

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

        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)
        // http://connector.sourceforge.net/doc-files/Properties.html
        val props = Properties()
        // props.setProperty("mail.debug", "true")
        // セッション
        val session: Session = Session.getInstance(props, null)
        // IMAP で SSL を使用する
        val imap4: Store = session.getStore("imaps")

        // 接続
        imap4.connect(host, port, user, pass)
        Log.d(this.localClassName, imap4.urlName.toString())

        // https://stackoverflow.com/questions/11435947/how-do-i-uniquely-identify-a-java-mail-message-using-imap
        val folder = imap4.getFolder("INBOX")
        val uf = folder as UIDFolder
        folder.open(Folder.READ_ONLY)

        // 全てのメッセージ
        val totalMessages = folder.messageCount
        Log.d(this.localClassName, "Total messages = $totalMessages")

        // 新しいメッセージ
        val newMessages = folder.newMessageCount
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
            val address: Array<Address> = msgs[i].from
            // address[0].toString() のままでは正しく表示されない
            val addressText = MimeUtility.decodeText(address[0].toString())
            // println("Address = $addressText")
            Log.d(this.localClassName, "Address = $addressText")

            // Subject
            val subjectText: String = MimeUtility.decodeText(msgs[i].getSubject())
            // println("Subject = $subjectText")
            Log.d(this.localClassName, "Subject = $subjectText")

            val date: Date = if (msgs[i].sentDate != null) {
                // Dateヘッダ(送信日時)がある場合、そちらを使用
                msgs[i].sentDate
            } else {
                // Dateヘッダがない場合、受信日時を使用
                msgs[i].receivedDate
            }
            // val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val dateText = DateFormat.getDateTimeInstance().format(date)
            // println("Date = $dateText")
            Log.d(this.localClassName, "Date = $dateText")

            // 本文(  UID より取得 )
            // val msg: Message = uf.getMessageByUID(messageId)
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
                        println("HtmlText = $htmlText")
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
            Log.d(this.localClassName, "Body = $bodyText")
        }
        folder.close()
        imap4.close()
    }
}