package com.ridelineTeam.application.rideline.util.helpers

import android.content.res.Resources
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import com.ridelineTeam.application.rideline.R
import android.util.Patterns



class InputsHelper {
    companion object {
        fun required(textInputLayout: TextInputLayout,resources: Resources) {
             textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
                 override fun afterTextChanged(editable: Editable?) {
                     if (editable.toString().trim().isEmpty()) {
                         textInputLayout.error = resources.getString(R.string.requiredFieldMessage)
                     } else {
                         textInputLayout.isErrorEnabled = false
                         textInputLayout.error = null
                     }
                 }

                 override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                 }

                 override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                 }

             })
         }
        fun email(textInputLayout: TextInputLayout,resources: Resources) {
            textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    when {
                        editable.toString().trim().isEmpty() -> textInputLayout.error = resources.getString(R.string.requiredFieldMessage)
                        isValidEmail(editable.toString()) -> textInputLayout.error = resources.getString(R.string.invalidEmail)
                        else -> {
                            textInputLayout.isErrorEnabled = false
                            textInputLayout.error = null
                        }
                    }
                }

                override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

            })
        }

        private fun isValidEmail(email: String): Boolean {
            val pattern = Patterns.EMAIL_ADDRESS
            return !pattern.matcher(email).matches()
        }

    }
}