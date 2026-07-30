package ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.DeunaSDK
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.initNextAction
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.web_views.deuna.extensions.isValid
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.elements_widget.ElementsEvent
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.example.deuna_integration.BuildConfig

class DeunaEmbeddedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Implement your embedded Deuna payment UI here
        val orderToken = intent.getStringExtra("ORDER_TOKEN").orEmpty()
        val userToken = intent.getStringExtra("USER_TOKEN").orEmpty()

        setContent {

            val deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                //env variable
                publicApiKey = BuildConfig.DEUNA_PUBLIC_API_KEY
            )

            DeunaScreen(
                deunaSDK = deunaSDK,
                orderToken = orderToken,
                userToken = userToken,
                orderPrice = "6784.0",
                currencyCode = "MXN",
                onFinish = { finish() }

            )
        }
    }
}

@Composable
fun DeunaScreen(
    deunaSDK: DeunaSDK,
    orderToken: String,
    userToken: String,
    orderPrice: String,
    currencyCode: String,
    onFinish: () -> Unit = {}
){

    val deunaWidget = remember { mutableStateOf<DeunaWidget?>(null) }



    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(800.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    DeunaWidget(context).apply {
                        this.widgetConfiguration = ElementsWidgetConfiguration(
                            sdkInstance = deunaSDK,
                            hidePayButton = true,
                            orderToken = orderToken,
                            userToken = userToken, // For authenticated users
                            domain = BuildConfig.DEUNA_DOMAIN,
                            userInfo = UserInfo(
                                firstName = "John",
                                lastName = "Doe",
                               // email = "threeds@deuna.com"
                                email = "euterpe5@snakement.com"
                            ),
                            callbacks = ElementsCallbacks().apply {
                                onEventDispatch = { event , data ->
                                    Log.d("DeunaWidget", "Event: $event data: $data")

                                    // Method 1: String matching (recommended for checkout events)
                                    // This works because event.toString() will contain the event name
                                    val eventString = event.toString()
                                    if (eventString.contains("paymentMethodsStarted", ignoreCase = true)) {
                                        Log.d("DeunaWidget", "paymentMethodsStarted event caught! Data: $data")
                                        // Handle payment methods started event
                                    }

                                    // Method 2: Using when with string matching for multiple events
                                    when {
                                        eventString.contains("paymentMethodsStarted", ignoreCase = true) -> {
                                            Log.d("DeunaWidget", "Payment methods started")
                                        }
                                        eventString.contains("purchaseRejected", ignoreCase = true) -> {
                                            Log.d("DeunaWidget", "Purchase rejected")
                                        }
                                        eventString.contains("purchaseSuccess", ignoreCase = true) -> {
                                            Log.d("DeunaWidget", "Purchase successful")
                                        }
                                        else -> {
                                            Log.d("DeunaWidget", "Other event: $event")
                                        }
                                    }

                                    // Method 3: Direct enum comparison (for ElementsEvent types only)
                                    if (event == ElementsEvent.checkoutStarted) {
                                        Log.d("DeunaWidget", "Checkout started (ElementsEvent)")
                                    }

                                }
                                onSuccess = { data ->
                                    Log.d("DeunaWidget", "Success: ${data["metadata"]}")
                                    deunaWidget.value?.destroy()
                                    deunaSDK.close()
                                    onFinish()

                                }
                                onError = { error ->
                                    Log.e("DeunaWidget", "Error: $error")
                                }
                                onInstallmentSelected = { installmentData ->
                                    Log.d("DeunaWidget", "Installment selected: ${installmentData?.get("metadata")}")
                                }


                            },
                            widgetExperience = ElementsWidgetExperience( // optional
                                userExperience = ElementsWidgetExperience.UserExperience(
                                    showSavedCardFlow = true,
                                    defaultCardFlow = false
                                )
                            ),
                            behavior = mapOf("paymentMethods" to mapOf(
                                "creditCard" to mapOf(
                                    "splitPayments" to mapOf("maxCards" to 2)
                                    )
                                )
                            )
                        )

                        build()
                        deunaWidget.value = this
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                deunaWidget.value!!.isValid { it ->

                    Log.d("DeunaWidget", "Is form valid? $it")
                    if(it){
                        deunaWidget.value?.submit { result ->
                            Log.d(
                                "DeunaWidget",
                                "Submit result: ${result.status} - ${result.message}"

                            )
                        }
                    }

                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Payment")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            deunaWidget.value?.destroy()
            Log.d("DeunaWidget", "WebView resources cleaned up")
        }
    }
}