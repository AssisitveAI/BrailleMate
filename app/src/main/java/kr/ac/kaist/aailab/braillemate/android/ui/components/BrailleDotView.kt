package kr.ac.kaist.aailab.braillemate.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 점자 6점 시각화 컴포넌트
 * 점 번호: 1(왼상) 4(오상)
 *         2(왼중) 5(오중)
 *         3(왼하) 6(오하)
 */
@Composable
fun BrailleDotView(
    dots: Set<Int>,
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    spacing: Dp = 4.dp,
    showNumbers: Boolean = false
) {
    val description = "점자: ${dots.sorted().joinToString(",") { "${it}점" }}"

    Column(
        modifier = modifier
            .semantics { contentDescription = description }
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(spacing * 2),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: dots 1, 4
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            BrailleDot(filled = 1 in dots, number = if (showNumbers) 1 else null, size = dotSize)
            BrailleDot(filled = 4 in dots, number = if (showNumbers) 4 else null, size = dotSize)
        }
        // Row 2: dots 2, 5
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            BrailleDot(filled = 2 in dots, number = if (showNumbers) 2 else null, size = dotSize)
            BrailleDot(filled = 5 in dots, number = if (showNumbers) 5 else null, size = dotSize)
        }
        // Row 3: dots 3, 6
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            BrailleDot(filled = 3 in dots, number = if (showNumbers) 3 else null, size = dotSize)
            BrailleDot(filled = 6 in dots, number = if (showNumbers) 6 else null, size = dotSize)
        }
    }
}

@Composable
private fun BrailleDot(
    filled: Boolean,
    number: Int?,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (filled) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (number != null) {
            Text(
                text = number.toString(),
                fontSize = (size.value * 0.5f).sp,
                color = if (filled) MaterialTheme.colorScheme.onSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
