package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubscriberEntity
import com.example.ui.components.AddSubscriberDialog
import com.example.ui.components.PaymentCollectionDialog
import com.example.ui.components.SubscriberDetailSheet
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusActive
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.IspViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val viewModel: IspViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        IspApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IspApp(viewModel: IspViewModel) {
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
  val subscribers by viewModel.subscribers.collectAsStateWithLifecycle()
  val filteredSubscribers by viewModel.filteredSubscribers.collectAsStateWithLifecycle()
  val plans by viewModel.plans.collectAsStateWithLifecycle()
  val invoices by viewModel.invoices.collectAsStateWithLifecycle()
  val payments by viewModel.payments.collectAsStateWithLifecycle()
  val networkNodes by viewModel.networkNodes.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedArea by viewModel.selectedAreaFilter.collectAsStateWithLifecycle()
  val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
  val selectedSubscriber by viewModel.selectedSubscriber.collectAsStateWithLifecycle()
  val staffRole by viewModel.staffRole.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var showAddSubscriberDialog by remember { mutableStateOf(false) }
  var showPaymentDialog by remember { mutableStateOf(false) }
  var paymentTargetSubscriber by remember { mutableStateOf<SubscriberEntity?>(null) }
  var showDetailSheet by remember { mutableStateOf(false) }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = MaterialTheme.colorScheme.primary,
              shape = CircleShape,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Madhabpur Titil Net",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "ISP Operations & Billing",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Staff role switcher
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("staff_role_chip")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(StatusActive)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = staffRole,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              IconButton(
                onClick = { viewModel.toggleStaffRole() },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Role", modifier = Modifier.size(14.dp))
              }
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationBarItem(
          selected = currentTab == AppTab.DASHBOARD,
          onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
          icon = { Icon(if (currentTab == AppTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
          label = { Text("Dashboard", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_dashboard")
        )
        NavigationBarItem(
          selected = currentTab == AppTab.SUBSCRIBERS,
          onClick = { viewModel.selectTab(AppTab.SUBSCRIBERS) },
          icon = { Icon(if (currentTab == AppTab.SUBSCRIBERS) Icons.Filled.People else Icons.Outlined.People, contentDescription = "Clients") },
          label = { Text("Clients", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_subscribers")
        )
        NavigationBarItem(
          selected = currentTab == AppTab.BILLING,
          onClick = { viewModel.selectTab(AppTab.BILLING) },
          icon = { Icon(if (currentTab == AppTab.BILLING) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Billing") },
          label = { Text("Billing", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_billing")
        )
        NavigationBarItem(
          selected = currentTab == AppTab.NETWORK,
          onClick = { viewModel.selectTab(AppTab.NETWORK) },
          icon = { Icon(if (currentTab == AppTab.NETWORK) Icons.Filled.Router else Icons.Outlined.Router, contentDescription = "Network") },
          label = { Text("Network", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_network")
        )
        NavigationBarItem(
          selected = currentTab == AppTab.PORTAL_SIMULATOR,
          onClick = { viewModel.selectTab(AppTab.PORTAL_SIMULATOR) },
          icon = { Icon(if (currentTab == AppTab.PORTAL_SIMULATOR) Icons.Filled.Language else Icons.Outlined.Language, contentDescription = "Portal") },
          label = { Text("Self-Care", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_portal")
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (currentTab) {
        AppTab.DASHBOARD -> {
          DashboardScreen(
            metrics = metrics,
            recentPayments = payments,
            subscribers = subscribers,
            onNavigateTab = { viewModel.selectTab(it) },
            onAddSubscriber = { showAddSubscriberDialog = true },
            onCollectPayment = {
              paymentTargetSubscriber = null
              showPaymentDialog = true
            },
            onRunBillingCycle = {
              viewModel.runMonthlyBillingCycle("October 2026", "10 Oct 2026")
            },
            onSelectSubscriber = { sub ->
              viewModel.selectSubscriber(sub)
              showDetailSheet = true
            }
          )
        }

        AppTab.SUBSCRIBERS -> {
          SubscribersScreen(
            subscribers = filteredSubscribers,
            searchQuery = searchQuery,
            selectedArea = selectedArea,
            selectedStatus = selectedStatus,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onAreaChange = { viewModel.setAreaFilter(it) },
            onStatusChange = { viewModel.setStatusFilter(it) },
            onSelectSubscriber = { sub ->
              viewModel.selectSubscriber(sub)
              showDetailSheet = true
            },
            onAddSubscriber = { showAddSubscriberDialog = true }
          )
        }

        AppTab.BILLING -> {
          BillingScreen(
            invoices = invoices,
            payments = payments,
            onCollectPayment = {
              paymentTargetSubscriber = null
              showPaymentDialog = true
            },
            onRunBillingCycle = {
              viewModel.runMonthlyBillingCycle("October 2026", "10 Oct 2026")
            }
          )
        }

        AppTab.NETWORK -> {
          NetworkScreen(
            nodes = networkNodes,
            onTriggerSync = { msg ->
              scope.launch {
                snackbarHostState.showSnackbar(msg)
              }
            }
          )
        }

        AppTab.PORTAL_SIMULATOR -> {
          CustomerPortalSimulatorScreen(
            subscribers = subscribers,
            selectedSubscriber = selectedSubscriber,
            onSelectSubscriber = { viewModel.selectSubscriber(it) },
            onSimulateCustomerPayment = { subId, amount, method, trx ->
              viewModel.collectPayment(subId, null, amount, method, trx)
            }
          )
        }
      }
    }
  }

  // Dialogs & Sheets
  if (showAddSubscriberDialog) {
    AddSubscriberDialog(
      plans = plans,
      onDismiss = { showAddSubscriberDialog = false },
      onConfirm = { name, phone, address, area, plan, pppoeUser, pppoePass, ip, olt, ponPort ->
        viewModel.addSubscriber(name, phone, address, area, plan, pppoeUser, pppoePass, ip, olt, ponPort)
        showAddSubscriberDialog = false
      }
    )
  }

  if (showPaymentDialog) {
    PaymentCollectionDialog(
      subscriber = paymentTargetSubscriber,
      allSubscribers = subscribers,
      onDismiss = { showPaymentDialog = false },
      onConfirm = { subId, invoiceId, amount, method, trxId ->
        viewModel.collectPayment(subId, invoiceId, amount, method, trxId)
        showPaymentDialog = false
      }
    )
  }

  if (showDetailSheet && selectedSubscriber != null) {
    SubscriberDetailSheet(
      subscriber = selectedSubscriber!!,
      onDismiss = { showDetailSheet = false },
      onToggleStatus = {
        selectedSubscriber?.let { viewModel.toggleSubscriberNetworkLine(it) }
      },
      onCollectPayment = {
        paymentTargetSubscriber = selectedSubscriber
        showPaymentDialog = true
      },
      onPingLine = {
        selectedSubscriber?.let { viewModel.pingSubscriberLine(it) }
      },
      onOpenCustomerPortal = {
        showDetailSheet = false
        viewModel.selectTab(AppTab.PORTAL_SIMULATOR)
      }
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

