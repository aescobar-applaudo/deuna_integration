package ui

import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.DeunaSDK
import com.deuna.maven.ElementsWidgetExperience
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.deuna.DeunaWidget
import com.deuna.maven.web_views.deuna.extensions.build
import com.deuna.maven.web_views.deuna.extensions.isValid
import com.deuna.maven.web_views.deuna.extensions.submit
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.example.deuna_integration.BuildConfig

class DeunaEmbeddedCardVaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implement your embedded Deuna payment UI here
        //val orderToken = intent.getStringExtra("ORDER_TOKEN").orEmpty()
        val userToken = intent.getStringExtra("USER_TOKEN").orEmpty()

        setContent {

            val deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = BuildConfig.DEUNA_PUBLIC_API_KEY
            )

            CardVaultScreen(
                deunaSDK = deunaSDK,
                //orderToken = orderToken,
                userToken = userToken,
                orderPrice = "196.0",
                currencyCode = "USD"

            )
        }
    }
}

@Composable
fun CardVaultScreen(
    deunaSDK: DeunaSDK,
    //orderToken: String,
    userToken: String,
    orderPrice: String,
    currencyCode: String
){
    val deunaWidget = remember { mutableStateOf<DeunaWidget?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(900.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    DeunaWidget(context).apply {
                        this.widgetConfiguration = ElementsWidgetConfiguration(
                            sdkInstance = deunaSDK,
                            //no order token needed for card vault
                            domain = BuildConfig.DEUNA_DOMAIN,
                            //userToken = userToken,
                            hidePayButton = true,
                            userInfo = UserInfo(
                                firstName= "John",
                                lastName= "Doe",
                              email= "john.doe26@mail.com"),
                            callbacks = ElementsCallbacks().apply {
                                onEventDispatch = { event, data ->
                                    Log.d("DeunaWidget", "Event: $event data: ${data["metadata"]}")
                                }
                                onSuccess = { data ->
                                    Log.d("DeunaWidget", "Success:${data["metadata"]}")

                                }
                                onError = { error ->
                                    Log.e("DeunaWidget", "Error: $error")
                                }
                            },
                            widgetExperience = ElementsWidgetExperience( // optional
                                userExperience = ElementsWidgetExperience.UserExperience(
                                    showSavedCardFlow = false,
                                    defaultCardFlow = true
                                )
                            ),
                           behavior = mapOf(
                                "orderPrice" to "",
                                "currencyCode" to "currencyCode"
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
                }
                deunaWidget.value?.submit { result ->
                    Log.d(
                        "DeunaWidget",
                        "Submit result: ${result.status} - ${result.message}"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save credit card")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            deunaWidget.value?.destroy()
            Log.d("DeunaWidget", "WebView resources cleaned up")
        }
    }
}