package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubscriberEntity::class,
        PlanEntity::class,
        InvoiceEntity::class,
        PaymentTransactionEntity::class,
        NetworkNodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriberDao(): SubscriberDao
    abstract fun planDao(): PlanDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun networkNodeDao(): NetworkNodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "titil_isp_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val planDao = db.planDao()
            val subDao = db.subscriberDao()
            val invDao = db.invoiceDao()
            val payDao = db.paymentDao()
            val nodeDao = db.networkNodeDao()

            // 1. Initial Plans
            val plans = listOf(
                PlanEntity(id = 1, name = "Titil Starter 10M", speedMbps = 10, monthlyPrice = 500.0, bandwidthType = "Shared 1:4", taxPercent = 5.0),
                PlanEntity(id = 2, name = "Titil Standard 20M", speedMbps = 20, monthlyPrice = 800.0, bandwidthType = "Shared 1:4", taxPercent = 5.0),
                PlanEntity(id = 3, name = "Titil Turbo 35M", speedMbps = 35, monthlyPrice = 1200.0, bandwidthType = "Dedicated 1:2", taxPercent = 5.0),
                PlanEntity(id = 4, name = "Titil Corporate 60M", speedMbps = 60, monthlyPrice = 2500.0, bandwidthType = "Dedicated 1:1", taxPercent = 5.0)
            )
            planDao.insertPlans(plans)

            // 2. Initial Subscribers
            val subscribers = listOf(
                SubscriberEntity(
                    id = 1,
                    subscriberId = "TNT-1001",
                    name = "Md. Rafiqul Islam",
                    phone = "01711223344",
                    address = "Ward 03, Madhabpur Sadar",
                    area = "Madhabpur Sadar",
                    planId = 2,
                    planName = "Titil Standard 20M",
                    pppoeUsername = "rafiq_tnt",
                    pppoePassword = "pass1001@rafiq",
                    ipAddress = "10.10.20.14",
                    oltName = "OLT-01 Main",
                    ponPort = "PON 1/1:3",
                    onuMac = "48:57:00:FA:11:01",
                    opticalPowerDbm = -18.2,
                    status = "ACTIVE",
                    permanentLinkToken = "tnt_sec_rafiq1001",
                    billingDay = 5,
                    balanceDue = 0.0
                ),
                SubscriberEntity(
                    id = 2,
                    subscriberId = "TNT-1002",
                    name = "Faruk Ahmed (Titil Pharmacy)",
                    phone = "01819988776",
                    address = "Shop 12, Titil Bazar Market",
                    area = "Titil Bazar",
                    planId = 3,
                    planName = "Titil Turbo 35M",
                    pppoeUsername = "titil_pharmacy",
                    pppoePassword = "pass1002@faruk",
                    ipAddress = "10.10.20.19",
                    oltName = "OLT-02 South",
                    ponPort = "PON 1/2:8",
                    onuMac = "48:57:00:FA:22:04",
                    opticalPowerDbm = -21.4,
                    status = "ACTIVE",
                    permanentLinkToken = "tnt_sec_faruk1002",
                    billingDay = 5,
                    balanceDue = 840.0
                ),
                SubscriberEntity(
                    id = 3,
                    subscriberId = "TNT-1003",
                    name = "Madhabpur Model School",
                    phone = "01912345678",
                    address = "School Road, Station Area",
                    area = "Station Road",
                    planId = 4,
                    planName = "Titil Corporate 60M",
                    pppoeUsername = "model_school_tnt",
                    pppoePassword = "pass1003@school",
                    ipAddress = "10.10.10.5",
                    oltName = "OLT-01 Main",
                    ponPort = "PON 1/3:1",
                    onuMac = "48:57:00:FA:33:09",
                    opticalPowerDbm = -17.5,
                    status = "ACTIVE",
                    permanentLinkToken = "tnt_sec_school1003",
                    billingDay = 1,
                    balanceDue = 0.0
                ),
                SubscriberEntity(
                    id = 4,
                    subscriberId = "TNT-1004",
                    name = "Tanvir Hasan",
                    phone = "01678901234",
                    address = "College Gate, Madhabpur",
                    area = "College Road",
                    planId = 1,
                    planName = "Titil Starter 10M",
                    pppoeUsername = "tanvir_col",
                    pppoePassword = "pass1004@tanvir",
                    ipAddress = "10.10.20.44",
                    oltName = "OLT-01 Main",
                    ponPort = "PON 1/1:9",
                    onuMac = "48:57:00:FA:44:12",
                    opticalPowerDbm = -26.8,
                    status = "SUSPENDED",
                    permanentLinkToken = "tnt_sec_tanvir1004",
                    billingDay = 5,
                    balanceDue = 1050.0
                ),
                SubscriberEntity(
                    id = 5,
                    subscriberId = "TNT-1005",
                    name = "Kabir Hossain",
                    phone = "01722334455",
                    address = "North Titil, Baniachong Road",
                    area = "Baniachong Link",
                    planId = 2,
                    planName = "Titil Standard 20M",
                    pppoeUsername = "kabir_baniachong",
                    pppoePassword = "pass1005@kabir",
                    ipAddress = "10.10.20.61",
                    oltName = "OLT-02 South",
                    ponPort = "PON 1/2:11",
                    onuMac = "48:57:00:FA:55:18",
                    opticalPowerDbm = -19.9,
                    status = "REACTIVATION_PENDING",
                    permanentLinkToken = "tnt_sec_kabir1005",
                    billingDay = 10,
                    balanceDue = 0.0
                )
            )
            subDao.insertSubscribers(subscribers)

            // 3. Initial Invoices
            val invoices = listOf(
                InvoiceEntity(
                    id = 1,
                    invoiceNumber = "INV-202609-1001",
                    subscriberId = 1,
                    subscriberName = "Md. Rafiqul Islam",
                    pppoeUsername = "rafiq_tnt",
                    monthYear = "September 2026",
                    planPrice = 800.0,
                    previousDue = 0.0,
                    lateFee = 0.0,
                    taxAmount = 40.0,
                    discount = 0.0,
                    totalPayable = 840.0,
                    paidAmount = 840.0,
                    status = "PAID",
                    dueDate = "10 Sep 2026"
                ),
                InvoiceEntity(
                    id = 2,
                    invoiceNumber = "INV-202609-1002",
                    subscriberId = 2,
                    subscriberName = "Faruk Ahmed (Titil Pharmacy)",
                    pppoeUsername = "titil_pharmacy",
                    monthYear = "September 2026",
                    planPrice = 1200.0,
                    previousDue = 0.0,
                    lateFee = 0.0,
                    taxAmount = 60.0,
                    discount = 0.0,
                    totalPayable = 1260.0,
                    paidAmount = 420.0,
                    status = "PARTIALLY_PAID",
                    dueDate = "10 Sep 2026"
                ),
                InvoiceEntity(
                    id = 3,
                    invoiceNumber = "INV-202609-1004",
                    subscriberId = 4,
                    subscriberName = "Tanvir Hasan",
                    pppoeUsername = "tanvir_col",
                    monthYear = "September 2026",
                    planPrice = 500.0,
                    previousDue = 500.0,
                    lateFee = 50.0,
                    taxAmount = 0.0,
                    discount = 0.0,
                    totalPayable = 1050.0,
                    paidAmount = 0.0,
                    status = "OVERDUE",
                    dueDate = "05 Sep 2026"
                ),
                InvoiceEntity(
                    id = 4,
                    invoiceNumber = "INV-202609-1003",
                    subscriberId = 3,
                    subscriberName = "Madhabpur Model School",
                    pppoeUsername = "model_school_tnt",
                    monthYear = "September 2026",
                    planPrice = 2500.0,
                    previousDue = 0.0,
                    lateFee = 0.0,
                    taxAmount = 125.0,
                    discount = 0.0,
                    totalPayable = 2625.0,
                    paidAmount = 2625.0,
                    status = "PAID",
                    dueDate = "05 Sep 2026"
                )
            )
            invDao.insertInvoices(invoices)

            // 4. Initial Payments
            val payments = listOf(
                PaymentTransactionEntity(
                    id = 1,
                    transactionId = "TRX-BK-789124",
                    invoiceId = 1,
                    subscriberId = 1,
                    subscriberName = "Md. Rafiqul Islam",
                    amount = 840.0,
                    paymentMethod = "bKash Merchant",
                    providerTxnId = "BK9A814B60",
                    status = "SUCCESS",
                    collectedBy = "Customer Portal (bKash IPN)",
                    receiptNumber = "RCT-2026-0012"
                ),
                PaymentTransactionEntity(
                    id = 2,
                    transactionId = "TRX-CS-789125",
                    invoiceId = 2,
                    subscriberId = 2,
                    subscriberName = "Faruk Ahmed (Titil Pharmacy)",
                    amount = 420.0,
                    paymentMethod = "Cash Collection",
                    providerTxnId = "CASH-STAFF-01",
                    status = "SUCCESS",
                    collectedBy = "Staff - Kamrul",
                    receiptNumber = "RCT-2026-0013"
                ),
                PaymentTransactionEntity(
                    id = 3,
                    transactionId = "TRX-NG-789126",
                    invoiceId = 4,
                    subscriberId = 3,
                    subscriberName = "Madhabpur Model School",
                    amount = 2625.0,
                    paymentMethod = "Nagad",
                    providerTxnId = "NG8821947",
                    status = "SUCCESS",
                    collectedBy = "Customer Portal (Nagad Gateway)",
                    receiptNumber = "RCT-2026-0014"
                )
            )
            payDao.insertPayments(payments)

            // 5. Initial Network Nodes
            val nodes = listOf(
                NetworkNodeEntity(
                    id = 1,
                    nodeName = "Core-CCR1036-Madhabpur",
                    nodeType = "MikroTik CCR1036",
                    ip = "103.145.88.1",
                    activePppoeSessions = 342,
                    cpuLoadPercent = 18,
                    memoryUsagePercent = 29,
                    uptime = "48d 14h 22m",
                    status = "ONLINE"
                ),
                NetworkNodeEntity(
                    id = 2,
                    nodeName = "Titil-OLT-Huawei5608T",
                    nodeType = "GPON OLT 8-Port",
                    ip = "192.168.88.10",
                    activePppoeSessions = 210,
                    cpuLoadPercent = 24,
                    memoryUsagePercent = 41,
                    uptime = "19d 08h 15m",
                    status = "ONLINE"
                ),
                NetworkNodeEntity(
                    id = 3,
                    nodeName = "Radius-AAA-Primary",
                    nodeType = "FreeRADIUS 3.2",
                    ip = "10.10.1.254",
                    activePppoeSessions = 342,
                    cpuLoadPercent = 7,
                    memoryUsagePercent = 19,
                    uptime = "92d 11h 05m",
                    status = "ONLINE"
                )
            )
            nodeDao.insertNodes(nodes)
        }
    }
}
