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

object Palette {
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
//            if (primaryColor != 0) this@Palette.primaryColor = primaryColor
//            if (secondaryColor != 0) this@Palette.secondaryColor = secondaryColor
//            if (successColor != 0) this@Palette.successColor = successColor
//            if (warningColor != 0) this@Palette.warningColor = warningColor
//            if (errorColor != 0) this@Palette.errorColor = errorColor
//            if (infoColor != 0) this@Palette.infoColor = infoColor
//            if (!cardImageUrl.isNullOrEmpty()) this@Palette.cardImageUrl = cardImageUrl
//            if (!typefaceName.isNullOrEmpty()) this@Palette.typefaceName = typefaceName
//
//            updateStyles()
//        }
//        //TODO uncomment when we can retrieve the response from backend
//    }

}