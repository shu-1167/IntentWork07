package in3a_2180289.intentwork07

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MailViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_view)

        // タイトルのセット
        title = intent.getStringExtra("subject")
        // 本文の取得
        val body = intent.getStringExtra("body")
        // MIMEタイプの取得
        val mime = intent.getStringExtra("mimeType")
        val webView: WebView = findViewById(R.id.webView)!!
        // 受け取ったメール本文を表示
        webView.loadData(body, mime, "utf-8")
    }
}
