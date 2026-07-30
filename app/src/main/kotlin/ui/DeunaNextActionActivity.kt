package ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deuna.maven.DeunaSDK
import com.deuna.maven.initNextAction
import com.deuna.maven.shared.Environment
import com.deuna.maven.shared.Json
import com.deuna.maven.widgets.checkout_widget.CheckoutEvent
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.payment_widget.OnEventDispatch
import com.example.deuna_integration.BuildConfig
import kotlin.time.measureTime

class DeunaNextActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderToken = intent.getStringExtra("ORDER_TOKEN").orEmpty()

        setContent {
            val deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = BuildConfig.DEUNA_PUBLIC_API_KEY
            )

            NextActionWidget(
                deunaSDK = deunaSDK,
                orderToken = orderToken
            )
        }
    }
}

@Composable
fun NextActionWidget(
    deunaSDK: DeunaSDK,
    orderToken: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                deunaSDK.initNextAction(
                    context = context,
                    orderToken = orderToken,
                    domain = "https://volaris.pay.sandbox.deuna.com",
                    callbacks = NextActionCallbacks().apply {
                        onSuccess = { nextActionData ->
                            Log.d("DeunaWidget", "Next action success: ${nextActionData["metadata"]}")
                        }
                        onError = { error ->
                            Log.e("DeunaWidget", "---------->Next action error: ${error.metadata}")
                            //deunaSDK.close()
                        }
                        onClosed = { data ->
                            Log.e("DeunaWidget", "---------->Next action closed $data")
                            //deunaSDK.close()
                        }
                        this.onEventDispatch = { event, data ->
                            Log.d("DeunaWidget", "Next action event: ${data["metadata"]}")
                        }
                    }
                )
                android.widget.FrameLayout(context)
            }
        )
    }
}

