package com.engageft.fis.pscu.feature

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding
import com.redmadrobot.inputmask.MaskedTextChangedListener

abstract class BaseCardPinFragment : LotusFullScreenFragment() {
    //TODO: styles textView
    //Todo: binding.apply{}
    //TODO: on touch event


    protected lateinit var binding: FragmentCardPinBinding
    protected lateinit var cardPinViewModel: CardPinViewModel
    abstract fun onPinEntered(pin: Int)
    private var tempPinInput = ""
    private val listOfImageViews = ArrayList<ImageView>()

    override fun createViewModel(): BaseViewModel? {
        cardPinViewModel = ViewModelProviders.of(this).get(CardPinViewModel::class.java)
        return cardPinViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardPinBinding.inflate(inflater, container, false)
        binding.viewModel = cardPinViewModel
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val TAG = "CardPinStep1Fragment"
        var tempLength = 0

        binding.pinInputWithLabel.addTextChangeListener(object: MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
//                val pinInput = extractedValue.toInt()
//                if (tempPinInput != extractedValue) {

                    if (extractedValue.length in 0..EngageAppConfig.cardPinLength) {
                        tempPinInput = extractedValue

                        if (extractedValue.length > tempLength) {
                            pinDigitAdded(extractedValue.length - 1)
                        } else if (extractedValue.length < tempLength) {
                            pinDigitDeleted(tempLength - 1)
                        }
                        tempLength = extractedValue.length

                        if (extractedValue.length == EngageAppConfig.cardPinLength) {
                            onPinEntered(extractedValue.toInt())
                        }
                    }
//                }
            }
        })

        listOfImageViews.add(binding.iconImageView1)
        listOfImageViews.add(binding.iconImageView2)
        listOfImageViews.add(binding.iconImageView3)
        listOfImageViews.add(binding.iconImageView4)

        binding.pinLayout.setOnClickListener {
            // requestFocus to show keyboard in case keyboard was dismissed
            binding.pinInputWithLabel.clearFocus()
            binding.pinInputWithLabel.requestFocusOnEditInput()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pinInputWithLabel.requestFocusOnEditInput()

        if (tempPinInput.isNotEmpty() && tempPinInput.length == EngageAppConfig.cardPinLength) {
            // restore view state
            for (index in 0 until listOfImageViews.size) {
                pinDigitAdded(index)
            }
        }
    }

    private fun pinDigitAdded(index: Int) {
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_selected_dot_shape)!!
        DrawableCompat.setTint(drawable, Palette.primaryColor)
        listOfImageViews[index].background = drawable
    }

    private fun pinDigitDeleted(index: Int) {
        listOfImageViews[index].background = ContextCompat.getDrawable(context!!, R.drawable.card_pin_empty_dot_shape)
    }
}