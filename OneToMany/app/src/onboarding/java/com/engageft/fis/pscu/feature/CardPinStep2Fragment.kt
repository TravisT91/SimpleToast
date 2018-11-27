package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.engageft.fis.pscu.R

class CardPinStep2Fragment : BaseCardPinFragment() {
    //todo rename shapes
    //TODO: styles textView
    //Todo: binding.apply{}

    companion object {
        private const val CARD_PIN_NUMBER = "CARD_PIN_NUMBER"

        fun getBundle(pin: Int): Bundle {
            val bundle = Bundle()
            bundle.putInt(CARD_PIN_NUMBER, pin)
            return bundle
        }
    }

    private var cardPinNumber = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let { bundle ->
            cardPinNumber = bundle.getInt(CARD_PIN_NUMBER, 0)
        }
        super.onCreateView(inflater, container, savedInstanceState)
        binding.chooseDescriptionTextView.text = getString(R.string.card_pin_choose_confirmation)

        cardPinViewModel.flowObservable.observe(this, Observer {
            when (it) {
                CardPinViewModel.CardPinFlow.INVALID_PIN -> {
//                    binding.chooseDescriptionTextView.text = getString(R.string.)
                }
                CardPinViewModel.CardPinFlow.CONFIRM_PIN -> {

                }
                CardPinViewModel.CardPinFlow.MISMATCH_PIN -> {
                    binding.chooseDescriptionTextView.text = getString(R.string.card_pin_mismatch_error_message)
                    // reset field
                    binding.pinInputWithLabel.inputText = ""

                    val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)
                    val listOfViews = listOf<ImageView>(binding.iconImageView1, binding.iconImageView2, binding.iconImageView3, binding.iconImageView4)
                    for (listOfView in listOfViews) {
                        listOfView.background = drawable
                    }
                }
            }
        })
//        cardPinViewModel.submit(-644, -644)
        return binding.root
    }

    override fun onPinEntered(pin: Int) {
        cardPinViewModel.submit(cardPinNumber, pin)
    }
}