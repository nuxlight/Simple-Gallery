package com.simplemobiletools.gallery.pro.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.commons.views.MyTextView
import com.simplemobiletools.gallery.pro.R

class WebDavConfigDialog(val activity: BaseSimpleActivity, userTitle: String, configVariable: String) {
    init {
        val sharedPref: SharedPreferences = activity.getSharedPreferences( "kotlinsharedpreference", Context.MODE_PRIVATE)
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_text, null)
        view.findViewById<MyTextView>(R.id.user_entry_title).text = userTitle
        val userEntry = view.findViewById<MyEditText>(R.id.user_entry)
        if (sharedPref.getString(configVariable, "") != "") {
            userEntry.setText(sharedPref.getString(configVariable, ""))
        }
        AlertDialog.Builder(activity)
            .setView(view)
            .setPositiveButton(R.string.ok) { dialog, which ->
                val editor = sharedPref.edit()
                editor.putString(configVariable,userEntry.text.toString())
                editor.apply()
                editor.commit()
            }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {

            }
            .show()
    }
}
