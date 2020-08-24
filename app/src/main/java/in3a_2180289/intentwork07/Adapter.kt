/**
 * スマートフォン・アプリ演習Ⅰ
 * 第6章オリジナル作品制作（その２）
 * Recyclerview用アダプター
 *
 * 作成日：2020/08/13
 * @author shu-1167
 */
package in3a_2180289.intentwork07

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class Adapter(private var list: Array<Array<String>>): RecyclerView.Adapter<Adapter.ViewHolder>() {
    private lateinit var listener: OnItemClickListener

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    // 上のViewHolderクラスを使用してViewHolderを作成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val item = layoutInflater.inflate(R.layout.recyclerview_item, parent, false)
        return ViewHolder(item)
    }

    // RecyclerViewのサイズ
    override fun getItemCount(): Int {
        return list.size
    }

    // ViewHolderに表示するテキストを設定
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.textView1.text = list[position][1]
        holder.itemView.textView2.text = list[position][2]
        // クリックイベント
        holder.itemView.setOnClickListener {
            listener.onItemClickListener(it, position, list[position][0].toLong())
        }
    }

    fun addItem(model: Array<String>) {
        list += model
    }

    fun sort() {
        list.sortByDescending { it.first() }
    }

    //インターフェースの作成
    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, uid: Long)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}