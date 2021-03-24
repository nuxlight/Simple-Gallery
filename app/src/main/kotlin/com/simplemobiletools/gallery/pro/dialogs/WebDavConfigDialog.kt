package com.simplemobiletools.gallery.pro.dialogs

import android.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.R

class WebDavConfigDialog(val activity: BaseSimpleActivity) {
    init {
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {

            }
            .show()
    }
}
