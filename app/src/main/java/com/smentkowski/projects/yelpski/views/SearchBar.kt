package com.smentkowski.projects.yelpski.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import com.smentkowski.projects.yelpski.MainInterface

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {

    private val TAG = SearchBar::class.java.simpleName
    private val MS_DELAY_BEFORE_ACCEPTING_TEXT: Long = 300

    var searchHandler: Handler = Handler()
    var mainInterface: MainInterface? = null

    init {
        setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mainInterface?.searchSubmitted(text.toString())
                handled = true
            }
            handled
        })


        // TODO: Possibly add search as you type in the future, for now the api was too slow and paging the list made this difficult
        // TODO: So it was easier to have an official trigger like a button
//        addTextChangedListener(object: TextWatcher {
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                searchHandler.removeCallbacksAndMessages(null)
//                searchHandler.postDelayed({
//                    mainInterface?.searchEntered(s.toString())
//                }, MS_DELAY_BEFORE_ACCEPTING_TEXT)
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        })
    }
}