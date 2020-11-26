package in3a_2180289.naviwork06

import androidx.collection.arrayMapOf

object MailSample {
    val SAMPLE = arrayOf(
        // users: メールアドレス, ユーザ名, パスワード
        // mails: タイトル, 送り元, 日時
        arrayMapOf(
            Pair("users", arrayOf("hoge@example.com", "hoge", "hoge")),
            Pair(
                "mails",
                arrayOf(
                    arrayOf("あいうえお", "sample@example.com", "2020/11/27 0:00:00"),
                    arrayOf("かきくけこ", "sample@example.net", "2020/11/27 0:00:00"),
                    arrayOf("さしすせそ", "sample@example.co.jp", "2020/11/27 0:00:00")
                )
            )
        ),
        arrayMapOf(
            Pair("users", arrayOf("fuga@example.com", "fuga", "fuga")),
            Pair(
                "mails",
                arrayOf(
                    arrayOf("abcde", "sample@example.net", "2020/11/27 0:00:00"),
                    arrayOf("fghij", "sample@example.co.jp", "2020/11/27 0:00:00"),
                    arrayOf("klmop", "sample@example.com", "2020/11/27 0:00:00")
                )
            )
        ),
        arrayMapOf(
            Pair("users", arrayOf("foo@example.net", "foo", "foo")),
            Pair(
                "mails",
                arrayOf(
                    arrayOf("123", "sample@example.jp", "2020/11/27 0:00:00"),
                    arrayOf("456", "sample@example.net", "2020/11/27 0:00:00"),
                    arrayOf("789", "sample@example.com", "2020/11/27 0:00:00")
                )
            )
        ),
        arrayMapOf(
            Pair("users", arrayOf("bar@example.net", "bar", "bar")),
            Pair(
                "mails",
                arrayOf(
                    arrayOf("あかさたなはまやらわ", "sample@example.jp", "2020/11/27 0:00:00"),
                    arrayOf("いきしちにひみゆりを", "sample@example.com", "2020/11/27 0:00:00"),
                    arrayOf("うくすつぬふむよるん", "sample@example.co.jp", "2020/11/27 0:00:00")
                )
            )
        )
    )
}