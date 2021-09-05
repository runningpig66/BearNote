package cloud.runningpig.bearnote.ui.note

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cloud.runningpig.bearnote.R
import cloud.runningpig.bearnote.databinding.CategoryItemBinding
import cloud.runningpig.bearnote.logic.model.IconMap
import cloud.runningpig.bearnote.logic.model.NoteCategory

class ItemList1Adapter(private val onItemClick: (position: Int, item: NoteCategory?) -> Unit) :
    ListAdapter<NoteCategory, ItemList1Adapter.ViewHolder>(ItemComparator()) {
    private var mSelectedPosition: Int = -1

    fun setSelectedPosition(p: Int) {
        mSelectedPosition = p
    }

    fun getSelectedPosition() = mSelectedPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noteCategory = getItem(position)
        holder.itemView.setOnClickListener {
            // TODO 添加或删除数据后，参数中的position点击获取到的还是旧数据的position
            if (holder.bindingAdapterPosition != currentList.lastIndex) {
                onItemClick(position, noteCategory)
                mSelectedPosition = holder.bindingAdapterPosition
                notifyDataSetChanged()
            } else {
                onItemClick(-1, null)
            }
        }
        holder.bind(noteCategory, position, mSelectedPosition)
    }

    class ViewHolder(private var binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(noteCategory: NoteCategory, p1: Int, p2: Int) {
            binding.apply {
                textView.text = noteCategory.name
                imageView.setImageResource(IconMap.map[noteCategory.icon] ?: R.drawable.ic_error)
                Log.d("test20210904", "p1: $p1, p2: $p2")
                if (p1 == p2) {
                    imageView.setBackgroundResource(R.drawable.oval_solid_ff5722)
                } else {
                    imageView.setBackgroundResource(R.drawable.oval_solid_f5f5f5)
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                return ViewHolder(CategoryItemBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<NoteCategory>() {
        override fun areItemsTheSame(oldItem: NoteCategory, newItem: NoteCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteCategory, newItem: NoteCategory): Boolean {
            return oldItem.name == newItem.name
        }
    }

}