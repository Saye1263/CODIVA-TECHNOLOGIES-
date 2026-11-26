package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.AuditLogEntry
import com.example.data.AuditSeverity
import com.example.data.CourseEnrollment
import com.example.data.MembershipStatus
import com.example.data.ProjectInquiry
import com.example.data.ReleasePlatform
import com.example.data.ReleaseStatus
import com.example.data.UserProfile
import com.example.data.WorkspaceRecord
import com.example.ui.theme.CodivaAmber
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaRose
import com.example.ui.theme.CodivaSkyBright
import com.example.ui.theme.CodivaSlateLight
import com.example.ui.theme.CodivaViolet
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ExecutiveKpiOverviewRow(
    registeredUsers: List<UserProfile>,
    auditLogs: List<AuditLogEntry>,
    paymentRecords: List<WorkspaceRecord>,
    supportRecords: List<WorkspaceRecord>,
    modifier: Modifier = Modifier
) {
    val activeUsers = remember(registeredUsers) {
        registeredUsers.count { it.statusEnum == MembershipStatus.ACTIVE }
    }
    val pendingUsers = remember(registeredUsers) {
        registeredUsers.count { it.statusEnum == MembershipStatus.PENDING }
    }
    val suspendedUsers = remember(registeredUsers) {
        registeredUsers.count { it.statusEnum == MembershipStatus.SUSPENDED }
    }
    val elevatedAlerts = remember(auditLogs) {
        auditLogs.count { it.severityEnum != AuditSeverity.INFO }
    }
    val openTickets = remember(supportRecords) {
        supportRecords.count { it.status == "OPEN" }
    }
    val paidDollars = remember(paymentRecords) {
        paymentRecords.filter { it.status == "PAID" }.sumOf { it.amountCents } / 100L
    }
    val pendingDollars = remember(paymentRecords) {
        paymentRecords.filter { it.status != "PAID" }.sumOf { it.amountCents } / 100L
    }

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("executive_kpi_overview")
    ) {
        val wide = maxWidth >= 680.dp
        if (wide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserSummaryCard(
                    total = registeredUsers.size,
                    active = activeUsers,
                    pending = pendingUsers,
                    suspended = suspendedUsers,
                    modifier = Modifier.weight(1f)
                )
                SecuritySummaryCard(
                    auditEvents = auditLogs.size,
                    elevatedAlerts = elevatedAlerts,
                    openTickets = openTickets,
                    modifier = Modifier.weight(1f)
                )
                BalanceSummaryCard(
                    paidDollars = paidDollars,
                    pendingDollars = pendingDollars,
                    paidCount = paymentRecords.count { it.status == "PAID" },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                UserSummaryCard(
                    total = registeredUsers.size,
                    active = activeUsers,
                    pending = pendingUsers,
                    suspended = suspendedUsers,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecuritySummaryCard(
                        auditEvents = auditLogs.size,
                        elevatedAlerts = elevatedAlerts,
                        openTickets = openTickets,
                        modifier = Modifier.weight(1f)
                    )
                    BalanceSummaryCard(
                        paidDollars = paidDollars,
                        pendingDollars = pendingDollars,
                        paidCount = paymentRecords.count { it.status == "PAID" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSummaryCard(
    total: Int,
    active: Int,
    pending: Int,
    suspended: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("System Accounts", style = MaterialTheme.typography.titleSmall)
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = CodivaCobalt, modifier = Modifier.size(18.dp))
            }
            Text("$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Active: $active", style = MaterialTheme.typography.labelSmall, color = CodivaEmerald)
                Text("Pending: $pending", style = MaterialTheme.typography.labelSmall, color = CodivaAmber)
                Text("Paused: $suspended", style = MaterialTheme.typography.labelSmall, color = CodivaRose)
            }
        }
    }
}

@Composable
private fun SecuritySummaryCard(
    auditEvents: Int,
    elevatedAlerts: Int,
    openTickets: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Audit & Support", style = MaterialTheme.typography.titleSmall)
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = CodivaSkyBright, modifier = Modifier.size(18.dp))
            }
            Text("$auditEvents", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Elevated alerts: $elevatedAlerts • Open tickets: $openTickets",
                style = MaterialTheme.typography.labelSmall,
                color = if (elevatedAlerts > 0) CodivaAmber else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BalanceSummaryCard(
    paidDollars: Long,
    pendingDollars: Long,
    paidCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CodivaCobalt)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(CodivaCobalt, CodivaViolet)))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paid Revenue",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = CodivaSlateLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "$$paidDollars",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$paidCount paid • $$pendingDollars pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = CodivaSlateLight
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformDistributionBarCard(
    appReleases: List<AppRelease>,
    modifier: Modifier = Modifier
) {
    val activeReleases = remember(appReleases) {
        appReleases.filter { it.statusEnum != ReleaseStatus.RETIRED }
    }
    val platformCounts = remember(activeReleases) {
        ReleasePlatform.entries.map { platform ->
            platform to activeReleases.count { it.platformEnum == platform }
        }
    }
    val total = activeReleases.size.coerceAtLeast(1)
    val palette = mapOf(
        ReleasePlatform.ANDROID to CodivaCobalt,
        ReleasePlatform.IOS to CodivaSkyBright,
        ReleasePlatform.WINDOWS to CodivaViolet,
        ReleasePlatform.WEB to CodivaEmerald
    )

    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("platform_distribution_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Live Distribution Channels", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "${activeReleases.size} active release(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                platformCounts.forEach { (platform, count) ->
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .weight(count.toFloat() / total.toFloat())
                                .height(14.dp)
                                .background(palette[platform] ?: CodivaCobalt)
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platformCounts.forEach { (platform, count) ->
                    val pct = if (activeReleases.isEmpty()) 0 else ((count * 100f) / total).toInt()
                    val color = palette[platform] ?: CodivaCobalt
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = "${platform.label}: $count ($pct%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityHeatmapAndCalendarRow(
    registeredUsers: List<UserProfile>,
    auditLogs: List<AuditLogEntry>,
    inquiries: List<ProjectInquiry>,
    enrollments: List<CourseEnrollment>,
    publications: List<AdminPublication>,
    modifier: Modifier = Modifier
) {
    val allTimestamps = remember(registeredUsers, auditLogs, inquiries, enrollments, publications) {
        buildList<Timestamp> {
            registeredUsers.mapNotNullTo(this) { it.createdAt }
            auditLogs.mapNotNullTo(this) { it.createdAt }
            inquiries.mapNotNullTo(this) { it.createdAt }
            enrollments.mapNotNullTo(this) { it.createdAt }
            publications.mapNotNullTo(this) { it.createdAt }
        }
    }

    val heatmapGrid = remember(allTimestamps) {
        val grid = Array(7) { IntArray(6) } // 7 days (Sun..Sat) x 6 4-hour blocks
        val cal = Calendar.getInstance()
        allTimestamps.forEach { ts ->
            cal.time = ts.toDate()
            val dayIdx = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
            val slotIdx = (cal.get(Calendar.HOUR_OF_DAY) / 4).coerceIn(0, 5)
            grid[dayIdx][slotIdx]++
        }
        grid
    }

    val activeDaysOfMonth = remember(allTimestamps) {
        val now = Calendar.getInstance()
        val curYear = now.get(Calendar.YEAR)
        val curMonth = now.get(Calendar.MONTH)
        val cal = Calendar.getInstance()
        buildSet {
            allTimestamps.forEach { ts ->
                cal.time = ts.toDate()
                if (cal.get(Calendar.YEAR) == curYear && cal.get(Calendar.MONTH) == curMonth) {
                    add(cal.get(Calendar.DAY_OF_MONTH))
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("activity_heatmap_calendar")
    ) {
        val wide = maxWidth >= 680.dp
        if (wide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeatmapCard(
                    grid = heatmapGrid,
                    totalEvents = allTimestamps.size,
                    modifier = Modifier.weight(1.35f)
                )
                OperationsCalendarCard(
                    activeDays = activeDaysOfMonth,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeatmapCard(
                    grid = heatmapGrid,
                    totalEvents = allTimestamps.size,
                    modifier = Modifier.fillMaxWidth()
                )
                OperationsCalendarCard(
                    activeDays = activeDaysOfMonth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HeatmapCard(
    grid: Array<IntArray>,
    totalEvents: Int,
    modifier: Modifier = Modifier
) {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val slots = listOf("00h", "04h", "08h", "12h", "16h", "20h")
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly Activity Matrix", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$totalEvents timestamped events",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            days.forEachIndexed { dIdx, day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(30.dp)
                    )
                    for (sIdx in 0..5) {
                        val count = grid[dIdx][sIdx]
                        val cellColor = when {
                            count == 0 -> MaterialTheme.colorScheme.surfaceVariant
                            count <= 2 -> CodivaCobalt.copy(alpha = 0.3f)
                            count <= 5 -> CodivaCobalt.copy(alpha = 0.65f)
                            else -> CodivaCobalt
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                slots.forEach { slot ->
                    Text(
                        text = slot,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationsCalendarCard(
    activeDays: Set<Int>,
    modifier: Modifier = Modifier
) {
    val now = remember { Calendar.getInstance() }
    val monthTitle = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.time) }
    val today = now.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = remember {
        val cal = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = CodivaCobalt, modifier = Modifier.size(18.dp))
                    Text(monthTitle, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "${activeDays.size} active day(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            val totalCells = firstDayOffset + daysInMonth
            val weeks = (totalCells + 6) / 7
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (w in 0 until weeks) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0..6) {
                            val cellIndex = w * 7 + c
                            val dayNum = cellIndex - firstDayOffset + 1
                            if (dayNum in 1..daysInMonth) {
                                val isToday = dayNum == today
                                val hasActivity = dayNum in activeDays
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isToday -> CodivaCobalt
                                        hasActivity -> CodivaCobalt.copy(alpha = 0.16f)
                                        else -> Color.Transparent
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isToday || hasActivity) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
