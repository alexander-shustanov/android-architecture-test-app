package com.example.alexander.library.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alexander.library.R
import com.example.alexander.library.data.Book
import com.example.alexander.library.network.NetworkState
import com.example.alexander.library.ui.info.BookDetailsFragmentArgs
import kotlin.properties.Delegates

class BookListAdapter(val context: Context) : PagedListAdapter<Book, RecyclerView.ViewHolder>(BOOK_DIFF_CALLBACK) {
    private val layoutInflater = LayoutInflater.from(context)

    var networkState: NetworkState by Delegates.observable(NetworkState.LOADED) { _, prevValue, newValue ->
        val hadExtraRow = prevValue != NetworkState.LOADED
        val hasExtraRow = newValue != NetworkState.LOADED
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && prevValue != newValue) {
            notifyItemChanged(itemCount - 1)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? Holder)?.book = getItem(position)!!
    }

    override fun getItemViewType(position: Int): Int = when (networkState) {
        NetworkState.LOADING -> if (position == itemCount - 1) {
            LOADING_HOLDER_TYPE
        } else {
            BOOK_HOLDER_TYPE
        }
        else -> BOOK_HOLDER_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        BOOK_HOLDER_TYPE -> Holder(
            layoutInflater.inflate(R.layout.book_view, parent, false)
        )
        else -> object : RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.loading_placeholder, parent, false)) {}
    }

    override fun getItemCount(): Int = when (networkState) {
        NetworkState.LOADING -> super.getItemCount() + 1
        else -> super.getItemCount()
    }

    companion object {
        val BOOK_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
                oldItem.author == newItem.author
                        && oldItem.title == newItem.title

        }

        private const val BOOK_HOLDER_TYPE = 1
        private const val LOADING_HOLDER_TYPE = 2
    }
}

class Holder(view: View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<TextView>(R.id.title)
    private val author = view.findViewById<TextView>(R.id.author)

    var book by Delegates.observable(Book.PLACEHOLDER_BOOK) { _, _, newValue ->
        title.text = newValue.title
        author.text = newValue.author ?: ""

        title.transitionName = newValue.id + newValue.title
        title.transitionName = newValue.id + newValue.author

        view.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(
                    R.id.bookInfoFragment,
                    BookDetailsFragmentArgs
                        .Builder(newValue.id).build().toBundle(),
                    null,
                    FragmentNavigatorExtras(
                        title to "title",
                        author to "author"
                    )
                )
        }
    }
}