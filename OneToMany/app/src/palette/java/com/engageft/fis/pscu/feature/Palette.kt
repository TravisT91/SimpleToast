package com.engageft.fis.pscu.feature

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.airbnb.paris.extensions.fontFamily
import com.airbnb.paris.extensions.lineSpacingExtra
import com.airbnb.paris.extensions.textSizeDp
import com.airbnb.paris.extensions.textViewStyle
import com.engageft.apptoolbox.R
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.OneToManyApplication
import com.ob.domain.lookup.branding.BrandingColorType
import com.ob.ws.dom.BasicResponse
import com.ob.ws.dom.BrandingInfoResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

    fun getBrandingWithToken(token : String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetAccountBrandingInfo(
                HashMap<String,String>().apply{ put("token",token)})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse){
                        applyBrandingInfo(it)
                    }
                }}

    fun getBrandingWithRefCode(refCode : String): Observable<BasicResponse> {
        return EngageService.getInstance().engageApiInterface.postGetBrandingInfoFromRefCode(
                HashMap<String,String>().apply{ put("refCode",refCode)})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isSuccess && it is BrandingInfoResponse){
                        applyBrandingInfo(it)
                    }
                }}

    fun applyBrandingInfo(brandingInfoResponse: BrandingInfoResponse){
        brandingInfoResponse.brandingInfo.apply {
            applyColors(colors)
            setFonts(font)
        }
    }

    fun applyColors(colors: MutableMap<BrandingColorType, String>) {
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

    fun setFonts(fontName: String){
        Palette.apply {
            val context = OneToManyApplication.sInstance.applicationContext
            when (fontName) {
                Palette.FontType.ARIAL.fontName -> run {
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
}