package com.ridelineTeam.application.rideline.util.helpers

import android.content.res.Resources
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import com.ridelineTeam.application.rideline.R
import android.util.Patterns
import java.util.regex.Pattern


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
        fun textOnly(textInputLayout: TextInputLayout,resources: Resources) {
            textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    when {
                        editable.toString().trim().isEmpty() -> textInputLayout.error = resources.getString(R.string.requiredFieldMessage)
                        isTextOnly(editable.toString()) -> textInputLayout.error = resources.getString(R.string.textOnlyFieldMessage)
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
        fun phoneNumber(textInputLayout: TextInputLayout,resources: Resources) {
            textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    when {
                        editable.toString().trim().isEmpty() -> textInputLayout.error = resources.getString(R.string.requiredFieldMessage)
                        isPhoneNumber(editable.toString()) -> textInputLayout.error = resources.getString(R.string.phoneFieldMessage)
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
        fun password(textInputLayout: TextInputLayout,resources: Resources) {
            textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    when{
                        editable.toString().trim().isEmpty() -> textInputLayout.error = resources.getString(R.string.requiredFieldMessage)
                        editable.toString().length < 6 -> textInputLayout.error = resources.getString(R.string.passwordLengthError)
                        else-> {
                            textInputLayout.isErrorEnabled = false
                            textInputLayout.error = ""
                        }
                    }
                }
                override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }
            })
        }


        fun isValidEmail(email: String): Boolean {
            val pattern = Patterns.EMAIL_ADDRESS
            return !pattern.matcher(email).matches()
        }

        fun isTextOnly(text:String):Boolean{
            val pattern = Pattern.compile("^[a-zA-ZáÁéÉíÍóÓúÚñÑüÜ\\s]+$")
            return !pattern.matcher(text.trim()).matches()
        }
        fun isPhoneNumber(text:String):Boolean{
            val patterns = Pattern.compile("^[0-9]{8}$")
            return !patterns.matcher(text.trim()).matches()
        }
    }
}