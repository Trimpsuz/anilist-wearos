package dev.trimpsuz.anilist.tile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.apollographql.apollo.ApolloClient
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.anilist.type.MediaListStatus
import dev.trimpsuz.anilist.utils.DataStoreRepository
import dev.trimpsuz.anilist.utils.GlobalVariables
import dev.trimpsuz.anilist.utils.REFRESH_INTERVAL_TILE
import dev.trimpsuz.anilist.utils.fetchMedia
import dev.trimpsuz.anilist.utils.firstBlocking
import dev.trimpsuz.anilist.utils.sendToMobile
import dev.trimpsuz.anilist.utils.updateMediaProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
class MainTileService : SuspendingTileService() {
    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    @Inject
    lateinit var globalVariables: GlobalVariables

    @Inject
    lateinit var apolloClient: ApolloClient

    override fun onCreate() {
        super.onCreate()
        if(globalVariables.accessToken == null) globalVariables.accessToken = dataStoreRepository.accessToken.firstBlocking()
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        val builder = ResourceBuilders.Resources.Builder().setVersion(globalVariables.RESOURCES_VERSION)

        val filesDir = applicationContext.filesDir
        val imageFiles = filesDir.listFiles { file -> file.extension == "png"}

        imageFiles?.forEach { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val byteArr = bitmapToByteArray(bitmap)
            val imageResource = ResourceBuilders.ImageResource.Builder()
                .setInlineResource(
                    ResourceBuilders.InlineImageResource.Builder()
                        .setData(byteArr)
                        .setWidthPx(bitmap.width)
                        .setHeightPx(bitmap.height)
                        .setFormat(ResourceBuilders.IMAGE_FORMAT_UNDEFINED)
                        .build()
                )
                .build()

            builder.addIdToImageMapping(file.nameWithoutExtension, imageResource)
        }

        return builder.build()
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        if(requestParams.currentState.lastClickableId.startsWith("ID_CLICK_ADD")) {
            val mediaId = requestParams.currentState.lastClickableId.split(":")[1]

            withContext(Dispatchers.IO) {
                val media = fetchMedia(apolloClient, listOf(mediaId.toInt()))?.get(0)
                val entryId = media?.mediaListEntry?.id
                val progress = media?.mediaListEntry?.progress?.plus(1)

                if(entryId != null && progress != null) {
                    updateMediaProgress(apolloClient, entryId, progress)

                    val total: Int? = media.episodes ?: media.chapters

                    if(total != null && progress >= total) {
                        val selectedMedia = dataStoreRepository.selectedMedia.firstBlocking()?.toList() ?: emptyList()

                        if(selectedMedia.isNotEmpty()) {
                            val newSelectedMedia = selectedMedia.filter { it != mediaId }.toSet()
                            sendToMobile("list", newSelectedMedia.toString(), applicationContext)
                            dataStoreRepository.setSelectedMedia(newSelectedMedia)
                            val imageFiles = filesDir.listFiles { file -> file.extension == "png"}
                            imageFiles?.forEach { file ->
                                if(!newSelectedMedia.contains(file.nameWithoutExtension)) file.delete()
                            }
                        }
                    }
                }
            }
        }

        val timelineEntry = TimelineBuilders.TimelineEntry.Builder()
            .setLayout(
                LayoutElementBuilders.Layout.Builder()
                    .setRoot(tileLayout(requestParams, this))
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(globalVariables.RESOURCES_VERSION)
            .setFreshnessIntervalMillis(REFRESH_INTERVAL_TILE)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(timelineEntry)
                    .build()
            )
            .build()
    }

