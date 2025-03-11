package dev.trimpsuz.anilist.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.trimpsuz.anilist.GetMediaListEntriesQuery
import dev.trimpsuz.anilist.presentation.viewModels.MediaListViewModel
import dev.trimpsuz.anilist.type.MediaListStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ListItem(entry: GetMediaListEntriesQuery.Entry, mediaListViewModel: MediaListViewModel) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    14.dp,
                )
            )
            .fillMaxWidth()
            .padding(
                vertical = 6.dp,
                horizontal = 8.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .weight(1f)
            ) {
                GlideImage(
                    model = entry.media?.coverImage?.medium,
                    contentDescription = "Cover Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(3f / 4f),
                    contentScale = ContentScale.FillBounds,
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(
                                topEnd = 4.dp,
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = when (entry.status) {
                            MediaListStatus.CURRENT -> Icons.Outlined.PlayCircleOutline
                            MediaListStatus.PLANNING -> Icons.Outlined.Schedule
                            MediaListStatus.COMPLETED -> Icons.Outlined.CheckCircle
                            MediaListStatus.DROPPED -> Icons.Outlined.DeleteOutline
                            MediaListStatus.PAUSED -> Icons.Outlined.PauseCircle
                            MediaListStatus.REPEATING -> Icons.Outlined.Replay
                            MediaListStatus.UNKNOWN__ -> Icons.AutoMirrored.Outlined.ListAlt
                            null -> Icons.AutoMirrored.Outlined.ListAlt
                        },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
            ) {
                Text(
                    text = entry.media?.title?.userPreferred ?: "Title",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(2f))

                Text(
                    text = entry.progress?.let { progress ->
                        "${if (progress > 0) progress else 0}${entry.media?.episodes?.let { "/$it" } ?: entry.media?.chapters?.let { "/$it" } ?: ""}"
                    } ?: entry.media?.episodes?.let { "0/$it" }
                    ?: entry.media?.chapters?.let { "0/$it" } ?: "0",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(
                                100.dp,
                            )
                        )
                ) {
                    if(entry.media?.episodes != null || entry.media?.chapters != null) {
                        var fraction = 0f
                        if(entry.progress != null) fraction =
                            (entry.progress.toFloat() / (entry.media.episodes ?: entry.media.chapters
                            ?: 0))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(2.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(
                                        100.dp,
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clickable {
                        scope.launch(Dispatchers.IO) {
                            if(entry.media != null) mediaListViewModel.updateMediaProgress(entry.media.id, entry.id)
                        }
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(
                            100.dp,
                        )
                    )
                    .padding(
                        vertical = 6.dp,
                        horizontal = 10.dp
                    )
            ) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}