package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriberEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusPending

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribersScreen(
    subscribers: List<SubscriberEntity>,
    searchQuery: String,
    selectedArea: String,
    selectedStatus: String,
    onSearchChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSelectSubscriber: (SubscriberEntity) -> Unit,
    onAddSubscriber: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSubscriber,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_subscriber")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Subscriber")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("subscribers_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("subscriber_search_input"),
                placeholder = { Text("Search by name, PPPoE, phone, IP...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips: Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("ALL", "ACTIVE", "SUSPENDED", "REACTIVATION_PENDING")
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusChange(status) },
                        label = {
                            Text(
                                when (status) {
                                    "ALL" -> "All Status"
                                    "ACTIVE" -> "Active"
                                    "SUSPENDED" -> "Suspended"
                                    "REACTIVATION_PENDING" -> "Reactivating"
                                    else -> status
                                },
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Filter Chips: Area / POP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val areas = listOf("All Areas", "Madhabpur Sadar", "Titil Bazar", "Station Road", "College Road", "Baniachong Link")
                areas.forEach { area ->
                    FilterChip(
                        selected = selectedArea == area,
                        onClick = { onAreaChange(area) },
                        label = { Text(area, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Results summary
            Text(
                text = "${subscribers.size} subscribers found",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Subscriber List
            if (subscribers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No subscribers match your search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(subscribers, key = { it.id }) { sub ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSubscriber(sub) }
                                .testTag("subscriber_item_${sub.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = sub.name.take(2).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(
                                                "${sub.subscriberId} • ${sub.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusBadge(status = sub.status)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = sub.planName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "PPPoE: ${sub.pppoeUsername} • IP: ${sub.ipAddress}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (sub.balanceDue > 0) {
                                            Text(
                                                text = "Due: ৳${sub.balanceDue.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        } else {
                                            Text(
                                                text = "Paid (৳0 Due)",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = StatusActive
                                            )
                                        }
                                        Text(
                                            text = "RX: ${sub.opticalPowerDbm} dBm",
                                            fontSize = 11.sp,
                                            color = if (sub.opticalPowerDbm > -24.0) StatusActive else StatusPending
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
