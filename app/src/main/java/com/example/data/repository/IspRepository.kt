package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class IspRepository(
    private val subscriberDao: SubscriberDao,
    private val planDao: PlanDao,
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao,
    private val networkNodeDao: NetworkNodeDao
) {
    val subscribers: Flow<List<SubscriberEntity>> = subscriberDao.getAllSubscribers()
    val plans: Flow<List<PlanEntity>> = planDao.getAllPlans()
    val invoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val payments: Flow<List<PaymentTransactionEntity>> = paymentDao.getAllPayments()
    val networkNodes: Flow<List<NetworkNodeEntity>> = networkNodeDao.getAllNodes()

    fun getSubscriberById(id: Int): Flow<SubscriberEntity?> = subscriberDao.getSubscriberById(id)
    fun getInvoicesForSubscriber(subscriberId: Int): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesForSubscriber(subscriberId)

    suspend fun getSubscriberByToken(token: String): SubscriberEntity? = subscriberDao.getSubscriberByToken(token)

    suspend fun addSubscriber(
        name: String,
        phone: String,
        address: String,
        area: String,
        plan: PlanEntity,
        pppoeUser: String,
        pppoePass: String,
        ip: String,
        olt: String,
        ponPort: String
    ): Long {
        val randomSuffix = (1000..9999).random()
        val token = "tnt_sec_" + UUID.randomUUID().toString().take(12)
        val subscriber = SubscriberEntity(
            subscriberId = "TNT-$randomSuffix",
            name = name,
            phone = phone,
            address = address,
            area = area,
            planId = plan.id,
            planName = plan.name,
            pppoeUsername = pppoeUser,
            pppoePassword = pppoePass,
            ipAddress = ip,
            oltName = olt,
            ponPort = ponPort,
            onuMac = "48:57:00:FA:${(10..99).random()}:${(10..99).random()}",
            opticalPowerDbm = -((160..240).random().toDouble() / 10.0),
            status = "ACTIVE",
            permanentLinkToken = token,
            billingDay = 5,
            balanceDue = plan.monthlyPrice + (plan.monthlyPrice * plan.taxPercent / 100.0)
        )
        val id = subscriberDao.insertSubscriber(subscriber)

        // Generate first invoice automatically
        val tax = plan.monthlyPrice * (plan.taxPercent / 100.0)
        val total = plan.monthlyPrice + tax
        val invoice = InvoiceEntity(
            invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(8)}",
            subscriberId = id.toInt(),
            subscriberName = name,
            pppoeUsername = pppoeUser,
            monthYear = "Initial Setup",
            planPrice = plan.monthlyPrice,
            previousDue = 0.0,
            lateFee = 0.0,
            taxAmount = tax,
            discount = 0.0,
            totalPayable = total,
            paidAmount = 0.0,
            status = "PENDING",
            dueDate = "Next 5 Days"
        )
        invoiceDao.insertInvoice(invoice)
        return id
    }

    suspend fun setSubscriberStatus(id: Int, status: String) {
        subscriberDao.updateStatus(id, status)
    }

    suspend fun updateSubscriber(subscriber: SubscriberEntity) {
        subscriberDao.updateSubscriber(subscriber)
    }

    suspend fun recordPayment(
        subscriberId: Int,
        invoiceId: Int?,
        amount: Double,
        paymentMethod: String,
        providerTxnId: String,
        collectedBy: String
    ): PaymentTransactionEntity {
        val currentSubs = subscribers.firstOrNull() ?: emptyList()
        val subscriber = currentSubs.find { it.id == subscriberId }
        val subName = subscriber?.name ?: "Subscriber #$subscriberId"

        val receiptNum = "RCT-" + System.currentTimeMillis().toString().takeLast(6)
        val payment = PaymentTransactionEntity(
            transactionId = "TRX-" + UUID.randomUUID().toString().take(8).uppercase(),
            invoiceId = invoiceId ?: 0,
            subscriberId = subscriberId,
            subscriberName = subName,
            amount = amount,
            paymentMethod = paymentMethod,
            providerTxnId = providerTxnId.ifBlank { "MANUAL-${System.currentTimeMillis().toString().takeLast(6)}" },
            status = "SUCCESS",
            collectedBy = collectedBy,
            receiptNumber = receiptNum,
            timestamp = System.currentTimeMillis()
        )
        paymentDao.insertPayment(payment)

        // Update subscriber balance
        if (subscriber != null) {
            val newBalance = maxOf(0.0, subscriber.balanceDue - amount)
            subscriberDao.updateBalance(subscriberId, newBalance)

            // If suspended and fully paid (or paying substantial due), trigger reactivation
            if (subscriber.status == "SUSPENDED" && newBalance <= 0.0) {
                subscriberDao.updateStatus(subscriberId, "ACTIVE")
            } else if (subscriber.status == "SUSPENDED") {
                subscriberDao.updateStatus(subscriberId, "REACTIVATION_PENDING")
            }
        }

        // If an invoice is targeted, update the invoice
        if (invoiceId != null && invoiceId > 0) {
            val invoice = invoiceDao.getInvoiceById(invoiceId)
            if (invoice != null) {
                val newPaid = invoice.paidAmount + amount
                val newStatus = when {
                    newPaid >= invoice.totalPayable -> "PAID"
                    newPaid > 0.0 -> "PARTIALLY_PAID"
                    else -> invoice.status
                }
                invoiceDao.updateInvoice(
                    invoice.copy(
                        paidAmount = newPaid,
                        status = newStatus
                    )
                )
            }
        }

        return payment
    }

    suspend fun generateMonthlyBillingCycle(monthYear: String, dueDate: String): Int {
        val currentSubs = subscribers.firstOrNull() ?: emptyList()
        val currentPlans = plans.firstOrNull() ?: emptyList()
        var count = 0

        for (sub in currentSubs) {
            val plan = currentPlans.find { it.id == sub.planId }
            val price = plan?.monthlyPrice ?: 800.0
            val tax = price * 0.05
            val total = price + tax + sub.balanceDue

            val invoice = InvoiceEntity(
                invoiceNumber = "INV-${(100000..999999).random()}",
                subscriberId = sub.id,
                subscriberName = sub.name,
                pppoeUsername = sub.pppoeUsername,
                monthYear = monthYear,
                planPrice = price,
                previousDue = sub.balanceDue,
                lateFee = 0.0,
                taxAmount = tax,
                discount = 0.0,
                totalPayable = total,
                paidAmount = 0.0,
                status = "PENDING",
                dueDate = dueDate
            )
            invoiceDao.insertInvoice(invoice)
            subscriberDao.updateBalance(sub.id, total)
            count++
        }
        return count
    }
}
