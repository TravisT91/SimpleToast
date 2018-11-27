package com.engageft.fis.pscu.feature

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.engageft.fis.pscu.R
import com.engageft.fis.pscu.databinding.FragmentCardPinBinding
import com.redmadrobot.inputmask.MaskedTextChangedListener

class CardPinStep1Fragment : BaseCardPinFragment() {

    private var tempPin: Int = 0
    override fun onStop() {
        super.onStop()
    }

    enum class PinState {
        STATE_STEP_ONE,
        STATE_STEP_TWO,
        PIN_MODIFIED
    }

    private var currentStepState: PinState = PinState.STATE_STEP_ONE
    val TAG = "CardPinStep1Fragment"
    override fun onSaveInstanceState(outState: Bundle) {
        Log.e(TAG, "onSaveInstanceState() called")
        super.onSaveInstanceState(outState)
    }

    override fun onPinEntered(pin: Int) {
        Log.e(TAG, "pin = $pin")
        if (currentStepState == PinState.STATE_STEP_ONE || tempPin != pin) {
            tempPin = pin
            binding.root.findNavController().navigate(R.id.action_cardPinStep1Fragment_to_cardPinStep2Fragment, CardPinStep2Fragment.getBundle(pin))
            currentStepState = PinState.STATE_STEP_TWO
        }

        // reset state for next time, user is returned to this scree
        if (tempPin != pin) {
            currentStepState = PinState.STATE_STEP_ONE
        }
    }
}