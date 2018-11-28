package com.example.alexander.library.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alexander.library.R
import com.example.alexander.library.network.Status
import kotlinx.android.synthetic.main.book_list_fragment.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class BookListFragment : Fragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()

    companion object {
        fun newInstance() = BookListFragment()
    }

    private val viewModel: BookListViewModel by lazy {
        val factory by instance<BookListViewModelFactory>()

        return@lazy ViewModelProviders.of(this, factory)
            .get(BookListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.book_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bookListAdapter = BookListAdapter(requireContext())
        books_list.layoutManager = LinearLayoutManager(requireContext())
        books_list.adapter = bookListAdapter

        swipe_refresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it.status == Status.RUNNING
        })

        viewModel.books.observe(this, Observer {
            bookListAdapter.submitList(it)
        })

        viewModel.networkState.observe(this, Observer {
            bookListAdapter.networkState = it
        })
    }

}
