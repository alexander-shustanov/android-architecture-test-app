package com.example.alexander.library.ui.info

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.alexander.library.R
import kotlinx.android.synthetic.main.book_details_fragment.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class BookDetailsFragment : Fragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()

    private val viewModel: BookDetailsViewModel by lazy {
        val factory by instance<BookDetailViewModelFactory>()

        return@lazy ViewModelProviders.of(this, factory)
            .get(BookDetailsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.book_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.setBookId(BookDetailsFragmentArgs.fromBundle(arguments).book)

        viewModel.book.observe(this as LifecycleOwner, Observer {
            title.setText(it.title)
            author.setText(it.author)
        })

        title.afterTextChanged {
            viewModel.book.value?.title = it
            viewModel.notifyChange()
        }

        author.afterTextChanged {
            viewModel.book.value?.author = it
            viewModel.notifyChange()
        }

        viewModel.showSave.observe(this, Observer {
            if (save_button.visibility != View.VISIBLE && it) {
                save_button.show()
            } else if (save_button.visibility == View.VISIBLE && !it) {
                save_button.hide()
            }
        })

        save_button.setOnClickListener {
            viewModel.save()
        }
    }

    companion object {
        fun newInstance() = BookDetailsFragment()

        fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
            this.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    afterTextChanged.invoke(editable.toString())
                }
            })
        }
    }

}
