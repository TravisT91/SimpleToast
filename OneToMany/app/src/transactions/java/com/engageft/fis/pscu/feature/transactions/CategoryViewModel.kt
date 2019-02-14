package com.engageft.fis.pscu.feature.transactions

import androidx.lifecycle.MutableLiveData
import com.engageft.engagekit.BudgetCategory
import com.engageft.engagekit.EngageService
import com.engageft.fis.pscu.feature.BaseEngageViewModel

/**
 * TODO: Class Name
 * </p>
 * TODO: Class Description
 * </p>
 * Created by Travis Tkachuk 1/30/19
 * Copyright (c) 2019 Engage FT. All rights reserved.
 */
class CategoryViewModel: BaseEngageViewModel() {

    val categories = MutableLiveData<List<BudgetCategory>>().apply {
        value = EngageService.getInstance().storageManager.budgetCategories
    }
}