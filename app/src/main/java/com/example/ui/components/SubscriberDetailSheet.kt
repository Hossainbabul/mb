package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriberEntity
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuspended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriberDetailSheet(
    subscriber: SubscriberEntity,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit,
    onCollectPayment: () -> Unit,
    onPingLine: () -> Unit,
    onOpenCustomerPortal: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = subscriber.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscriber.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${subscriber.subscriberId} • ${subscriber.area}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = subscriber.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Due & Plan Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Plan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(subscriber.planName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Billing Cycle: Day ${subscriber.billingDay} of month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Outstanding Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "৳${subscriber.balanceDue.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (subscriber.balanceDue > 0) MaterialTheme.colorScheme.error else StatusActive
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permanent Self-Care Link Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customer Permanent Self-Care Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "https://titil.net/p/${subscriber.permanentLinkToken}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Customer Link", "https://titil.net/p/${subscriber.permanentLinkToken}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Link", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenCustomerPortal,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("preview_portal_button")
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Self-Care View", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Network Configuration & Telemetry
            Text("MikroTik & Fiber Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(label = "PPPoE Username", value = subscriber.pppoeUsername)
                    InfoRow(label = "PPPoE Secret", value = subscriber.pppoePassword)
                    InfoRow(label = "Framed IP", value = subscriber.ipAddress)
                    InfoRow(label = "OLT Node", value = subscriber.oltName)
                    InfoRow(label = "PON Port", value = subscriber.ponPort)
                    InfoRow(label = "ONU MAC", value = subscriber.onuMac)
                    InfoRow(
                        label = "Optical RX Power",
                        value = "${subscriber.opticalPowerDbm} dBm",
                        valueColor = if (subscriber.opticalPowerDbm > -24.0) StatusActive else StatusPending
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCollectPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_collect_payment_button")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Collect Bill")
                }

                val isSuspended = subscriber.status == "SUSPENDED"
                Button(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuspended) StatusActive else StatusSuspended
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_toggle_status_button")
                ) {
                    Icon(
                        imageVector = if (isSuspended) Icons.Default.PlayArrow else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSuspended) "Reactivate Line" else "Suspend Line")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onPingLine,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Diagnostic Ping & Signal Check")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
