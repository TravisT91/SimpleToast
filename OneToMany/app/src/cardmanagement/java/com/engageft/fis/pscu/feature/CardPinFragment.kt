package com.engageft.fis.pscu.feature

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.engageft.apptoolbox.BaseViewModel
import com.engageft.apptoolbox.view.InformationDialogFragment
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding
import com.engageft.fis.pscu.feature.branding.BrandingInfoRepo
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes

/**
 * CardPinFragment
 * <p>
 * Fragment for handling changing card PIN number
 * </p>
 * Created by Atia Hashimi on 12/3/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardPinFragment : BaseEngageFullscreenFragment() {

    private lateinit var binding: FragmentCardPinBinding
    private lateinit var cardPinViewModel: CardPinViewModel
    private val listOfImageViews = ArrayList<ImageView>()

    override fun createViewModel(): BaseViewModel? {
        cardPinViewModel = ViewModelProviders.of(this).get(CardPinViewModel::class.java)
        return cardPinViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardPinBinding.inflate(inflater, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.apply {
            viewModel = cardPinViewModel

            BrandingInfoRepo.cards?.get(0)?.let {
                binding.cardView.applyBranding(it,cardPinViewModel.compositeDisposable) { e ->
                    Toast.makeText(context, "Failed to retrieve card image", Toast.LENGTH_SHORT).show()
                    Log.e("BRANDING_INFO_FAIL", e.message)
                    //TODO(ttkachuk) right now it is not clear on how we should handle failure to retrieve the card image
                }
            }

            listOfImageViews.apply {
                add(iconImageView1)
                add(iconImageView2)
                add(iconImageView3)
                add(iconImageView4)
            }

            pinLayout.setOnClickListener {
                // requestFocus to show keyboard in case keyboard was dismissed
                if (pinInputField.visibility == View.VISIBLE) {
                    pinInputField.clearFocus()
                    pinInputField.requestFocus()
                } else {
                    confirmPinInputField.clearFocus()
                    confirmPinInputField.requestFocus()
                }
            }
        }

        cardPinViewModel.apply {
            cardPinStateObservable.observe(this@CardPinFragment, Observer {
                when (it) {
                    CardPinViewModel.CardPinState.MISMATCH_PIN -> {
                        // do Hanlder().post so that the last PIN digit drawable gets drawn then this code is run, otherwise, it happens so fast.
                        Handler().post {
                            binding.pinInputField.visibility = View.VISIBLE
                            binding.confirmPinInputField.visibility = View.GONE
                            binding.pinInputField.requestFocus()
                            // reset fields
                            binding.confirmPinInputField.inputText = ""
                            binding.pinInputField.inputText = ""

                            val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)!!
                            drawable.setTint(Palette.errorColor)
                            updateView(getString(R.string.card_pin_mismatch_error_message),
                                    Palette.errorColor,
                                    drawable)
                        }
                    }
                    CardPinViewModel.CardPinState.CONFIRM_PIN -> {
                        // do Hanlder().post so that the last PIN digit drawable gets drawn then this code is run, otherwise, it happens really fast.
                        Handler().post {
                            binding.pinInputField.visibility = View.GONE
                            binding.confirmPinInputField.visibility = View.VISIBLE
                            binding.confirmPinInputField.requestFocus()

                            updateView(getString(R.string.card_pin_choose_confirmation),
                                    ContextCompat.getColor(context!!, R.color.textPrimary),
                                    ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)!!)
                        }
                    }
                    CardPinViewModel.CardPinState.INVALID_PIN -> {
                        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)!!
                        drawable.setTint(Palette.errorColor)
                        updateView(getString(R.string.card_pin_choose_description),
                                Palette.errorColor,
                                drawable)
                    }
                    CardPinViewModel.CardPinState.INITIAL_ENTER_PIN -> {
                        updateView(getString(R.string.card_pin_choose_description),
                                ContextCompat.getColor(context!!, R.color.textPrimary),
                                ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)!!)
                    }
                }
            })

            cardPinDigitsState.observe(this@CardPinFragment, Observer {
                when (it.first) {
                    CardPinViewModel.PinDigits.DIGIT_DELETED -> {
                        pinDigitDeleted(it.second)
                    }
                    CardPinViewModel.PinDigits.DIGIT_ADDED -> {
                        pinDigitAdded(it.second)
                    }
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
            binding.pinInputField.requestFocus()
        }
    }

    private fun updateView(text: String, @ColorInt textColor: Int, drawable: Drawable) {
        binding.apply {
            chooseDescriptionTextView.text = text
            chooseDescriptionTextView.setTextColor(textColor)
        }

        for (imageView in listOfImageViews) {
            imageView.background = drawable
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