package com.engageft.fis.pscu.feature

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.config.EngageAppConfig
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes
import com.redmadrobot.inputmask.MaskedTextChangedListener

class CardPinFragment : BaseEngageFullscreenFragment() {

    private lateinit var binding: FragmentCardPinBinding
    private lateinit var cardPinViewModel: CardPinViewModel
    private val listOfImageViews = ArrayList<ImageView>()
    private var tempLength = 0

    override fun createViewModel(): BaseViewModel? {
        cardPinViewModel = ViewModelProviders.of(this).get(CardPinViewModel::class.java)
        return cardPinViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardPinBinding.inflate(inflater, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.apply {
            viewModel = cardPinViewModel

            pinInputField.addTextChangeListener(object: MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                    updateView(extractedValue)

                    if (extractedValue.length == EngageAppConfig.cardPinLength) {
                        // must call this after updateView() is done. This allows drawable.background to complete before
                        // running this code so that all drawables are drawn
                        Handler().post {
                            when (cardPinViewModel.cardPinStateObservable.value) {
                                CardPinViewModel.CardPinState.ENTER_PIN -> {
                                    cardPinViewModel.validatePin(extractedValue)
                                }
                                CardPinViewModel.CardPinState.CONFIRM_PIN -> {
                                    cardPinViewModel.submit(pinInputField.inputText.toString())
                                }
                                else -> {
                                    cardPinViewModel.validatePin(extractedValue)
                                }
                            }
                        }
                    }
                }
            })

            listOfImageViews.add(iconImageView1)
            listOfImageViews.add(iconImageView2)
            listOfImageViews.add(iconImageView3)
            listOfImageViews.add(iconImageView4)

            pinLayout.setOnClickListener {
                // requestFocus to show keyboard in case keyboard was dismissed
                pinInputField.clearFocus()
                pinInputField.requestFocusOnEditInput()
            }
        }

        cardPinViewModel.apply {
            cardPinStateObservable.observe(this@CardPinFragment, Observer {
                when (it) {
                    CardPinViewModel.CardPinState.MISMATCH_PIN -> {
                        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)!!
                        drawable.setTint(Palette.errorColor)
                        updateView(getString(R.string.card_pin_mismatch_error_message),
                                Palette.errorColor,
                                drawable)
                    }
                    CardPinViewModel.CardPinState.CONFIRM_PIN -> {
                        updateView(getString(R.string.card_pin_choose_confirmation),
                                ContextCompat.getColor(context!!, R.color.textPrimary),
                                ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)!!)
                    }
                    CardPinViewModel.CardPinState.INVALID_PIN -> {
                        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)!!
                        drawable.setTint(Palette.errorColor)
                        updateView(getString(R.string.card_pin_choose_description),
                                Palette.errorColor,
                                drawable)
                    }
                    else -> {}
                }
            })

            dialogInfoObservable.observe(this@CardPinFragment, Observer {
                when (it.dialogType) {
                    DialogInfo.DialogType.GENERIC_SUCCESS -> {
                        val listener = object: InformationDialogFragment.InformationDialogFragmentListener {
                            override fun onDialogFragmentNegativeButtonClicked() {
                            }

                            override fun onDialogFragmentPositiveButtonClicked() {
                                binding.root.findNavController().popBackStack()
                            }

                            override fun onDialogCancelled() {
                                binding.root.findNavController().popBackStack()
                            }
                        }

                        showDialog(infoDialogGenericSuccessTitleMessageNewInstance(context!!, listener = listener))
                    }
                    else -> {}
                }
            })

            productCardViewModelDelegate.cardInfoModelObservable.observe(this@CardPinFragment, Observer { productCardModel ->
                productCardModel.cardStatusText = getString(productCardModel.cardStatus.cardStatusStringRes())
                binding.cardView.updateWithProductCardModel(productCardModel)
            })

            productCardViewModelDelegate.updateCardView()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // must do Handler().post() because when this fragment is displayed from DashboardFragment,
        // the keyboard doesn't get displayed
        Handler().post {
            binding.pinInputField.requestFocusOnEditInput()
        }
    }

    private fun updateView(text: String, @ColorInt textColor: Int, drawable: Drawable) {
        binding.apply {
            chooseDescriptionTextView.text = text
            chooseDescriptionTextView.setTextColor(textColor)
            // reset field
            pinInputField.inputText = ""
        }

        for (imageView in listOfImageViews) {
            imageView.background = drawable
        }
    }

    private fun updateView(extractedValue: String) {
        if (extractedValue.length in 0..EngageAppConfig.cardPinLength) {

            if (extractedValue.length > tempLength) {
                pinDigitAdded(extractedValue.length - 1)
            } else if (extractedValue.length < tempLength) {
                pinDigitDeleted(tempLength - 1)
            }
            tempLength = extractedValue.length
        }
    }

    private fun pinDigitAdded(index: Int) {
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_selected_dot_shape)!!
        DrawableCompat.setTint(drawable, Palette.primaryColor)
        listOfImageViews[index].background = drawable
    }

    private fun pinDigitDeleted(index: Int) {
        listOfImageViews[index].background = ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)
    }
}