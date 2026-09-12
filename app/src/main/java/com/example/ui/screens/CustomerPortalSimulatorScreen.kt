package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriberEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.StatusActive
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPortalSimulatorScreen(
    subscribers: List<SubscriberEntity>,
    selectedSubscriber: SubscriberEntity?,
    onSelectSubscriber: (SubscriberEntity) -> Unit,
    onSimulateCustomerPayment: (subId: Int, amount: Double, method: String, trxId: String) -> Unit
) {
    var currentSub by remember(selectedSubscriber, subscribers) {
        mutableStateOf(selectedSubscriber ?: subscribers.firstOrNull())
    }
    var expandedSubDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("customer_portal_simulator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer Selector Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Customer Self-Care Web Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Permanent Customer Link (`/p/{token}`) resolves tenant + subscriber data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSubDropdown,
                    onExpandedChange = { expandedSubDropdown = it }
                ) {
                    OutlinedTextField(
                        value = currentSub?.let { "${it.name} • Due ৳${it.balanceDue.toInt()}" } ?: "Choose Client",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubDropdown,
                        onDismissRequest = { expandedSubDropdown = false }
                    ) {
                        subscribers.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text("${sub.name} (${sub.pppoeUsername}) • ৳${sub.balanceDue.toInt()}") },
                                onClick = {
                                    currentSub = sub
                                    onSelectSubscriber(sub)
                                    expandedSubDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // The Customer Portal Phone Canvas
        val sub = currentSub
        if (sub != null) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("portal_customer_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Portal Branding Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("TITIL.NET", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                                Text("Madhabpur Customer Self-Care", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusBadge(status = sub.status)
                    }

                    HorizontalDivider()

                    // Customer Greeting & Connection Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Welcome Back,", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Client ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(sub.subscriberId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Package", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(sub.planName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PPPoE Login", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(sub.pppoeUsername, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Payable Bill Section
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Current Statement & Billing", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Internet Fee", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${sub.balanceDue.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Government VAT (5%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Included", fontSize = 13.sp, color = StatusActive)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Late Fee / Penance", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳0", fontSize = 13.sp)
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "৳${sub.balanceDue.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = if (sub.balanceDue > 0) MaterialTheme.colorScheme.error else StatusActive
                                )
                            }
                        }
                    }

                    // Instant Checkout Options
                    if (sub.balanceDue > 0) {
                        Text(
                            text = "Instant Payment Gateways",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // bKash Button
                            Button(
                                onClick = {
                                    val trx = "BK" + UUID.randomUUID().toString().take(8).uppercase()
                                    onSimulateCustomerPayment(sub.id, sub.balanceDue, "bKash Merchant", trx)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bkash_pay_button")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay bKash", fontWeight = FontWeight.Bold)
                            }

                            // Nagad Button
                            Button(
                                onClick = {
                                    val trx = "NG" + UUID.randomUUID().toString().take(8).uppercase()
                                    onSimulateCustomerPayment(sub.id, sub.balanceDue, "Nagad", trx)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NagadOrange),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nagad_pay_button")
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay Nagad", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StatusActive.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActive)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("All Bills Cleared!", fontWeight = FontWeight.Bold, color = StatusActive)
                                    Text("Your high-speed broadband line is active and healthy.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // ISP Contact Footer
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("24/7 Helpline: 01711-223344 • Titil Bazar, Madhabpur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Madhabpur Titil Dot Net • Powered by MikroTik & Huawei GPON", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
