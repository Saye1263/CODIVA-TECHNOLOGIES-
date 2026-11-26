package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AcademyProgram
import com.example.data.CodivaCatalog
import com.example.data.CourseEnrollment
import com.example.data.EnrollmentStatus
import com.example.data.NewsArticle
import com.example.ui.UiState
import com.example.ui.components.BadgeTone
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NewsDetailSheet
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CodivaEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(
    enrollmentsState: UiState<List<CourseEnrollment>>,
    isSubmitting: Boolean,
    onEnroll: (AcademyProgram) -> Unit,
    onAdvanceProgress: (CourseEnrollment) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Programs", "News & Press")
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    val enrollmentsByCourse = remember(enrollmentsState) {
        ((enrollmentsState as? UiState.Success)?.data ?: emptyList()).associateBy { it.courseId }
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
                    eyebrow = "Learn with CODIVA",
                    title = "Training, Certification & News"
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("academy_tab_bar")
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
                                .height(160.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_academy_banner),
                                contentDescription = "CODIVA Academy training banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Training and certification",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Training programs and certification programs from CODIVA. Enroll below and track your progress in real time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (enrollmentsState is UiState.Error) {
                item {
                    ErrorBanner(
                        message = enrollmentsState.message,
                        modifier = Modifier.widthIn(max = 860.dp)
                    )
                }
            }

            items(CodivaCatalog.academyPrograms, key = { it.id }) { program ->
                val enrollment = enrollmentsByCourse[program.id]
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${program.level} • ${program.duration}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            StatusBadge(
                                text = if (program.programType == "CERTIFICATION") "Certification" else "Training",
                                tone = if (program.programType == "CERTIFICATION") BadgeTone.SUCCESS else BadgeTone.INFO
                            )
                        }

                        Text(
                            text = program.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = program.headline,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = program.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (enrollment == null) {
                            Button(
                                onClick = { onEnroll(program) },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("enroll_${program.id}")
                            ) {
                                Text("Enroll in Program")
                            }
                        } else {
                            val isCompleted = enrollment.statusEnum == EnrollmentStatus.COMPLETED
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isCompleted) "Completed (100%)" else "Progress: ${enrollment.progressPercent}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isCompleted) CodivaEmerald else MaterialTheme.colorScheme.primary
                                    )
                                    StatusBadge(
                                        text = enrollment.statusEnum.label,
                                        tone = if (isCompleted) BadgeTone.SUCCESS else BadgeTone.INFO
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (enrollment.progressPercent.coerceIn(0L, 100L) / 100f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                                if (!isCompleted) {
                                    OutlinedButton(
                                        onClick = { onAdvanceProgress(enrollment) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("progress_${program.id}")
                                    ) {
                                        Text("Log Module (+25%)")
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = CodivaEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "CODIVA completion record verified in your Portal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CodivaEmerald
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            items(CodivaCatalog.newsArticles, key = { it.id }) { article ->
                Card(
                    modifier = Modifier
                        .widthIn(max = 860.dp)
                        .clickable { selectedArticle = article },
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = article.publishedDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(text = article.category, tone = BadgeTone.INFO)
                        }
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    selectedArticle?.let { article ->
        NewsDetailSheet(
            article = article,
            onDismiss = { selectedArticle = null }
        )
    }
}
