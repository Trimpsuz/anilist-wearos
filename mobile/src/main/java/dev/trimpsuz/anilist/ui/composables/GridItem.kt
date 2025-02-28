package dev.trimpsuz.anilist.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.trimpsuz.anilist.GetMediaListEntriesQuery

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GridItem(
    entry: GetMediaListEntriesQuery.Entry,
    isSelected: Boolean,
    onClick: (GetMediaListEntriesQuery.Entry) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(entry) }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        GlideImage(
            model = entry.media?.coverImage?.large,
            contentDescription = "Cover Image",
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(3f / 4f),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize().aspectRatio(3f / 4f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x80000000),
                            Color.Transparent,
                            Color.Transparent
                        ),
                    )
                )
        )

        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            text = entry.media?.title?.userPreferred ?: "Title",
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(
                        topEnd = 12.dp,
                    )
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = entry.progress?.let { progress ->
                    "${if (progress > 0) progress else 0}${entry.media?.episodes?.let { "/$it" } ?: entry.media?.chapters?.let { "/$it" } ?: ""}"
                } ?: entry.media?.episodes?.let { "0/$it" } ?: entry.media?.chapters?.let { "0/$it" } ?: "0",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}