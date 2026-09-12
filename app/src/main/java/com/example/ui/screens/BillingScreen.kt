package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.data.model.PaymentTransactionEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    invoices: List<InvoiceEntity>,
    payments: List<PaymentTransactionEntity>,
    onCollectPayment: () -> Unit,
    onRunBillingCycle: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Invoices, 1: Payments

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCollectPayment,
                icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                text = { Text("Collect Bill") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_collect_bill")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("billing_screen")
        ) {
            // Header with Billing Cycle Action
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automated Monthly Billing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Calculate Plan + Due + Tax for active users", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onRunBillingCycle,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("run_cycle_button")
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Cycle", fontSize = 12.sp)
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedSubTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Invoices (${invoices.size})") }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Payment Receipts (${payments.size})") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedSubTab == 0) {
                // Invoices List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(invoices, key = { it.id }) { inv ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${inv.subscriberName} (${inv.pppoeUsername})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(status = inv.status)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Billing Period: ${inv.monthYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Due Date: ${inv.dueDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Plan: ৳${inv.planPrice.toInt()} + Tax: ৳${inv.taxAmount.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("৳${inv.totalPayable.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        if (inv.paidAmount > 0) {
                                            Text("Paid: ৳${inv.paidAmount.toInt()}", fontSize = 11.sp, color = StatusActive)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Payments List
                val timeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(payments, key = { it.id }) { pay ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
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
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pay.subscriberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${pay.paymentMethod} • Trx: ${pay.providerTxnId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("By: ${pay.collectedBy} • ${timeFormat.format(Date(pay.timestamp))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("+৳${pay.amount.toInt()}", fontWeight = FontWeight.Bold, color = StatusActive, fontSize = 16.sp)
                                    Surface(
                                        color = StatusActive.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = pay.receiptNumber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusActive,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
