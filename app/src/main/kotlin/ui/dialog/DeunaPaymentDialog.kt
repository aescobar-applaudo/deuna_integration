package ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import com.deuna.maven.DeunaSDK
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.generateFraudId
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.example.deuna_integration.BuildConfig
import com.example.deuna_integration.R

class DeunaPaymentDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.deuna_webview_cont) // Create this layout as needed

        // Clear WebView cache
        val webView = dialog.findViewById<WebView>(R.id.deunaWebView)
        webView.clearCache(true)

        // Initialize checkout (customize params as needed)
        val deunaSDK = DeunaSDK(
            environment = Environment.SANDBOX,
            publicApiKey = BuildConfig.DEUNA_PUBLIC_API_KEY
        )

        var fraudId: String? = null

        deunaSDK.generateFraudId(
            context = requireContext(),
            params = mapOf(
                "ACCERTIFY" to mapOf(
                "src" to "sr",
                "id" to "id",
                "dvc" to "dvc",
                "collectorHost" to "collectorHost",
                "sid" to "sid"
                ),
                "RISKIFIED" to mapOf(
                    "storeDomain" to ""
                )
            ),
            callback = { fraudIdResult ->
                Log.e("DeunaPaymentDialog", "Fraud ID: $fraudIdResult")
                fraudId = fraudIdResult
            }
        )

        deunaSDK.initElements(
            context = requireContext(),
            orderToken = "your-order-token-here", //call /api/v1/deuna/order
            //required if user is registered
            //userToken = "user-token-here", //call /api/v1/deuna/session

            //Required if userToken is not provided / anonymous user
            userInfo = UserInfo(
                firstName= "John",
                lastName= "Doe",
                email= "j.doe@mail.com"),

            callbacks = ElementsCallbacks().apply {
                onSuccess = { data ->
                    Log.d("DeunaPaymentDialog", "onSuccess data: $data")
                    Log.d("DeunaPaymentDialog", "Fraud ID used: $fraudId")
                    deunaSDK.close() // Close the DialogFragment of the payment widget
                    // Your additional code
                }

                onError = { error ->
                    // Error handling
                    Log.e("DeunaPaymentDialog", "onError: $error")

                }
                onClosed = { action ->
                    // Widget closed
                }
            },
            widgetExperience = ElementsWidgetExperience( // optional
                userExperience = ElementsWidgetExperience.UserExperience(
                    showSavedCardFlow = false, // optional
                    defaultCardFlow = true // optional
                )
            ),
            domain= BuildConfig.DEUNA_DOMAIN,
            language = "en",


        )
        return dialog
    }
}