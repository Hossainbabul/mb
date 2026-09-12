package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PlanEntity
import com.example.data.model.SubscriberEntity
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.StatusActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCollectionDialog(
    subscriber: SubscriberEntity?,
    allSubscribers: List<SubscriberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (subId: Int, invoiceId: Int?, amount: Double, method: String, trxId: String) -> Unit
) {
    var selectedSub by remember { mutableStateOf(subscriber ?: allSubscribers.firstOrNull()) }
    var amountText by remember { mutableStateOf((selectedSub?.balanceDue ?: 800.0).toInt().toString()) }
    var selectedMethod by remember { mutableStateOf("bKash Merchant") }
    var trxId by remember { mutableStateOf("") }
    var expandedSubDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Collect ISP Bill", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subscriber picker if not pre-locked
                if (subscriber == null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubDropdown,
                        onExpandedChange = { expandedSubDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSub?.let { "${it.name} (${it.pppoeUsername})" } ?: "Select Subscriber",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subscriber") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubDropdown,
                            onDismissRequest = { expandedSubDropdown = false }
                        ) {
                            allSubscribers.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.name} • Due ৳${sub.balanceDue.toInt()}") },
                                    onClick = {
                                        selectedSub = sub
                                        amountText = sub.balanceDue.toInt().toString()
                                        expandedSubDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(selectedSub?.name ?: "", fontWeight = FontWeight.Bold)
                            Text("PPPoE: ${selectedSub?.pppoeUsername} • Plan: ${selectedSub?.planName}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Outstanding Due: ৳${selectedSub?.balanceDue?.toInt() ?: 0}",
                                fontWeight = FontWeight.Bold,
                                color = if ((selectedSub?.balanceDue ?: 0.0) > 0) MaterialTheme.colorScheme.error else StatusActive
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (BDT ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                // Payment Method
                Text("Payment Channel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                val methods = listOf("bKash Merchant", "Nagad", "Cash Collection", "Bank Transfer")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedMethod = method })
                                Spacer(modifier = Modifier.width(8.dp))
                                val iconColor = when (method) {
                                    "bKash Merchant" -> BkashPink
                                    "Nagad" -> NagadOrange
                                    "Cash Collection" -> StatusActive
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(iconColor, shape = RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(method, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                // Transaction ID
                OutlinedTextField(
                    value = trxId,
                    onValueChange = { trxId = it.uppercase() },
                    label = { Text("Provider Trx ID / Receipt Reference") },
                    placeholder = { Text(if (selectedMethod.contains("bKash")) "e.g. BK892A109" else "e.g. CASH-01") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_trx_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = selectedSub ?: return@Button
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(sub.id, null, amt, selectedMethod, trxId)
                    }
                },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Confirm & Reconcile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriberDialog(
    plans: List<PlanEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, phone: String, address: String, area: String,
        plan: PlanEntity, pppoeUser: String, pppoePass: String,
        ip: String, olt: String, ponPort: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val areas = listOf("Madhabpur Sadar", "Titil Bazar", "Station Road", "College Road", "Baniachong Link")
    var selectedArea by remember { mutableStateOf(areas.first()) }
    var expandedArea by remember { mutableStateOf(false) }

    var selectedPlan by remember { mutableStateOf(plans.firstOrNull()) }
    var expandedPlan by remember { mutableStateOf(false) }

    var pppoeUser by remember { mutableStateOf("") }
    var pppoePass by remember { mutableStateOf("tnt1234") }
    var ip by remember { mutableStateOf("10.10.20." + (20..99).random()) }
    val olts = listOf("OLT-01 Main", "OLT-02 South")
    var selectedOlt by remember { mutableStateOf(olts.first()) }
    var ponPort by remember { mutableStateOf("PON 1/1:" + (1..16).random()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Subscriber Registration", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (pppoeUser.isBlank()) {
                            pppoeUser = it.trim().lowercase().replace(" ", "_") + "_tnt"
                        }
                    },
                    label = { Text("Full Name / Business") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone") },
                    placeholder = { Text("017XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Installation Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Area Selector
                ExposedDropdownMenuBox(
                    expanded = expandedArea,
                    onExpandedChange = { expandedArea = it }
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("POP / Coverage Area") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArea) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedArea,
                        onDismissRequest = { expandedArea = false }
                    ) {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = { Text(area) },
                                onClick = {
                                    selectedArea = area
                                    expandedArea = false
                                }
                            )
                        }
                    }
                }

                // Plan Selector
                ExposedDropdownMenuBox(
                    expanded = expandedPlan,
                    onExpandedChange = { expandedPlan = it }
                ) {
                    OutlinedTextField(
                        value = selectedPlan?.let { "${it.name} (৳${it.monthlyPrice.toInt()}/mo)" } ?: "Select Plan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Internet Bandwidth Plan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlan) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPlan,
                        onDismissRequest = { expandedPlan = false }
                    ) {
                        plans.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} • ${p.speedMbps} Mbps • ৳${p.monthlyPrice.toInt()}") },
                                onClick = {
                                    selectedPlan = p
                                    expandedPlan = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("MikroTik & GPON Provisioning", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = pppoeUser,
                    onValueChange = { pppoeUser = it },
                    label = { Text("PPPoE Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pppoePass,
                    onValueChange = { pppoePass = it },
                    label = { Text("PPPoE Password / Secret") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("Framed IP") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ponPort,
                        onValueChange = { ponPort = it },
                        label = { Text("PON Port") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = selectedPlan ?: return@Button
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, address, selectedArea, p, pppoeUser, pppoePass, ip, selectedOlt, ponPort)
                    }
                },
                modifier = Modifier.testTag("submit_subscriber_button")
            ) {
                Text("Provision & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
