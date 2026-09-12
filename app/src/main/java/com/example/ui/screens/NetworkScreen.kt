package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkNodeEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusPending

@Composable
fun NetworkScreen(
    nodes: List<NetworkNodeEntity>,
    onTriggerSync: (String) -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("network_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Network Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Madhabpur Network Backbone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("MikroTik CCR1036 + Huawei GPON OLT", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = "ONLINE")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onTriggerSync("MikroTik RouterOS API Queues synchronized")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_mikrotik_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync MikroTik", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onTriggerSync("RADIUS AAA Heartbeat OK - Active sessions reconciled")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RADIUS Ping", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Nodes List
        item {
            Text(
                text = "Core Nodes & Edge Gateways",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(nodes, key = { it.id }) { node ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (node.nodeType.contains("OLT")) Icons.Default.Hub else Icons.Default.Router,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(node.nodeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${node.nodeType} • IP: ${node.ip}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusBadge(status = node.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Telemetry row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Sessions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${node.activePppoeSessions} PPPoE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("CPU Load", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${node.cpuLoadPercent}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (node.cpuLoadPercent > 60) StatusPending else StatusActive)
                        }
                        Column {
                            Text("Memory Usage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${node.memoryUsagePercent}%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Uptime", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(node.uptime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Fiber Distribution & PON Ports
        item {
            Text(
                text = "GPON Splitter & Fiber Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PonPortRow(port = "PON 1/1", coverage = "Madhabpur Sadar Central", activeOnu = 32, maxOnu = 64, rxAvg = "-18.5 dBm")
                    HorizontalDivider()
                    PonPortRow(port = "PON 1/2", coverage = "Titil Bazar South Loop", activeOnu = 28, maxOnu = 64, rxAvg = "-20.2 dBm")
                    HorizontalDivider()
                    PonPortRow(port = "PON 1/3", coverage = "Station Road & Model School", activeOnu = 19, maxOnu = 64, rxAvg = "-17.8 dBm")
                    HorizontalDivider()
                    PonPortRow(port = "PON 1/4", coverage = "College Gate Extension", activeOnu = 14, maxOnu = 64, rxAvg = "-22.1 dBm")
                }
            }
        }
    }
}

@Composable
private fun PonPortRow(
    port: String,
    coverage: String,
    activeOnu: Int,
    maxOnu: Int,
    rxAvg: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$port • $coverage", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("$activeOnu / $maxOnu ONUs connected • Avg RX: $rxAvg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            color = StatusActive.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "Normal",
                color = StatusActive,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
