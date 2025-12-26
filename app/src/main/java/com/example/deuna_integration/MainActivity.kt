package com.example.deuna_integration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ui.DeunaEmbeddedActivity
import ui.DeunaEmbeddedCardVaultActivity
import ui.dialog.DeunaPaymentDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val showDeunaDialogButton = findViewById<Button>(R.id.showDeunaDialogButton)
        showDeunaDialogButton.setOnClickListener {
            DeunaPaymentDialog().show(supportFragmentManager, "DeunaPaymentDialog")

        }

        val embeddedButton: Button = findViewById(R.id.showDeunaEmbeddedButton)

        embeddedButton.setOnClickListener {
            val intent = Intent(this, DeunaEmbeddedActivity::class.java).apply {
                putExtra("ORDER_TOKEN", BuildConfig.DEUNA_ORDER_TOKEN)
                //putExtra("USER_TOKEN", "your-user-token-here")
            }
            startActivity(intent)
        }

        val embeddedCardVaultButton: Button = findViewById(R.id.showDeunaEmbeddedCardVaultButton)

        embeddedCardVaultButton.setOnClickListener {
            val intent = Intent(this, DeunaEmbeddedCardVaultActivity::class.java).apply {
                //putExtra("ORDER_TOKEN", "your-order-token-here")
                putExtra("USER_TOKEN", BuildConfig.DEUNA_USER_TOKEN)
            }
            startActivity(intent)
        }
    }
}