package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.SubscriberEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.DashboardMetrics
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    recentPayments: List<PaymentTransactionEntity>,
    subscribers: List<SubscriberEntity>,
    onNavigateTab: (AppTab) -> Unit,
    onAddSubscriber: () -> Unit,
    onCollectPayment: () -> Unit,
    onRunBillingCycle: () -> Unit,
    onSelectSubscriber: (SubscriberEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0369A1), Color(0xFF0A2540))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MADHABPUR TITIL DOT NET",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Broadband Operations Core",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            color = StatusActive.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusActive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MikroTik Sync OK",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Outstanding Due", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(
                                text = "৳${metrics.totalOutstandingDue.toInt()}",
                                color = Color(0xFFFDE047),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Month Collection", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(
                                text = "৳${metrics.totalCollectedRevenue.toInt()}",
                                color = Color(0xFF6EE7B7),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Quick Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCollectPayment,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_collect_bill_button")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Collect Bill", fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = onAddSubscriber,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_sub_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Client", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onRunBillingCycle,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_billing_cycle_button")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Cycle", fontSize = 12.sp)
                }
            }
        }

        // 4 KPI Cards
        item {
            Text(
                text = "Network & Subscriber Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Active Lines",
                    value = "${metrics.activeSubscribers}",
                    subtitle = "${metrics.totalSubscribers} Total Clients",
                    icon = Icons.Default.Wifi,
                    iconTint = StatusActive,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Suspended",
                    value = "${metrics.suspendedSubscribers}",
                    subtitle = "Payment Due",
                    icon = Icons.Default.Block,
                    iconTint = StatusSuspended,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "PPPoE Sessions",
                    value = "${metrics.activePppoeSessions}",
                    subtitle = "CCR1036 Core",
                    icon = Icons.Default.Router,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Revenue",
                    value = "৳${metrics.totalCollectedRevenue.toInt()}",
                    subtitle = "Reconciled",
                    icon = Icons.Default.AccountBalance,
                    iconTint = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Client Quick Attention List (Clients with Due / Suspended)
        item {
            val dueSubscribers = subscribers.filter { it.balanceDue > 0 || it.status == "SUSPENDED" }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requires Attention (${dueSubscribers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateTab(AppTab.SUBSCRIBERS) }) {
                    Text("View All")
                }
            }
            if (dueSubscribers.isEmpty()) {
                Text(
                    text = "All subscriber accounts in Madhabpur are clear and active!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val dueSubs = subscribers.filter { it.balanceDue > 0 || it.status == "SUSPENDED" }.take(3)
        items(dueSubs) { sub ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSubscriber(sub) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sub.name, fontWeight = FontWeight.Bold)
                        Text("${sub.pppoeUsername} • ${sub.area}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Due: ৳${sub.balanceDue.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        StatusBadge(status = sub.status)
                    }
                }
            }
        }

        // Recent Payments Stream
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateTab(AppTab.BILLING) }) {
                    Text("All Payments")
                }
            }
        }

        items(recentPayments.take(4)) { pay ->
            val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor = when {
                        pay.paymentMethod.contains("bKash") -> BkashPink
                        pay.paymentMethod.contains("Nagad") -> NagadOrange
                        pay.paymentMethod.contains("Cash") -> StatusActive
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Surface(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pay.subscriberName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${pay.paymentMethod} • ${pay.providerTxnId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeFormat.format(Date(pay.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+৳${pay.amount.toInt()}", fontWeight = FontWeight.Bold, color = StatusActive, fontSize = 15.sp)
                        Text(pay.receiptNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
