package app.dinar.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dinar.core.common.format.DateFormatter
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType

/** One transaction in a list: category badge, title, meta line and signed amount. */
@Composable
fun TransactionRow(
    details: TransactionDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: JalaliDate = JalaliDate.today(),
    showDate: Boolean = true,
) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    val t = details.transaction

    val (icon, tint) = when (t.type) {
        TransactionType.TRANSFER -> DinarIcons.Transfer to colors.transfer
        else -> (details.category?.icon?.imageVector ?: DinarIcons.Receipt) to
            (details.category?.color ?: MaterialTheme.colorScheme.primary)
    }
    val title = when (t.type) {
        TransactionType.TRANSFER -> "انتقال به ${details.toAccount?.name.orEmpty()}"
        else -> t.note.ifBlank { details.category?.name ?: "بدون دسته" }
    }
    val meta = buildList {
        if (t.type != TransactionType.TRANSFER && t.note.isNotBlank()) details.category?.name?.let(::add)
        add(details.account.name)
        if (showDate) {
            add(DateFormatter.relativeDay(t.occurredAt, today, persianDigits = settings.persianDigits))
        } else {
            add(DateFormatter.time(t.occurredAt, persianDigits = settings.persianDigits))
        }
    }.joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBadge(icon = icon, tint = tint)
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AmountText(
            rial = when (t.type) {
                TransactionType.INCOME -> t.amount
                TransactionType.EXPENSE -> -t.amount
                TransactionType.TRANSFER -> t.amount
            },
            signed = t.type != TransactionType.TRANSFER,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = when (t.type) {
                TransactionType.INCOME -> colors.income
                TransactionType.EXPENSE -> MaterialTheme.colorScheme.onSurface
                TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
