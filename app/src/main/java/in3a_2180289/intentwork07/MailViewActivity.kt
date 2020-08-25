package in3a_2180289.intentwork07

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MailViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_view)

        val bodyText: TextView = findViewById(R.id.textView)
        bodyText.movementMethod = ScrollingMovementMethod()
        // テキストを選択(コピー)できるようにする
        bodyText.setTextIsSelectable(true)
        // 受け取ったメール本文を表示
        bodyText.text = intent.getStringExtra("body")
        title = intent.getStringExtra("subject")
    }
}
