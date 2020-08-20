/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 * アカウント追加アクティビティ
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */

package in3a_2180289.intentwork07

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_account_add.*

class AccountAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_add)

        // id取得
        val addrText: TextInputEditText = findViewById(R.id.address)
        val passText: TextInputEditText = findViewById(R.id.password)

        button.setOnClickListener {
            val data = Intent()
            // 入力されたメールアドレスとパスワードを取得
            val addr = addrText.text.toString()
            val pass = passText.text.toString()
            data.putExtra("address", addr)
            data.putExtra("password", pass)

            // resultにセット、終了
            setResult(RESULT_OK, data)
            finish()
        }
    }
}