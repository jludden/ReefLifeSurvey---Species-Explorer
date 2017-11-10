package me.jludden.reeflifesurvey

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_search.*


/**
 * Created by Jason on 11/9/2017.
 */
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        searchback.setOnClickListener(
                {
                    searchback.setBackground(null)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition()
                    } else{
                        finish()
                    }
                })
    }


    /**
     * show the keyboard
     */
    override fun onEnterAnimationComplete() {
        search_view.requestFocus()
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(search_view.getWindowToken(),0,0);
    }

}