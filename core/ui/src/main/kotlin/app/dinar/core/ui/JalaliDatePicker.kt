package app.dinar.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.designsystem.icon.DinarIcons

private val WEEK_HEADERS = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

/**
 * A Material 3 styled date picker for the Jalali calendar. Weeks start on Saturday,
 * as they do in Iran. Future dates can be disabled with [allowFuture].
 */
@Composable
fun JalaliDatePickerDialog(
    initial: JalaliDate,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit,
    allowFuture: Boolean = true,
) {
    val settings = LocalDisplaySettings.current
    val today = JalaliDate.today()
    var selected by rememberSaveable(stateSaver = JalaliSaver) { mutableStateOf(initial) }
    var shown by rememberSaveable(stateSaver = JalaliSaver) { mutableStateOf(initial.firstDayOfMonth()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(selected) }) { Text("تأیید") } },
        dismissButton = {
            Row {
                TextButton(onClick = { selected = today; shown = today.firstDayOfMonth() }) { Text("امروز") }
                TextButton(onClick = onDismiss) { Text("انصراف") }
            }
        },
        title = { Text(settings.fullDate(selected), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { shown = shown.plusMonths(-1) }) {
                        Icon(DinarIcons.PreviousMonth, contentDescription = "ماه قبل")
                    }
                    Text(
                        settings.monthYear(shown),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    IconButton(onClick = { shown = shown.plusMonths(1) }) {
                        Icon(DinarIcons.NextMonth, contentDescription = "ماه بعد")
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    WEEK_HEADERS.forEach {
                        Text(
                            it,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val offset = (shown.toLocalDate().dayOfWeek.value + 1) % 7
                val cells = List(offset) { null } + (1..shown.lengthOfMonth).map { shown.copy(day = it) }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (date != null) {
                                    val enabled = allowFuture || date <= today
                                    DayCell(
                                        label = settings.number(date.day),
                                        isSelected = date == selected,
                                        isToday = date == today,
                                        enabled = enabled,
                                        description = settings.fullDate(date),
                                        onClick = { selected = date },
                                    )
                                }
                            }
                        }
                        repeat(7 - week.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
        },
    )
}

@Composable
private fun DayCell(
    label: String,
    isSelected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    description: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (isSelected) scheme.primary else Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(1.dp, scheme.primary, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> scheme.onPrimary
                !enabled -> scheme.onSurface.copy(alpha = 0.38f)
                else -> scheme.onSurface
            },
        )
    }
}

private val JalaliSaver = androidx.compose.runtime.saveable.Saver<JalaliDate, IntArray>(
    save = { intArrayOf(it.year, it.month, it.day) },
    restore = { JalaliDate(it[0], it[1], it[2]) },
)
