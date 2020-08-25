/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 * データベース定義用クラス
 *
 * 作成日：2020/08/05
 * @author shu-1167
 */

package in3a_2180289.intentwork07

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MailDBHelper(
    context: Context,
    databaseName: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, databaseName, factory, version) {
    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, username TEXT, password TEXT)")
        database?.execSQL("CREATE TABLE IF NOT EXISTS mails (user_id INTEGER, uid INTEGER, subject TEXT, sender TEXT, date TEXT, body BLOB, mime_type TEXT, PRIMARY KEY(user_id, uid))")
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            if (newVersion == 2) database?.execSQL("ALTER TABLE mails ADD COLUMN mime_type TEXT DEFAULT 'text/plain'")
        }
    }
}