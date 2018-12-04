package com.engageft.fis.pscu.feature.branding

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.airbnb.paris.extensions.fontFamily
import com.airbnb.paris.extensions.lineSpacingExtra
import com.airbnb.paris.extensions.textSizeDp
import com.airbnb.paris.extensions.textViewStyle
import com.engageft.apptoolbox.R
import com.engageft.fis.pscu.OneToManyApplication
import com.ob.domain.lookup.branding.BrandingColorType
import com.ob.domain.lookup.branding.BrandingInfo


object Palette {
    private const val NOT_SET = 0

    //COLORS
    @ColorInt
    var primaryColor: Int = NOT_SET
        private set
    @ColorInt
    var secondaryColor: Int = NOT_SET
        private set
    @ColorInt
    var successColor: Int = NOT_SET
        private set
    @ColorInt
    var warningColor: Int = NOT_SET
        private set
    @ColorInt
    var errorColor: Int = NOT_SET
        private set
    @ColorInt
    var infoColor: Int = NOT_SET
        private set

    enum class FontType(val fontName: String){
        ARIAL("Arial")
    }

    //FONTS
    var font_bold: Typeface? = null
        private set
    var font_italic: Typeface? = null
        private set
    var font_light: Typeface? = null
        private set
    var font_medium: Typeface? = null
        private set
    var font_regular: Typeface? = null
        private set

    @Suppress("unused")
//IMAGES
    var cardImageUrl: String? = null

    //TEXT STYLES
    var Title4Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_light?.let { fontFamily(it) }
            }
        }

    var Title2Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_light?.let { fontFamily(it) }
            }
        }

    var LargeTitle = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_regular?.let { fontFamily(it) }
            }
        }

    var BodyQuiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(16)
                lineSpacingExtra(12)
                font_light?.let { fontFamily(it) }
            }
        }

    var LargeTitleLoud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title3Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption1Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_light?.let { fontFamily(it) }
            }
        }

    var  Title1Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption2Quiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_light?.let { fontFamily(it) }
            }
        }

    var Title1 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title2 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title3 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title4 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_regular?.let { fontFamily(it) }
            }
        }

    var FootnoteQuiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_light?.let { fontFamily(it) }
            }
        }

    var Body = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(16)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Footnote = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Caption1 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Caption2 = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_regular?.let { fontFamily(it) }
            }
        }

    var Title1Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title2Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title4Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_bold?.let { fontFamily(it) }
            }
        }

    var BodyLoud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                font_bold?.let { fontFamily(it) }
                textSizeDp(16)
            }
        }

    var LargeTitleQuiet = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_light?.let { fontFamily(it) }
            }
        }

    var Caption1Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Title3Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_bold?.let { fontFamily(it) }
            }
        }

    var Caption2Loud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_bold?.let { fontFamily(it) }
            }
        }

    var FootnoteLoud = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_bold?.let { fontFamily(it) }
            }
        }

    var FootnoteMedium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(14)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title1Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(32)
                font_medium?.let { fontFamily(it) }
            }
        }

    var LargeTitleMedium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(38)
                font_medium?.let { fontFamily(it) }
            }
        }

    var BodyMedium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(16)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title3Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(20)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Caption1Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(12)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Caption2Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(10)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title2Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(28)
                font_medium?.let { fontFamily(it) }
            }
        }

    var Title4Medium = textViewStyle { }
        private set
        get() {
            return textViewStyle {
                textSizeDp(18)
                font_medium?.let { fontFamily(it) }
            }
        }

    fun applyBrandingInfo(brandingInfo: BrandingInfo){
        applyColors(brandingInfo.colors)
        setFontsFromString(brandingInfo.font)
    }

    private fun applyColors(colors: MutableMap<BrandingColorType, String>) {
        colors.apply {
            this.forEach {
                val color = Color.parseColor(it.value)
                when(it.key){
                    BrandingColorType.SUCCESS -> successColor = color
                    BrandingColorType.WARNING -> warningColor = color
                    BrandingColorType.ERROR -> errorColor = color
                    BrandingColorType.INFO -> infoColor = color
                    BrandingColorType.PRIMARY -> primaryColor = color
                    BrandingColorType.SECONDARY -> secondaryColor = color
                }
            }
        }
    }

    private fun setFontsFromString(fontName: String){
        Palette.apply {
            val context = OneToManyApplication.sInstance.applicationContext
            when (fontName) {
                FontType.ARIAL.fontName -> run {
                    font_regular = ResourcesCompat.getFont(context, R.font.font_regular)
                    font_bold = ResourcesCompat.getFont(context, R.font.font_bold)
                    font_italic = ResourcesCompat.getFont(context, R.font.font_italic)
                    font_light = ResourcesCompat.getFont(context, R.font.font_light)
                    font_medium = ResourcesCompat.getFont(context, R.font.font_medium)
                }
                else -> run { }
            }
        }
    }

    fun setPaletteColors(
            primaryColor : Int,
            secondaryColor : Int,
            successColor : Int,
            warningColor : Int,
            errorColor : Int,
            infoColor : Int){
        Palette.primaryColor = primaryColor
        Palette.secondaryColor = secondaryColor
        Palette.successColor = successColor
        Palette.warningColor = warningColor
        Palette.errorColor = errorColor
        Palette.infoColor = infoColor
    }

    fun setFonts(
            font_regular: Typeface?,
            font_bold: Typeface?,
            font_italic: Typeface?,
            font_light: Typeface?,
            font_medium: Typeface?) {
        Palette.font_regular = font_regular
        Palette.font_bold = font_bold
        Palette.font_italic = font_italic
        Palette.font_light = font_light
        Palette.font_medium = font_medium
    }
}