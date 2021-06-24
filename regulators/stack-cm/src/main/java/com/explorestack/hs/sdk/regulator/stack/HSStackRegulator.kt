package com.explorestack.hs.sdk.regulator.stack

import android.content.Context
import android.text.TextUtils
import com.explorestack.consent.*
import com.explorestack.consent.Consent.ShouldShow.TRUE
import com.explorestack.consent.ConsentForm.Builder
import com.explorestack.consent.exception.ConsentManagerException
import com.explorestack.hs.sdk.HSComponentCallback
import com.explorestack.hs.sdk.HSComponentParams
import com.explorestack.hs.sdk.HSRegulator

class HSStackRegulator : HSRegulator<Consent>(
    "Stack Consent Manager",
    BuildConfig.COMPONENT_SDK_VERSION,
    BuildConfig.COMPONENT_VERSION
) {

    private var _consentManager: ConsentManager? = null
    private val consentManager get() = _consentManager!!

    override fun start(
        context: Context,
        params: HSComponentParams,
        callback: HSComponentCallback,
    ) {
        if (TextUtils.isEmpty(params.appKey)) {
            callback.onFail(buildError("AppKey not provided"))
            return
        }
        _consentManager = ConsentManager.getInstance(context)
        // TODO: 21.06.2021 set extra data for tracking id
        // consentManager.setExtraData("track_id", params.trackId)
        consentManager.requestConsentInfoUpdate(
            params.appKey!!,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consent: Consent?) {
                    if (consentManager.shouldShowConsentDialog() == TRUE) {
                        showConsentDialog(context, callback)
                    } else {
                        if (consent == null) {
                            callback.onFail(buildError("Consent is null"))
                        } else {
                            callback.onFinished()
                        }
                    }
                }

                override fun onFailedToUpdateConsentInfo(exception: ConsentManagerException?) {
                    callback.onFail(buildError("Consent Update failed"))
                }
            }
        )
    }

    private fun showConsentDialog(context: Context, callback: HSComponentCallback) {
        var consentForm: ConsentForm? = null
        consentForm = Builder(context)
            .withListener(object : ConsentFormListener {
                override fun onConsentFormLoaded() {
                    // TODO: 21.05.2021
                    consentForm!!.showAsActivity()
                }

                override fun onConsentFormError(exception: ConsentManagerException) {
                    callback.onFail(buildError("Consent Form failed"))
                }

                override fun onConsentFormOpened() {
                }

                override fun onConsentFormClosed(consent: Consent) {
                    callback.onFinished()
                }
            })
            .build()
        consentForm.load()
    }

    override fun getConsent(): Consent? {
        return consentManager.consent
    }
}