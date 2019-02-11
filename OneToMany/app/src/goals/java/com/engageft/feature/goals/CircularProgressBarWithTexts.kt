package com.engageft.feature.goals

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.engageft.apptoolbox.util.applyBackgroundColor
import com.engageft.apptoolbox.util.applyColors
import com.engageft.apptoolbox.util.applyProgressColor
import com.engageft.apptoolbox.util.setStyle
import com.engageft.apptoolbox.util.showOrRemove
import com.engageft.fis.pscu.R

class CircularProgressBarWithTexts(context : Context, val attrs: AttributeSet?, var defStyleAttr: Int) :
        ConstraintLayout(context,attrs,defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    companion object {
        const val NOT_SET = 0
    }

    private var styleId : Int = NOT_SET
    private val innerTopTextView : TextView
    private val innerBottomTextView : TextView
    private val outerBottomTextView : TextView
    private val progressBar : ProgressBar

    init {
        inflate(context, R.layout.circular_progress_bar_with_texts, this)
        progressBar = findViewById(R.id.progress_bar)
        innerTopTextView = findViewById(R.id.topInnerTextView)
        innerBottomTextView = findViewById(R.id.bottomInnerTextView)
        outerBottomTextView = findViewById(R.id.bottomOuterTextView)
        setAttrs(attrs)
    }

    private fun setAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBarWithTexts, defStyleAttr, styleId).apply {

            //progress bar
            getResourceId(R.styleable.CircularProgressBarWithTexts_progressDrawables, NOT_SET).let {
                if (it != NOT_SET) setProgressDrawables(it)
            }
            getColor(R.styleable.CircularProgressBarWithTexts_progressBarProgressColor, NOT_SET).let {
                if (it != NOT_SET) setProgressColor(it)
            }
            getColor(R.styleable.CircularProgressBarWithTexts_progressBarBackgroundColor, NOT_SET).let {
                if (it != NOT_SET) setBackgroundBarColor(it)
            }

            if (hasValue(R.styleable.CircularProgressBarWithTexts_progress)) {
                setProgress(getInt(R.styleable.CircularProgressBarWithTexts_progress, 0))
            }

            //text
            getString(R.styleable.CircularProgressBarWithTexts_innerTopText)?.let{
                setInnerTopText(it)
            }
            getString(R.styleable.CircularProgressBarWithTexts_innerBottomText)?.let{
                setInnerBottomText(it)
            }
            getString(R.styleable.CircularProgressBarWithTexts_outerBottomText)?.let{
                setOuterBottomText(it)
            }

            //styles
            getResourceId(R.styleable.CircularProgressBarWithTexts_innerTopTextStyle, NOT_SET).let {
                @StyleRes
                if (it != NOT_SET) setInnerTopTextStyle(it)
            }
            getResourceId(R.styleable.CircularProgressBarWithTexts_innerBottomTextStyle, NOT_SET).let {
                @StyleRes
                if (it != NOT_SET) setInnerBottomTextStyle(it)
            }
            getResourceId(R.styleable.CircularProgressBarWithTexts_outerBottomTextStyle, NOT_SET).let {
                @StyleRes
                if (it != NOT_SET) setOuterBottomTextStyle(it)
            }

            recycle()
        }
    }

    fun setStyle(@StyleRes styleId: Int) : CircularProgressBarWithTexts {
        this.styleId = styleId
        setAttrs(null)
        return this
    }

    //progress
    fun setProgressColor(@ColorInt progressColor: Int) : CircularProgressBarWithTexts {
        progressBar.applyProgressColor(progressColor)
        return this
    }

    fun setBackgroundBarColor(@ColorInt backgroundBarColor : Int) : CircularProgressBarWithTexts {
        progressBar.applyBackgroundColor(backgroundBarColor)
        return this
    }

    fun setProgressAndBackgroundColor(@ColorInt progressColor: Int, @ColorInt backgroundBarColor: Int) : CircularProgressBarWithTexts {
        progressBar.applyColors(progressColor,backgroundBarColor)
        return this
    }

    fun setProgress(progress : Int) : CircularProgressBarWithTexts {
        progressBar.progress = progress
        return this
    }

    /**
     * This function takes in a float value converts it to an int and sets it as the progress bar
     * progress. Values can range from 0f to 1f, anything higher than 1f will be capped to 1f
     * to set the progress to its max value of 100.
     */
    fun setProgress(progress: Float) : CircularProgressBarWithTexts {
        progressBar.progress = (progress * 100).toInt()
        return this
    }

    fun setProgressDrawables(@DrawableRes drawableId : Int) : CircularProgressBarWithTexts {
        progressBar.progressDrawable = ContextCompat.getDrawable(context, drawableId)
        return this
    }

    // text
    fun setInnerTopText(text : CharSequence) : CircularProgressBarWithTexts {
        innerTopTextView.text = text
        return this
    }

    fun setInnerBottomText(text : CharSequence) : CircularProgressBarWithTexts {
        innerBottomTextView.text = text
        return this
    }

    fun setOuterBottomText(text : CharSequence) : CircularProgressBarWithTexts {
        outerBottomTextView.text = text
        return this
    }
    
    // styles
    fun setInnerTopTextStyle(@StyleRes styleId : Int) : CircularProgressBarWithTexts {
        innerTopTextView.setStyle(styleId)
        return this
    }

    fun setInnerBottomTextStyle(@StyleRes styleId : Int) : CircularProgressBarWithTexts {
        innerBottomTextView.setStyle(styleId)
        return this
    }

    fun setOuterBottomTextStyle(@StyleRes styleId : Int) : CircularProgressBarWithTexts {
        outerBottomTextView.setStyle(styleId)
        return this
    }

    // visibility
    fun showInnerTopText(shouldShow: Boolean) : CircularProgressBarWithTexts {
        innerTopTextView.showOrRemove(shouldShow)
        return this
    }
    fun showInnerBottomText(shouldShow: Boolean) : CircularProgressBarWithTexts {
        innerBottomTextView.showOrRemove(shouldShow)
        return this
    }

    fun showOuterBottomText(shouldShow: Boolean) : CircularProgressBarWithTexts {
        outerBottomTextView.showOrRemove(shouldShow)
        return this
    }

    fun setInnerTopTextColor(@ColorInt color: Int) {
        innerTopTextView.setTextColor(color)
    }

    fun setInnerBottomTextColor(@ColorInt color: Int) {
        innerBottomTextView.setTextColor(color)
    }

    fun setOuterBottomTextColor(@ColorInt color: Int) {
        outerBottomTextView.setTextColor(color)
    }
}
