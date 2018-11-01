package com.engageft.feature

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.paris.extensions.fontFamily
import com.airbnb.paris.extensions.lineSpacingExtra
import com.airbnb.paris.extensions.style
import com.airbnb.paris.extensions.textSizeDp
import com.airbnb.paris.extensions.textViewStyle
import com.airbnb.paris.styles.Style
import com.engageft.apptoolbox.R
import com.engageft.apptoolbox.view.PillButton

object ThemeUtils {
    private const val NOT_SET = 0

    //COLORS
    @ColorInt
    var primaryColor: Int = NOT_SET
    @ColorInt
    var secondaryColor: Int = NOT_SET
    @ColorInt
    var successColor: Int = NOT_SET
    @ColorInt
    var warningColor: Int = NOT_SET
    @ColorInt
    var errorColor: Int = NOT_SET
    @ColorInt
    var infoColor: Int = NOT_SET

    enum class FontType(val fontName: String){
        ARIAL("Arial")
    }

    //FONTS
    var typefaceName: String? = null
    var font_bold: Typeface? = null
    var font_italic: Typeface? = null
    var font_light: Typeface? = null
    var font_medium: Typeface? = null
    var font_regular: Typeface? = null

    //IMAGES
    var cardImageUrl: String? = null

    //TEXT STYLES
    var Title4Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_light?.let { fontFamily(it) }
            }
        }

    var Title2Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_light?.let { fontFamily(it) }
            }
        }

    var LargeTitle = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_regular?.let { fontFamily(it) }
            }
        }

    var BodyQuiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(16)
                lineSpacingExtra(12)
                font_light?.let { fontFamily(it) }
            }
        }

    var LargeTitleLoud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title3Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption1Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_light?.let { fontFamily(it) }
            }
        }

    var  Title1Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption2Quiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_light?.let { fontFamily(it) }
            }
        }

    var Title1 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title2 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title3 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title4 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_regular?.let { fontFamily(it) }
            }
        }

    var FootnoteQuiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_light?.let { fontFamily(it) }
            }
        }

    var Body = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(16)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Footnote = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Caption1 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Caption2 = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title1Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title2Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title4Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_bold?.let { fontFamily(it) }
            }
        }

    var BodyLoud = textViewStyle { }
        get() {
            return textViewStyle {
                font_bold?.let { fontFamily(it) }
                textSizeDp(16)
            }
        }

    var LargeTitleQuiet = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption1Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title3Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Caption2Loud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_bold?.let { fontFamily(it) }
            }
        }

    var FootnoteLoud = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_bold?.let { fontFamily(it) }
            }
        }

    var FootnoteMedium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title1Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_medium?.let { fontFamily(it) }
            }
        }

    var LargeTitleMedium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_medium?.let { fontFamily(it) }
            }
        }

    var BodyMedium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(16)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title3Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Caption1Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Caption2Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title2Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title4Medium = textViewStyle { }
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_medium?.let { fontFamily(it) }
            }
        }

//    fun updateWithAccountPropertiesResponse(accountPropertiesResponse: AccountPropertiesResponse?) {
//        accountPropertiesResponse?.apply {
//            if (primaryColor != 0) this@ThemeUtils.primaryColor = primaryColor
//            if (secondaryColor != 0) this@ThemeUtils.secondaryColor = secondaryColor
//            if (successColor != 0) this@ThemeUtils.successColor = successColor
//            if (warningColor != 0) this@ThemeUtils.warningColor = warningColor
//            if (errorColor != 0) this@ThemeUtils.errorColor = errorColor
//            if (infoColor != 0) this@ThemeUtils.infoColor = infoColor
//            if (!cardImageUrl.isNullOrEmpty()) this@ThemeUtils.cardImageUrl = cardImageUrl
//            if (!typefaceName.isNullOrEmpty()) this@ThemeUtils.typefaceName = typefaceName
//
//            updateStyles()
//        }
//        //TODO uncomment when we can retrieve the response from backend
//    }

}

@BindingAdapter("parisStyle", requireAll = true)
fun TextView.setParisStyle(style: Style?){
    style?.let {
        this.style(it)
    }
}

@BindingAdapter("switchButtonTint",requireAll = true)
fun Switch.setButtonTint(@ColorInt color: Int){
    this.thumbDrawable.setColorFilter(color,PorterDuff.Mode.MULTIPLY)
    this.trackDrawable.setColorFilter(color,PorterDuff.Mode.MULTIPLY)
}

@BindingAdapter("isFilled", requireAll = true)
fun PillButton.setFilled(isFilled: Boolean){
    if (isFilled){
        this.setColorStateList(
                fillPressed = ThemeUtils.secondaryColor,
                strokePressed = ThemeUtils.secondaryColor,
                fillEnabled = ThemeUtils.primaryColor,
                strokeEnabled = ThemeUtils.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, R.color.structure2),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = ContextCompat.getColor(this.context, R.color.white),
                        enabledColor = ContextCompat.getColor(this.context, R.color.white),
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    } else {
        this.setColorStateList(
                fillPressed = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokePressed = ThemeUtils.secondaryColor,
                fillEnabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeEnabled = ThemeUtils.primaryColor,
                fillDisabled = ContextCompat.getColor(this.context, android.R.color.transparent),
                strokeDisabled = ContextCompat.getColor(this.context, R.color.structure2))
        this.setTextColor(
                getTextStateList(
                        pressedColor = ThemeUtils.secondaryColor,
                        enabledColor = ThemeUtils.primaryColor,
                        disabledColor = ContextCompat.getColor(this.context, R.color.structure4)))
    }
}

fun getTextStateList(@ColorInt pressedColor : Int, @ColorInt disabledColor : Int, @ColorInt enabledColor: Int) : ColorStateList {
    val s = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))
    val c = intArrayOf(pressedColor, disabledColor,enabledColor)
    return ColorStateList(s,c)
}