package com.engageft.fis.pscu.feature

import androidx.lifecycle.MutableLiveData

/**
 * TODO(joeyhutchins): ClassName
 * <p>
 * TODO(joeyhutchins): Class description.
 * </p>
 * Created by joeyhutchins on 12/13/18.
 * Copyright (c) 2018 Engage FT. All rights reserved.
 */
class EnrollmentViewModel : BaseEngageViewModel() {
    val getStartedDelegate = GetStartedDelegate()
    var cardPinDelegate: EnrollmentCardPinDelegate? = null
    var createAccountDelegate: CreateAccountDelegate? = null
    var verifyIdentityDelegate: VerifyIdentityDelegate? = null
    var termsOfUseDelegate: TermsOfUseDelegate? = null

    val progressMaxObservable = MutableLiveData<Int>()
    val progressCurrentObservable = MutableLiveData<Int>()

    inner class GetStartedDelegate {
        init {

        }
    }

    inner class EnrollmentCardPinDelegate {
        init {

        }
    }

    inner class CreateAccountDelegate {
        init {

        }
    }

    inner class VerifyIdentityDelegate {
        init {

        }
    }

    inner class TermsOfUseDelegate {
        init {

        }
    }

}
