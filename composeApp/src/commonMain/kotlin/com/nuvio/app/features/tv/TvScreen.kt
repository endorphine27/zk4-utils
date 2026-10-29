package com.nuvio.app.features.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nuvio.app.features.catalog.CatalogRepository
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.HomeRepository
import com.nuvio.app.features.home.MetaPreview

private val TileMinWidth = 168.dp
private val TileImageHeight = 112.dp

private fun HomeCatalogSection.isTvSection(): Boolean =
    target.contentType.equals("tv", ignoreCase = true) ||
        items.any { it.type.equals("tv", ignoreCase = true) }

/** Live TV channels: the full "tv" catalog shown as a grid of channel tiles. */
@Composable
fun TvScreen(
    modifier: Modifier = Modifier,
    topChromePadding: Dp? = null,
    onChannelClick: ((MetaPreview) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") onSectionClick: ((HomeCatalogSection) -> Unit)? = null,
) {
    val homeState by HomeRepository.uiState.collectAsState()
    val sections = remember(homeState.sections) {
        homeState.sections.filter { it.isTvSection() && it.items.isNotEmpty() }
    }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    val selected = sections.firstOrNull { it.key == selectedKey } ?: sections.firstOrNull()

    LaunchedEffect(selected?.target) {
        selected?.let { CatalogRepository.load(it.target) }
    }
    val catalogState by CatalogRepository.uiState.collectAsState()
    val channels = when {
        selected == null -> emptyList()
        catalogState.items.isNotEmpty() && !catalogState.isLoading -> catalogState.items
        else -> selected.items
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = topChromePadding ?: 0.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = "TV",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        if (sections.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sections.forEach { section ->
                    val isSelected = section.key == selected?.key
                    Text(
                        text = section.title,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            )
                            .clickable { selectedKey = section.key }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        } else if (selected != null) {
            Text(
                text = selected.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
        }

        if (selected == null) {
            Text(
                text = if (homeState.isLoading) {
                    "Se încarcă canalele..."
                } else {
                    "Nu am găsit canale TV. Adaugă un addon cu canale (tip „tv”) din Addonuri și activează-i cataloagele pe ecranul principal."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(TileMinWidth),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(channels, key = { "${it.type}:${it.id}" }) { channel ->
                    ChannelTile(channel = channel, onClick = { onChannelClick?.invoke(channel) })
                }
                if (catalogState.canLoadMore) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "load-more-${channels.size}") {
                        LaunchedEffect(channels.size) { CatalogRepository.loadMore() }
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelTile(
    channel: MetaPreview,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TileImageHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = channel.poster ?: channel.logo ?: channel.landscapePoster,
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize().padding(10.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Text(
            text = channel.name,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
