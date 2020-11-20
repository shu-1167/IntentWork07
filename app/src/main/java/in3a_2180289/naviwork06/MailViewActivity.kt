package in3a_2180289.naviwork06

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
        // エンコード(charset)の取得
        val enc = intent.getStringExtra("encoding")
        val webView: WebView = findViewById(R.id.webView)!!
        // webView.settings.javaScriptEnabled = true
        // 受け取ったメール本文を表示
        webView.loadDataWithBaseURL(null, body, mime, enc, null)
    }
}
