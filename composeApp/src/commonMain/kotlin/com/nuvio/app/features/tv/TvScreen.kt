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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.HomeRepository
import com.nuvio.app.features.home.MetaPreview

private val TileWidth = 168.dp
private val TileHeight = 112.dp

private fun HomeCatalogSection.isTvSection(): Boolean =
    target.contentType.equals("tv", ignoreCase = true) ||
        items.any { it.type.equals("tv", ignoreCase = true) }

/** Live TV channels: every home catalog whose content type is "tv", shown as rows of channel tiles. */
@Composable
fun TvScreen(
    modifier: Modifier = Modifier,
    topChromePadding: Dp? = null,
    onChannelClick: ((MetaPreview) -> Unit)? = null,
    onSectionClick: ((HomeCatalogSection) -> Unit)? = null,
) {
    val homeState by HomeRepository.uiState.collectAsState()
    val sections = remember(homeState.sections) {
        homeState.sections.filter { it.isTvSection() && it.items.isNotEmpty() }
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
        if (sections.isEmpty()) {
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(sections, key = { it.key }) { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = section.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (onSectionClick != null) {
                                Text(
                                    text = "Vezi tot",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSectionClick(section) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(section.items, key = { "${it.type}:${it.id}" }) { channel ->
                                ChannelTile(channel = channel, onClick = { onChannelClick?.invoke(channel) })
                            }
                        }
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
            .width(TileWidth)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .width(TileWidth)
                .height(TileHeight)
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
