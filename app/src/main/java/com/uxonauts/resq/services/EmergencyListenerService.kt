package com.uxonauts.resq.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.uxonauts.resq.MainActivity
import com.uxonauts.resq.R

class EmergencyListenerService : Service() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerReg: ListenerRegistration? = null
    private var previousIds = setOf<String>()
    private var isFirstSnapshot = true

    companion object {
        const val FOREGROUND_CHANNEL = "emergency_listener_channel"
        const val ALERT_CHANNEL = "emergency_alert_channel"
        const val FOREGROUND_ID = 3001
        const val ALERT_ID_BASE = 4000
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(FOREGROUND_ID, buildForegroundNotification())
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val foregroundChannel = NotificationChannel(
                FOREGROUND_CHANNEL,
                "Layanan Kontak Darurat",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Memantau notifikasi darurat dari keluarga/teman"
            }
            nm.createNotificationChannel(foregroundChannel)
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL,
                "Kontak Darurat SOS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi saat kontak darurat Anda butuh bantuan"
                enableVibration(true)
                enableLights(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            nm.createNotificationChannel(alertChannel)
        }
    }

    private fun buildForegroundNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL)
            .setContentTitle("RESQ Aktif")
            .setContentText("Memantau kontak darurat")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startListening() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            stopSelf()
            return
        }
        listenerReg = db.collection("emergency_notifications")
            .whereEqualTo("targetUserId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EmergencyListener", "Listen error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val currentIds = snapshot.documents.map { it.id }.toSet()
                if (isFirstSnapshot) {
                    previousIds = currentIds
                    isFirstSnapshot = false
                    return@addSnapshotListener
                }

                val newIds = currentIds - previousIds
                if (newIds.isNotEmpty()) {
                    newIds.forEach { id ->
                        val doc = snapshot.documents.find { it.id == id }
                        if (doc != null) {
                            val isRead = doc.getBoolean("read") ?: false
                            if (!isRead) {
                                showAlertNotification(
                                    notifId = id,
                                    senderName = doc.getString("senderName") ?: "Kontak Darurat",
                                    category = doc.getString("category") ?: "SOS",
                                    address = doc.getString("address") ?: "-",
                                    hubungan = doc.getString("hubungan") ?: ""
                                )
                            }
                        }
                    }
                }
                previousIds = currentIds
            }
    }

    private fun showAlertNotification(
        notifId: String,
        senderName: String,
        category: String,
        address: String,
        hubungan: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_emergency_notif_id", notifId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notifId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "🆘 $senderName butuh bantuan!"
        val bigText = buildString {
            append("$senderName")
            if (hubungan.isNotEmpty()) append(" ($hubungan)")
            append(" menekan tombol SOS!\n\n")
            append("Kategori: $category\n")
            append("Lokasi: $address")
        }

        val notif = NotificationCompat.Builder(this, ALERT_CHANNEL)
            .setContentTitle(title)
            .setContentText("Kategori: $category")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ALERT_ID_BASE + notifId.hashCode().and(0xFFFF), notif)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}