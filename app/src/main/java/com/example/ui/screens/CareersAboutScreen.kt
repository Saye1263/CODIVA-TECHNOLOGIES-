package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ApplicationStatus
import com.example.data.CareerApplication
import com.example.data.CareerRole
import com.example.data.CodivaCatalog
import com.example.ui.UiState
import com.example.ui.components.BadgeTone
import com.example.ui.components.CareerApplicationSheet
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareersAboutScreen(
    defaultApplicantName: String,
    initialTab: Int,
    applicationsState: UiState<List<CareerApplication>>,
    isSubmitting: Boolean,
    onApplyRole: (CareerRole, applicantName: String, portfolioNote: String, onDone: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Careers", "About & Team")
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var searchQuery by remember { mutableStateOf("") }
    var activeRoleForSheet by remember { mutableStateOf<CareerRole?>(null) }

    val applicationsByRole = remember(applicationsState) {
        ((applicationsState as? UiState.Success)?.data ?: emptyList()).associateBy { it.roleId }
    }

    val filteredRoles = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) CodivaCatalog.careerRoles
        else CodivaCatalog.careerRoles.filter {
            it.title.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.location.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    eyebrow = "People & Purpose",
                    title = "Careers & CODIVA Leadership"
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("careers_about_tab_bar")
                ) {
                    tabs.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        if (selectedTab == 0) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search careers") },
                    placeholder = { Text("Role, department, or Monrovia...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .widthIn(max = 860.dp)
                        .fillMaxWidth()
                        .testTag("careers_search_input")
                )
            }

            if (filteredRoles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.widthIn(max = 860.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("No matching openings", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Try searching for Engineering, Design, Operations, or Monrovia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredRoles, key = { it.id }) { role ->
                    val existingApp = applicationsByRole[role.id]
                    val alreadyApplied = existingApp != null && existingApp.statusEnum == ApplicationStatus.SUBMITTED
                    Card(
                        modifier = Modifier.widthIn(max = 860.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${role.department} • ${role.employmentType}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatusBadge(
                                    text = if (alreadyApplied) "Applied" else "Closes ${role.closingDate}",
                                    tone = if (alreadyApplied) BadgeTone.SUCCESS else BadgeTone.INFO
                                )
                            }
                            Text(
                                text = role.title,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = role.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = role.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { activeRoleForSheet = role },
                                enabled = !alreadyApplied,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("apply_${role.id}")
                            ) {
                                Icon(Icons.Rounded.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (alreadyApplied) "Application Submitted" else "Apply Now")
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_monrovia_vision),
                                contentDescription = "CODIVA Liberian-founded global vision",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_codiva_logo),
                                    contentDescription = "CODIVA Technologies Logo",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Text("About CODIVA Technologies", style = MaterialTheme.typography.headlineSmall)
                                    Text(
                                        text = CodivaCatalog.SLOGAN,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "Our History: CODIVA Technologies began in Liberia with a simple idea: good technology should be built close to the people and businesses it serves.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Our Vision: A technology company with a global reach, rooted in Liberia — writing software that is sound, making things people can use, and bringing ideas together.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Our Mission: Building digital solutions. Connecting people. Empowering businesses.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(CodivaCatalog.teamMembers, key = { it.name }) { member ->
                val isFounder = member.group == "Founder"
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFounder) {
                            Image(
                                painter = painterResource(id = R.drawable.img_founder_ceo),
                                contentDescription = member.name,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(CodivaNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.initials,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CodivaSky,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                StatusBadge(text = member.group, tone = if (isFounder) BadgeTone.SUCCESS else BadgeTone.NEUTRAL)
                            }
                            Text(
                                text = member.role,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = member.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    activeRoleForSheet?.let { role ->
        CareerApplicationSheet(
            role = role,
            defaultName = defaultApplicantName,
            isSubmitting = isSubmitting,
            onDismiss = { activeRoleForSheet = null },
            onSubmit = { name, note ->
                onApplyRole(role, name, note) {
                    activeRoleForSheet = null
                }
            }
        )
    }
}
