package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers")
data class SubscriberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberId: String,
    val name: String,
    val phone: String,
    val address: String,
    val area: String,
    val planId: Int,
    val planName: String,
    val pppoeUsername: String,
    val pppoePassword: String = "tnt1234",
    val ipAddress: String,
    val oltName: String = "OLT-01 Main",
    val ponPort: String = "PON 1/1:2",
    val onuMac: String = "48:57:00:1A:2B:3C",
    val opticalPowerDbm: Double = -19.4,
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "REACTIVATION_PENDING"
    val permanentLinkToken: String,
    val billingDay: Int = 5,
    val balanceDue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val speedMbps: Int,
    val monthlyPrice: Double,
    val bandwidthType: String = "Shared 1:4",
    val taxPercent: Double = 5.0
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val subscriberId: Int,
    val subscriberName: String,
    val pppoeUsername: String,
    val monthYear: String,
    val planPrice: Double,
    val previousDue: Double,
    val lateFee: Double,
    val taxAmount: Double,
    val discount: Double,
    val totalPayable: Double,
    val paidAmount: Double,
    val status: String, // "PAID", "PENDING", "PARTIALLY_PAID", "OVERDUE"
    val dueDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: String,
    val invoiceId: Int,
    val subscriberId: Int,
    val subscriberName: String,
    val amount: Double,
    val paymentMethod: String, // "bKash Merchant", "Nagad", "Bank Transfer", "Cash Collection"
    val providerTxnId: String,
    val status: String = "SUCCESS",
    val collectedBy: String = "Admin / Staff",
    val receiptNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "network_nodes")
data class NetworkNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nodeName: String,
    val nodeType: String,
    val ip: String,
    val activePppoeSessions: Int,
    val cpuLoadPercent: Int,
    val memoryUsagePercent: Int,
    val uptime: String,
    val status: String = "ONLINE"
)
