package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriberDao {
    @Query("SELECT * FROM subscribers ORDER BY id DESC")
    fun getAllSubscribers(): Flow<List<SubscriberEntity>>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    fun getSubscriberById(id: Int): Flow<SubscriberEntity?>

    @Query("SELECT * FROM subscribers WHERE permanentLinkToken = :token LIMIT 1")
    suspend fun getSubscriberByToken(token: String): SubscriberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscribers(subscribers: List<SubscriberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: SubscriberEntity): Long

    @Update
    suspend fun updateSubscriber(subscriber: SubscriberEntity)

    @Delete
    suspend fun deleteSubscriber(subscriber: SubscriberEntity)

    @Query("UPDATE subscribers SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE subscribers SET balanceDue = :balanceDue WHERE id = :id")
    suspend fun updateBalance(id: Int, balanceDue: Double)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY monthlyPrice ASC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<PlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE subscriberId = :subscriberId ORDER BY id DESC")
    fun getInvoicesForSubscriber(subscriberId: Int): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payments WHERE subscriberId = :subscriberId ORDER BY timestamp DESC")
    fun getPaymentsForSubscriber(subscriberId: Int): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentTransactionEntity>)
}

@Dao
interface NetworkNodeDao {
    @Query("SELECT * FROM network_nodes ORDER BY id ASC")
    fun getAllNodes(): Flow<List<NetworkNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NetworkNodeEntity>)

    @Update
    suspend fun updateNode(node: NetworkNodeEntity)
}
