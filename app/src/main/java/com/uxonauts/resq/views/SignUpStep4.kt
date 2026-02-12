package com.uxonauts.resq.views

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.*

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpStep4(controller: AuthController, navController: NavController) {
    var expandedRelation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
            try {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        val nameParts = name.split(" ")
                        controller.ecFirstName = nameParts.firstOrNull() ?: ""
                        controller.ecLastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""
                    }

                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    if (idIndex != -1 && hasPhoneIndex != -1) {
                        val contactId = cursor.getString(idIndex)
                        val hasPhone = cursor.getInt(hasPhoneIndex) > 0

                        if (hasPhone) {
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )
                            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                                val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (phoneIndex != -1) {
                                    controller.ecPhone = phoneCursor.getString(phoneIndex).replace(" ", "").replace("-", "")
                                }
                                phoneCursor.close()
                            }
                        }
                    }
                    Toast.makeText(context, "Kontak berhasil dimuat", Toast.LENGTH_SHORT).show()
                }
                cursor?.close()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat kontak. Pastikan izin diberikan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            contactLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kontak diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Kontak Darurat", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tambahkan orang yang dapat dihubungi (Tidak Wajib).", textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))

            // Tampilkan Error (Misal: Nomor sendiri)
            controller.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        contactLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pilih dari Buku Telepon")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Nama Lengkap", fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.ecFirstName, onValueChange = { controller.ecFirstName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Depan") })
                OutlinedTextField(value = controller.ecLastName, onValueChange = { controller.ecLastName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Belakang") })
            }
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandedRelation,
                onExpandedChange = { expandedRelation = !expandedRelation },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = controller.ecRelation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hubungan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRelation) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedRelation,
                    onDismissRequest = { expandedRelation = false }
                ) {
                    controller.ecRelationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                controller.ecRelation = option
                                expandedRelation = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.ecPhone, onValueChange = { controller.ecPhone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(
                    onClick = { controller.addEmergencyContact() },
                    enabled = controller.isStep4ContactValid()
                ) {
                    Text(
                        "+ Tambahkan ke Daftar",
                        color = if(controller.isStep4ContactValid()) ResqBlue else TextGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (controller.savedContacts.isNotEmpty()) {
            item {
                Text("Daftar Kontak Tersimpan:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(controller.savedContacts) { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(ResqLightBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ResqBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(contact.namaLengkap, fontWeight = FontWeight.Bold)
                        Text("${contact.hubungan} - ${contact.noTelepon}", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = controller.termsAccepted, onCheckedChange = { controller.termsAccepted = it })
                Text("Saya menyetujui ", fontSize = 12.sp)
                Text("Syarat dan Ketentuan", fontSize = 12.sp, color = ResqBlue)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { controller.submitRegistration(navController) },
                // Aktif jika syarat dicentang dan tidak loading. Kontak tidak wajib.
                enabled = controller.termsAccepted && !controller.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue), shape = RoundedCornerShape(8.dp)
            ) {
                if (controller.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Mulai", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}