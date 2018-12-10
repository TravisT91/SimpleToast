package com.engageft.fis.pscu.feature.palettebindings

import android.graphics.Color
import com.engageft.apptoolbox.view.ProductCardView
import com.engageft.fis.pscu.feature.branding.Palette
import com.ob.domain.lookup.branding.BrandingCard
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ProductCardViewBindings
 * </p>
 * Contains the bindings and extensions used to style the ProductCardView
 * </p>
 * Created by Travis Tkachuk 12/4/18
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */

fun ProductCardView.applyBranding(brandingCard: BrandingCard, cd: CompositeDisposable, onFailedToApplyBranding: ((e:Throwable) -> Unit)?) {
    val picasso = Picasso.Builder(context).memoryCache(true).build()
    //Using this rx solution we are able to apply the bitmap without having to worry about creating
    //a target and garbage collection, however if we want to set a placeholder image we must do so
    //in xml on the ProductCardView
    cd.add(Single.fromCallable { picasso.load(brandingCard.image).get() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                    { bitmap ->
                        this.setCardBackground(bitmap)
                        this.setCardTextColor(Color.parseColor(brandingCard.textColor))
                        this.setRibbonOkColor(Palette.primaryColor)
                        this.setRibbonNotOkColor(Palette.errorColor)
                    },
                    { e ->
                        onFailedToApplyBranding?.invoke(e)
                    }
            )
    )
}
