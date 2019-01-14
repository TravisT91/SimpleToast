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
import com.engageft.fis.pscu.EnrollmentActivity
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding
import com.engageft.fis.pscu.feature.branding.Palette
import com.engageft.fis.pscu.feature.palettebindings.applyBranding
import com.engageft.fis.pscu.feature.utils.cardStatusStringRes
import com.ob.domain.lookup.branding.BrandingCard

/**
 * CardPinFragment
 * <p>
 * Fragment for handling changing card PIN number
 * </p>
 * Created by Atia Hashimi on 12/3/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class CardPinFragment : BaseEngagePageFragment() {

    private lateinit var binding: FragmentCardPinBinding
    private lateinit var viewModel: BaseEngageViewModel
    private lateinit var cardPinViewModel: CardPinViewModelDelegate
    private val listOfImageViews = ArrayList<ImageView>()
    private lateinit var unselectedDot: Drawable
    private lateinit var selectedDot: Drawable

    private val brandingCardObserver = Observer<BrandingCard> { updateBrandingCard(it) }

    override fun createViewModel(): BaseViewModel? {
        // This Fragment's usage is supported in two places:
        // 1. The Enrollment flow as a part of the EnrollmentViewModel
        // 2. The CardManagement flow as a part of the CardPinViewModel.
        viewModel = if (activity is EnrollmentActivity) {
            val vm = ViewModelProviders.of(activity!!).get(EnrollmentViewModel::class.java)
            cardPinViewModel = vm.cardPinDelegate.cardPinViewModelDelegate
            vm
        } else {
            val vm = ViewModelProviders.of(this).get(CardPinViewModel::class.java)
            cardPinViewModel = vm.cardPinViewModelDelegate
            vm
        }
        return viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCardPinBinding.inflate(inflater, container, false)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        unselectedDot = ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)!!
        selectedDot = ContextCompat.getDrawable(context!!, R.drawable.card_pin_selected_dot_shape)!!
        DrawableCompat.setTint(selectedDot, Palette.primaryColor)
        binding.apply {
            viewModel = cardPinViewModel

            listOfImageViews.clear()
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
                    CardPinViewModelDelegate.CardPinState.MISMATCH_PIN -> {
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
                    CardPinViewModelDelegate.CardPinState.CONFIRM_PIN -> {
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
                    CardPinViewModelDelegate.CardPinState.INVALID_PIN -> {
                        val drawable = ContextCompat.getDrawable(context!!, R.drawable.card_pin_error_dot_shape)!!
                        drawable.setTint(Palette.errorColor)
                        updateView(getString(R.string.card_pin_choose_description),
                                Palette.errorColor,
                                drawable)
                    }
                    CardPinViewModelDelegate.CardPinState.INITIAL_ENTER_PIN -> {
                        updateView(getString(R.string.card_pin_choose_description),
                                ContextCompat.getColor(context!!, R.color.textPrimary),
                                ContextCompat.getDrawable(context!!, R.drawable.card_pin_unselected_dot_shape)!!)
                    }
                }
            })
            brandingCardObservable.observe(this@CardPinFragment, brandingCardObserver)

            cardPinDigitsState.observe(this@CardPinFragment, Observer {
                when (it.first) {
                    CardPinViewModelDelegate.PinDigits.DIGIT_DELETED -> {
                        pinDigitDeleted(it.second)
                    }
                    CardPinViewModelDelegate.PinDigits.DIGIT_ADDED -> {
                        pinDigitAdded(it.second)
                    }
                    CardPinViewModelDelegate.PinDigits.DIGITS_CLEARED -> {
                        pinDigitsCleared()
                    }
                }
            })

            (fragmentDelegate.viewModel!! as BaseEngageViewModel).dialogInfoObservable.observe(this@CardPinFragment, Observer {
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

                        fragmentDelegate.showDialog(infoDialogGenericSuccessTitleMessageNewInstance(context!!, listener = listener))
                    }
                    else -> {}
                }
            })

            productCardViewModelDelegate.cardInfoModelObservable.observe(this@CardPinFragment, Observer { productCardModel ->
                productCardModel.cardStatusText = getString(productCardModel.cardStatus.cardStatusStringRes())
                binding.cardView.updateWithProductCardModel(productCardModel)
            })
        }

        return binding.root
    }

    private fun updateBrandingCard(brandingCard: BrandingCard?) {
        brandingCard?.let {
            binding.cardView.applyBranding(it, viewModel.compositeDisposable) { e ->
                Toast.makeText(context, "Failed to retrieve card image", Toast.LENGTH_SHORT).show()
                Log.e("BRANDING_INFO_FAIL", e.message)
                //TODO(ttkachuk) right now it is not clear on how we should handle failure to retrieve the card image
                //tracked in FOTM-497
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // must do Handler().post() because when this fragment is displayed from DashboardFragment,
        // the keyboard doesn't get displayed
        Handler().post {
            if (binding.pinInputField.visibility == View.VISIBLE) {
                binding.pinInputField.clearFocus()
                binding.pinInputField.requestFocus()
            } else {
                binding.confirmPinInputField.clearFocus()
                binding.confirmPinInputField.requestFocus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardPinViewModel.cardPinDigitsState.removeObservers(this)
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
        for (i in 0..index) {
            listOfImageViews[i].background = selectedDot
        }
    }

    private fun pinDigitDeleted(index: Int) {
        listOfImageViews[index].background = unselectedDot
    }

    private fun pinDigitsCleared() {
        listOfImageViews[0].background = unselectedDot
        listOfImageViews[1].background = unselectedDot
        listOfImageViews[2].background = unselectedDot
        listOfImageViews[3].background = unselectedDot
    }
}