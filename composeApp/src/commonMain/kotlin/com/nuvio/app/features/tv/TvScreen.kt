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
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.catalog.CatalogRepository
import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.buildHomeCatalogDefinitions

private val TileMinWidth = 168.dp
private val TileImageHeight = 112.dp

private data class TvCatalog(
    val key: String,
    val title: String,
    val target: CatalogTarget,
)

/** Live TV channels: every addon catalog of type "tv" (independent of what is shown on the home screen), as a grid. */
@Composable
fun TvScreen(
    modifier: Modifier = Modifier,
    topChromePadding: Dp? = null,
    onChannelClick: ((MetaPreview) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") onSectionClick: ((HomeCatalogSection) -> Unit)? = null,
) {
    val addonsState by AddonRepository.uiState.collectAsState()
    val catalogs = remember(addonsState.addons) {
        buildHomeCatalogDefinitions(addonsState.addons)
            .filter { it.serverConnectionId == null && it.type.equals("tv", ignoreCase = true) }
            .map { definition ->
                TvCatalog(
                    key = definition.key,
                    title = definition.catalogName,
                    target = CatalogTarget.Addon(
                        manifestUrl = definition.manifestUrl,
                        contentType = definition.type,
                        catalogId = definition.catalogId,
                        supportsPagination = definition.supportsPagination,
                    ),
                )
            }
    }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    val selected = catalogs.firstOrNull { it.key == selectedKey } ?: catalogs.firstOrNull()

    LaunchedEffect(selected?.target) {
        selected?.let { CatalogRepository.load(it.target) }
    }
    val catalogState by CatalogRepository.uiState.collectAsState()
    val channels = if (selected == null) emptyList() else catalogState.items

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
        if (catalogs.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                catalogs.forEach { section ->
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
                text = "Nu am găsit canale TV. Adaugă un addon cu canale (tip „tv”) din Addonuri.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (channels.isEmpty()) {
            Text(
                text = catalogState.errorMessage ?: if (catalogState.isLoading) "Se încarcă canalele..." else "Catalogul nu are canale.",
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
