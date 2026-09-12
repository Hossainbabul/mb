package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.IspRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab {
    DASHBOARD,
    SUBSCRIBERS,
    BILLING,
    NETWORK,
    PORTAL_SIMULATOR
}

data class DashboardMetrics(
    val totalSubscribers: Int = 0,
    val activeSubscribers: Int = 0,
    val suspendedSubscribers: Int = 0,
    val totalOutstandingDue: Double = 0.0,
    val totalCollectedRevenue: Double = 0.0,
    val activePppoeSessions: Int = 0
)

class IspViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IspRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = IspRepository(
            database.subscriberDao(),
            database.planDao(),
            database.invoiceDao(),
            database.paymentDao(),
            database.networkNodeDao()
        )
    }

    val subscribers: StateFlow<List<SubscriberEntity>> = repository.subscribers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plans: StateFlow<List<PlanEntity>> = repository.plans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentTransactionEntity>> = repository.payments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val networkNodes: StateFlow<List<NetworkNodeEntity>> = repository.networkNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAreaFilter = MutableStateFlow("All Areas")
    val selectedAreaFilter: StateFlow<String> = _selectedAreaFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedSubscriber = MutableStateFlow<SubscriberEntity?>(null)
    val selectedSubscriber: StateFlow<SubscriberEntity?> = _selectedSubscriber.asStateFlow()

    private val _staffRole = MutableStateFlow("Admin")
    val staffRole: StateFlow<String> = _staffRole.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val filteredSubscribers = combine(
        subscribers,
        _searchQuery,
        _selectedAreaFilter,
        _selectedStatusFilter
    ) { subs, query, area, status ->
        subs.filter { sub ->
            val matchesQuery = query.isBlank() ||
                    sub.name.contains(query, ignoreCase = true) ||
                    sub.pppoeUsername.contains(query, ignoreCase = true) ||
                    sub.phone.contains(query) ||
                    sub.ipAddress.contains(query) ||
                    sub.subscriberId.contains(query, ignoreCase = true)

            val matchesArea = area == "All Areas" || sub.area == area
            val matchesStatus = status == "ALL" || sub.status == status

            matchesQuery && matchesArea && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardMetrics = combine(
        subscribers,
        payments,
        networkNodes
    ) { subs, pays, nodes ->
        val total = subs.size
        val active = subs.count { it.status == "ACTIVE" }
        val suspended = subs.count { it.status == "SUSPENDED" }
        val due = subs.sumOf { it.balanceDue }
        val collected = pays.sumOf { it.amount }
        val pppoeSessions = nodes.sumOf { it.activePppoeSessions }
        DashboardMetrics(
            totalSubscribers = total,
            activeSubscribers = active,
            suspendedSubscribers = suspended,
            totalOutstandingDue = due,
            totalCollectedRevenue = collected,
            activePppoeSessions = pppoeSessions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAreaFilter(area: String) {
        _selectedAreaFilter.value = area
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun selectSubscriber(sub: SubscriberEntity?) {
        _selectedSubscriber.value = sub
    }

    fun toggleStaffRole() {
        _staffRole.value = if (_staffRole.value == "Admin") "Billing Agent / Lineman" else "Admin"
        _snackbarMessage.value = "Active profile switched to: ${_staffRole.value}"
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleSubscriberNetworkLine(subscriber: SubscriberEntity) {
        viewModelScope.launch {
            val newStatus = if (subscriber.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
            repository.setSubscriberStatus(subscriber.id, newStatus)
            _selectedSubscriber.value = _selectedSubscriber.value?.copy(status = newStatus)
            val actionName = if (newStatus == "ACTIVE") "Reactivated" else "Suspended"
            _snackbarMessage.value = "MikroTik & RADIUS: Line $actionName for ${subscriber.pppoeUsername}"
        }
    }

    fun addSubscriber(
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
    ) {
        viewModelScope.launch {
            val id = repository.addSubscriber(
                name, phone, address, area, plan, pppoeUser, pppoePass, ip, olt, ponPort
            )
            _snackbarMessage.value = "New Subscriber added: $name ($pppoeUser)"
        }
    }

    fun collectPayment(
        subscriberId: Int,
        invoiceId: Int?,
        amount: Double,
        method: String,
        trxId: String
    ) {
        viewModelScope.launch {
            val payment = repository.recordPayment(
                subscriberId = subscriberId,
                invoiceId = invoiceId,
                amount = amount,
                paymentMethod = method,
                providerTxnId = trxId,
                collectedBy = _staffRole.value
            )
            // Refresh selected subscriber if active
            _selectedSubscriber.value?.let { current ->
                if (current.id == subscriberId) {
                    val updated = repository.getSubscriberById(subscriberId).firstOrNull()
                    _selectedSubscriber.value = updated
                }
            }
            _snackbarMessage.value = "Payment of ৳${amount.toInt()} received via $method. Receipt #${payment.receiptNumber}"
        }
    }

    fun runMonthlyBillingCycle(monthYear: String, dueDate: String) {
        viewModelScope.launch {
            val count = repository.generateMonthlyBillingCycle(monthYear, dueDate)
            _snackbarMessage.value = "Monthly Billing Cycle executed: $count invoices generated for $monthYear"
        }
    }

    fun pingSubscriberLine(subscriber: SubscriberEntity) {
        viewModelScope.launch {
            val latency = (4..18).random()
            _snackbarMessage.value = "Ping to ${subscriber.ipAddress} (${subscriber.pppoeUsername}): $latency ms, Optical Power: ${subscriber.opticalPowerDbm} dBm (Good)"
        }
    }
}