    @androidx.annotation.OptIn(ProtoLayoutExperimental::class)
    private suspend fun tileLayout(
        requestParams: RequestBuilders.TileRequest,
        context: Context,
    ): LayoutElementBuilders.LayoutElement {
        val isLoggedIn: Boolean = withContext(Dispatchers.IO) {
            dataStoreRepository.isLoggedIn.firstBlocking()
        }

        if (!isLoggedIn) return createTextTile(requestParams, context, "Please log in in the companion app.")

        var selectedMediaIds: List<String> = withContext(Dispatchers.IO) {
            dataStoreRepository.selectedMedia.firstBlocking()?.toList() ?: emptyList()
        }

        if(selectedMediaIds.isEmpty()) return createTextTile(requestParams, context, "Please select some entries in the companion app.")

        val mediaList = withContext(Dispatchers.IO) {
            fetchMedia(apolloClient, selectedMediaIds.map { it.toInt() })
        }

        if (mediaList.isNullOrEmpty()) return createTextTile(requestParams, context, "No media entries returned from the API.")

        val mediaMap = mediaList.associate { media ->
            val total = media?.episodes ?: media?.chapters
            val progress = media?.mediaListEntry?.progress
            (media?.id ?: 0) to Pair(progress, total)
        }

        withContext(Dispatchers.IO) {
            val newSelectedMediaIds = selectedMediaIds.toMutableList()
            mediaList.forEach { media ->
                if (media?.mediaListEntry?.status !in listOf(
                        MediaListStatus.CURRENT,
                        MediaListStatus.REPEATING
                    )
                ) {
                    newSelectedMediaIds.remove(media?.id.toString())
                }
            }
            selectedMediaIds = newSelectedMediaIds.toList()
            sendToMobile("list", selectedMediaIds.toString(), applicationContext)
            dataStoreRepository.setSelectedMedia(selectedMediaIds.toSet())
            val imageFiles = filesDir.listFiles { file -> file.extension == "png" }
            imageFiles?.forEach { file ->
                if (!selectedMediaIds.contains(file.nameWithoutExtension)) file.delete()
            }
        }

        return PrimaryLayout.Builder(requestParams.deviceConfiguration)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                LayoutElementBuilders.Box.Builder()
                    .addContent(
                        LayoutElementBuilders.Row.Builder().apply {
                            selectedMediaIds.forEachIndexed { index, mediaId ->
                                val (progress, total) = mediaMap[mediaId.toInt()]
                                    ?: Pair(null, null)
                                if (index in 1..2) {
                                    addContent(
                                        Spacer.Builder().setWidth(DimensionBuilders.dp(4f)).build()
                                    )
                                    addContent(
                                        LayoutElementBuilders.Box.Builder()
                                            .setWidth(DimensionBuilders.dp(2f))
                                            .setHeight(DimensionBuilders.expand())
                                            .setModifiers(
                                                ModifiersBuilders.Modifiers.Builder()
                                                    .setBackground(
                                                        ModifiersBuilders.Background.Builder()
                                                            .setColor(argb(Color.parseColor("#8a8a8a")))
                                                            .setCorner(
                                                                ModifiersBuilders.Corner.Builder()
                                                                    .setRadius(DimensionBuilders.dp(100f))
                                                                    .build()
                                                            ).build()
                                                    ).build()
                                            ).build()
                                    )
                                    addContent(
                                        Spacer.Builder().setWidth(DimensionBuilders.dp(4f)).build()
                                    )
                                }
                                addContent(
                                    LayoutElementBuilders.Box.Builder()
                                        .addContent(
                                            LayoutElementBuilders.Column.Builder()
                                                .addContent(
                                                    LayoutElementBuilders.Box.Builder()
                                                        .addContent(
                                                            LayoutElementBuilders.Image.Builder()
                                                                .setResourceId(mediaId)
                                                                .apply {
                                                                    if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                                        setWidth(DimensionBuilders.dp(54f))
                                                                        setHeight(DimensionBuilders.dp(77f))
                                                                    } else {
                                                                        setWidth(DimensionBuilders.dp(45f))
                                                                        setHeight(DimensionBuilders.dp(65f))
                                                                    }
                                                                }.build()
                                                        ).setModifiers(
                                                            ModifiersBuilders.Modifiers.Builder()
                                                                .setBackground(
                                                                    ModifiersBuilders.Background.Builder()
                                                                        .setCorner(
                                                                            ModifiersBuilders.Corner.Builder()
                                                                                .setRadius(
                                                                                    DimensionBuilders.dp(8f)
                                                                                )
                                                                                .build()
                                                                        ).build()
                                                                ).build()
                                                        ).build()
                                                ).addContent(
                                                    Spacer.Builder().apply {
                                                        if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                            setHeight(DimensionBuilders.dp(2f))
                                                        } else {
                                                            setHeight(DimensionBuilders.dp(4f))
                                                        }
                                                    }.build()
                                                ).addContent(
                                                    LayoutElementBuilders.Row.Builder()
                                                        .addContent(
                                                            LayoutElementBuilders.Column.Builder()
                                                                .apply {
                                                                    addContent(
                                                                        LayoutElementBuilders.Text.Builder()
                                                                            .setText(progress.toString())
                                                                            .setFontStyle(
                                                                                LayoutElementBuilders.FontStyle.Builder()
                                                                                    .setSize(
                                                                                        DimensionBuilders.SpProp.Builder()
                                                                                            .apply {
                                                                                                if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                                                                    setValue(12f)
                                                                                                } else {
                                                                                                    setValue(10f)
                                                                                                }
                                                                                            }.build()
                                                                                    )
                                                                                    .setVariant(
                                                                                        LayoutElementBuilders.FontVariantProp.Builder()
                                                                                            .setValue(LayoutElementBuilders.FONT_VARIANT_BODY)
                                                                                            .build()
                                                                                    )
                                                                                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_MEDIUM)
                                                                                    .setColor(argb(Colors.DEFAULT.onSurface))
                                                                                    .build()
                                                                            ).setModifiers(
                                                                                ModifiersBuilders.Modifiers.Builder()
                                                                                    .setPadding(
                                                                                        ModifiersBuilders.Padding.Builder().apply {
                                                                                            if(total == null) {
                                                                                                setTop(DimensionBuilders.dp(7f))
                                                                                                setBottom(DimensionBuilders.dp(7f))
                                                                                            }
                                                                                        }.build()
                                                                                    ).build()
                                                                            ).build()
                                                                    )
                                                                    if(total != null) {
                                                                        addContent(
                                                                            LayoutElementBuilders.Box.Builder()
                                                                                .setWidth(DimensionBuilders.expand())
                                                                                .setHeight(DimensionBuilders.dp(2f))
                                                                                .setModifiers(
                                                                                    ModifiersBuilders.Modifiers.Builder()
                                                                                        .setBackground(
                                                                                            ModifiersBuilders.Background.Builder()
                                                                                                .setColor(argb(Color.parseColor("#8a8a8a")))
                                                                                                .setCorner(
                                                                                                    ModifiersBuilders.Corner.Builder()
                                                                                                        .setRadius(DimensionBuilders.dp(100f))
                                                                                                        .build()
                                                                                                )
                                                                                                .build()
                                                                                        ).build()
                                                                                ).build()
                                                                        )
                                                                        addContent(
                                                                            LayoutElementBuilders.Text.Builder()
                                                                                .setText(total.toString())
                                                                                .setFontStyle(
                                                                                    LayoutElementBuilders.FontStyle.Builder()
                                                                                        .setSize(
                                                                                            DimensionBuilders.SpProp.Builder()
                                                                                                .apply {
                                                                                                    if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                                                                        setValue(12f)
                                                                                                    } else {
                                                                                                        setValue(10f)
                                                                                                    }
                                                                                                }
                                                                                                .build()
                                                                                        )
                                                                                        .setVariant(
                                                                                            LayoutElementBuilders.FontVariantProp.Builder()
                                                                                                .setValue(LayoutElementBuilders.FONT_VARIANT_BODY)
                                                                                                .build()
                                                                                        )
                                                                                        .setWeight(LayoutElementBuilders.FONT_WEIGHT_MEDIUM)
                                                                                        .setColor(argb(Colors.DEFAULT.onSurface))
                                                                                        .build()
                                                                                ).build()
                                                                        )
                                                                    }
                                                                }.build()
                                                        )
                                                        .addContent(
                                                            Spacer.Builder()
                                                                .setWidth(DimensionBuilders.dp(4f))
                                                                .build()
                                                        )
                                                        .addContent(
                                                            LayoutElementBuilders.Box.Builder()
                                                                .addContent(
                                                                    LayoutElementBuilders.Text.Builder()
                                                                        .setText("+")
                                                                        .setFontStyle(
                                                                            LayoutElementBuilders.FontStyle.Builder()
                                                                                .setSize(
                                                                                    DimensionBuilders.SpProp.Builder()
                                                                                        .apply {
                                                                                            if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                                                                setValue(12f)
                                                                                            } else {
                                                                                                setValue(10f)
                                                                                            }
                                                                                        }.build()
                                                                                )
                                                                                .setVariant(
                                                                                    LayoutElementBuilders.FontVariantProp.Builder()
                                                                                        .setValue(
                                                                                            LayoutElementBuilders.FONT_VARIANT_BODY
                                                                                        ).build()
                                                                                )
                                                                                .setWeight(LayoutElementBuilders.FONT_WEIGHT_MEDIUM)
                                                                                .setColor(argb(Colors.DEFAULT.onPrimary))
                                                                                .build()
                                                                        )
                                                                        .build()
                                                                )
                                                                .setModifiers(
                                                                    ModifiersBuilders.Modifiers.Builder()
                                                                        .setPadding(
                                                                            ModifiersBuilders.Padding.Builder()
                                                                                .apply {
                                                                                    if (requestParams.deviceConfiguration.screenHeightDp >= 225) {
                                                                                        setAll(DimensionBuilders.dp(4f))
                                                                                        setStart(DimensionBuilders.dp(8f))
                                                                                        setEnd(DimensionBuilders.dp(8f))
                                                                                    } else {
                                                                                        setAll(DimensionBuilders.dp(4f))
                                                                                        setStart(DimensionBuilders.dp(7f))
                                                                                        setEnd(DimensionBuilders.dp(7f))
                                                                                    }
                                                                                }.build()
                                                                        )
                                                                        .setBackground(
                                                                            ModifiersBuilders.Background.Builder()
                                                                                .setCorner(
                                                                                    ModifiersBuilders.Corner.Builder()
                                                                                        .setRadius(DimensionBuilders.dp(100f))
                                                                                        .build()
                                                                                )
                                                                                .setColor(
                                                                                    argb(Color.parseColor("#75acff"))
                                                                                )
                                                                                .build()
                                                                        )
                                                                        .setClickable(
                                                                            Clickable.Builder()
                                                                                .setId("ID_CLICK_ADD:$mediaId")
                                                                                .setOnClick(
                                                                                    ActionBuilders.LoadAction.Builder()
                                                                                        .build()
                                                                                ).build()
                                                                        ).build()
                                                                ).build()
                                                        ).build()
                                                ).build()
                                        )
                                        .setModifiers(
                                            ModifiersBuilders.Modifiers.Builder()
                                                .setPadding(
                                                    ModifiersBuilders.Padding.Builder()
                                                        .setTop(DimensionBuilders.dp(8f))
                                                        .setBottom(DimensionBuilders.dp(6f))
                                                        .build()
                                                ).build()
                                        ).build()
                                )
                            }
                        }.build()
                    )
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder().setId("ID_REFRESH").setOnClick(
                                    ActionBuilders.LoadAction.Builder().build()
                                ).build()
                            )
                            .build()
                    ).build()
            ).build()
    }

    private fun createTextTile(
        requestParams: RequestBuilders.TileRequest,
        context: Context,
        message: String
    ): LayoutElementBuilders.LayoutElement {
        return PrimaryLayout.Builder(requestParams.deviceConfiguration)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder(context, message)
                    .setMaxLines(3)
                    .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .build()
            )
            .build()
    }
}