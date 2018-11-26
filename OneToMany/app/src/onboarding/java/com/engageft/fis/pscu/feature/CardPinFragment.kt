package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.LotusFullScreenFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding

class CardPinFragment : LotusFullScreenFragment() {

    private lateinit var binding: FragmentCardPinBinding
    private lateinit var cardPinViewModel: CardPinViewModel

    override fun createViewModel(): BaseViewModel? {
        cardPinViewModel = ViewModelProviders.of(this).get(CardPinViewModel::class.java)
        return cardPinViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardPinBinding.inflate(inflater, container, false)
        binding.viewModel = cardPinViewModel
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        cardPinViewModel.flowObservable.observe(this, Observer {
            when (it) {
                CardPinViewModel.CardPinFlow.CONFIRM_PIN -> {

                }
                CardPinViewModel.CardPinFlow.MISMATCH_PIN -> {

                }
            }
        })
        val TAG = "CardPinFragment"
        var tempLength = 0
        var pin = 0
        var confirmPin = 0
        binding.pinInputWithLabel.addEditTextTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.e(TAG, "s = $s")
                s?.let { pinNum ->
                    Log.e(TAG, "s = $pinNum")
                    // todo rename
                    if (pinNum.length in 0..4) {
                        if (pinNum.length > tempLength) {
                            pinAdded(pinNum.length)
                        } else if (pinNum.length < tempLength) {
                            pinDeleted(tempLength)
                        }
                        tempLength = pinNum.length
                    }

                    if (pinNum.isNotEmpty()) {
                        pin = pinNum.toString().toInt()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.confirmPinInputWithLabel.addEditTextTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let { pinNum ->
                    // todo rename
                    if (pinNum.length <= 4) {
                        confirmPin = pinNum.toString().toInt()
                        if (pinNum.length > tempLength) {
                            pinAdded(pinNum.length)
                        } else if (pinNum.length < tempLength) {
                            pinDeleted(tempLength)
                        }
                        tempLength = pinNum.length
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.pinInputWithLabel.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.pinInputWithLabel.onImeAction(EditorInfo.IME_ACTION_DONE) {
            cardPinViewModel.validatePin(pin)
        }

        binding.confirmPinInputWithLabel.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.confirmPinInputWithLabel.onImeAction(EditorInfo.IME_ACTION_DONE) {
            cardPinViewModel.confirmPin(confirmPin)
        }
        binding.pinInputWithLabel.isFocusableInTouchMode = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pinInputWithLabel.editText.requestFocus()
    }

    private fun pinAdded(length: Int) {
        when (length) {
            1 -> {
                binding.iconImageView1.background = ContextCompat.getDrawable(context!!, R.drawable.shape_selected)

//                val gradientDrawable = binding.iconImageView1.background as GradientDrawable
//                gradientDrawable.setColor(ContextCompat.getColor(context!!, R.color.primary))
//                gradientDrawable.setStroke(0, ContextCompat.getColor(context!!, R.color.primary))
            }
            2 -> {
                binding.iconImageView2.background = ContextCompat.getDrawable(context!!, R.drawable.shape_selected)

//                val gradientDrawable = binding.iconImageView2.background as GradientDrawable
//                gradientDrawable.setColor(ContextCompat.getColor(context!!, R.color.primary))
//                gradientDrawable.setStroke(0, ContextCompat.getColor(context!!, R.color.primary))
            }
            3 -> {
                binding.iconImageView3.background = ContextCompat.getDrawable(context!!, R.drawable.shape_selected)

//                val gradientDrawable = binding.iconImageView3.background as GradientDrawable
//                gradientDrawable.setColor(ContextCompat.getColor(context!!, R.color.primary))
//                gradientDrawable.setStroke(0, ContextCompat.getColor(context!!, R.color.primary))
            }
            4 -> {
                binding.iconImageView4.background = ContextCompat.getDrawable(context!!, R.drawable.shape_selected)

//                val gradientDrawable = binding.iconImageView4.background as GradientDrawable
//                gradientDrawable.setColor(ContextCompat.getColor(context!!, R.color.primary))
//                gradientDrawable.setStroke(0, ContextCompat.getColor(context!!, R.color.primary))
            }
        }
    }

    private fun pinDeleted(length: Int) {
        when (length) {
            1 -> {
                binding.iconImageView1.background = ContextCompat.getDrawable(context!!, R.drawable.shape)
            }
            2 -> {
                binding.iconImageView2.background = ContextCompat.getDrawable(context!!, R.drawable.shape)
            }
            3 -> {
                binding.iconImageView3.background = ContextCompat.getDrawable(context!!, R.drawable.shape)
            }
            4 -> {
                binding.iconImageView4.background = ContextCompat.getDrawable(context!!, R.drawable.shape)
            }
        }
    }
}