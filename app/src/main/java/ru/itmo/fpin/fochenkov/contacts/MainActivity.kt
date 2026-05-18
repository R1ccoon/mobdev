package ru.itmo.fpin.fochenkov.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import ru.itmo.fpin.fochenkov.contacts.ui.ContactsScreen

class MainActivity : ComponentActivity() {

    private val readContactsPermission = Manifest.permission.READ_CONTACTS

    private var hasContactsPermission by mutableStateOf(false)

    private val requestContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasContactsPermission = isGranted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasContactsPermission = ContextCompat.checkSelfPermission(
            this,
            readContactsPermission
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            ContactsScreen(
                hasPermission = hasContactsPermission,
                onRequestPermission = {
                    requestContactsPermissionLauncher.launch(readContactsPermission)
                }
            )
        }
    }
}