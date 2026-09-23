package com.freeb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.freeb.app.ui.Adaptive
import com.freeb.app.ui.components.FreebIconButton
import kotlin.math.max

private val Accent = Color(0xFFFFD54A)
private val Panel = Color(0xFF111111)
private val Line = Color.White.copy(.10f)
private val Muted = Color.White.copy(.56f)

enum class Screen { CHATS, STORIES, CAMERA, DISCOVER, PROFILE;
    companion object {
        fun fromIndex(index: Int) = entries.getOrElse(index) { CAMERA }
    }
}

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    HomeScaffold { }
}

@Composable
fun ChatListScreen(onOpenChat: () -> Unit = {}) {
    ResponsivePage {
        PageTopBar(
            left = Icons.Filled.PersonOutline,
            right = listOf(Icons.Filled.Search, Icons.Filled.Edit),
            onLeft = {},
            onRight = { if (it == 0) {} else {} }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it)),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(Icons.Filled.GroupAdd)
                    ActionTile(Icons.Filled.CameraAlt)
                    ActionTile(Icons.Filled.PersonAddAlt1)
                    ActionTile(Icons.Filled.Search)
                }
            }
            items((0 until 12).toList()) { index ->
                ChatRow(index, onOpenChat)
            }
        }
    }
}

@Composable
fun StoriesListScreen(onOpenStory: () -> Unit = {}) {
    ResponsivePage {
        PageTopBar(
            left = Icons.Filled.PersonOutline,
            right = listOf(Icons.Filled.Search, Icons.Filled.Add),
            onLeft = {},
            onRight = {}
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it))
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items((0 until 10).toList()) { index ->
                    StoryBubble(index, onOpenStory)
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) { StoryCard(it, onOpenStory) }
            }
        }
    }
}

@Composable
fun DiscoverScreen(onOpenStory: () -> Unit = {}) {
    ResponsivePage {
        PageTopBar(
            left = Icons.Filled.Search,
            right = listOf(Icons.Filled.Tune, Icons.Filled.NotificationsNone),
            onLeft = {},
            onRight = {}
        )
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it)),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { ChipVisual(it) }
                }
            }
            items((0 until 8).toList()) { index ->
                DiscoveryCard(index, onOpenStory)
            }
        }
    }
}

@Composable
fun ProfileScreen(onSettings: () -> Unit = {}) {
    ResponsivePage {
        Row(
            Modifier.fillMaxWidth().padding(
                horizontal = Adaptive.safeContentPadding(it),
                vertical = Adaptive.scaled(12.dp, it)
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FreebIconButton(Icons.Filled.ArrowBack, Adaptive.adaptiveIcon(it, true), {}, false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FreebIconButton(Icons.Filled.Share, Adaptive.adaptiveIcon(it, true), {})
                FreebIconButton(Icons.Filled.Settings, Adaptive.adaptiveIcon(it, true), onSettings)
                FreebIconButton(Icons.Filled.MoreVert, Adaptive.adaptiveIcon(it, true), {})
            }
        }

        Column(
            Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(104.dp, 0, modifier = Modifier.padding(top = 16.dp))
            Row(
                Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                StatVisual()
                StatVisual()
                StatVisual()
            }
            Row(
                Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PillVisual(Icons.Filled.Edit)
                PillVisual(Icons.Filled.PersonAddAlt1)
                PillVisual(Icons.Filled.Share)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabIcon(Icons.Filled.GridView, true)
                TabIcon(Icons.Filled.StarBorder, false)
                TabIcon(Icons.Filled.Home, false)
            }
            ProfileGrid()
        }
    }
}

@Composable
fun ChatDetailScreen(onBack: () -> Unit = {}) {
    ResponsivePage {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Adaptive.safeContentPadding(it), vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FreebIconButton(Icons.Filled.ArrowBack, Adaptive.adaptiveIcon(it, true), onBack)
            Avatar(38.dp, 1)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FreebIconButton(Icons.Filled.Call, Adaptive.adaptiveIcon(it, true), {})
                FreebIconButton(Icons.Filled.Videocam, Adaptive.adaptiveIcon(it, true), {})
                FreebIconButton(Icons.Filled.MoreVert, Adaptive.adaptiveIcon(it, true), {})
            }
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it)),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items((0 until 14).toList()) { index ->
                MessageBubble(index)
            }
        }
        ComposerBar(
            Modifier.align(Alignment.BottomCenter).padding(horizontal = Adaptive.safeContentPadding(it), vertical = 10.dp)
        )
    }
}

@Composable
fun StoryViewerScreen(onBack: () -> Unit = {}) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF292929), Color.Black)))) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Adaptive.safeContentPadding(scale), vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(5) { Box(Modifier.weight(1f).size(height = 3.dp).background(Color.White.copy(if (it == 0) 1f else .25f), RoundedCornerShape(3.dp))) }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Adaptive.safeContentPadding(scale), vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(38.dp, 3)
                    Dot(6.dp, Accent)
                }
                FreebIconButton(Icons.Filled.Close, Adaptive.adaptiveIcon(scale, true), onBack)
            }
            Box(Modifier.align(Alignment.Center).size(120.dp).background(Color.White.copy(.08f), RoundedCornerShape(30.dp))) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(48.dp))
            }
            ComposerBar(Modifier.align(Alignment.BottomCenter).padding(Adaptive.safeContentPadding(scale)))
        }
    }
}

@Composable
fun MediaEditorScreen(onBack: () -> Unit = {}) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().padding(horizontal = Adaptive.scaled(12.dp, scale), vertical = Adaptive.scaled(18.dp, scale)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(.66f).clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF303030), Color(0xFF101010), Color(0xFF393939))))
                )
            }
            FreebIconButton(Icons.Filled.Close, Adaptive.adaptiveIcon(scale), onBack, modifier = ModifierHelper.topStart(scale))
            Column(
                Modifier.align(Alignment.CenterEnd).padding(end = Adaptive.safeContentPadding(scale)),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(Icons.Filled.Tune, Icons.Filled.StarBorder, Icons.Filled.Edit, Icons.Filled.PhotoLibrary, Icons.Filled.MoreVert).forEach {
                    FreebIconButton(it, Adaptive.adaptiveIcon(scale, true), {})
                }
            }
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = Adaptive.scaled(24.dp, scale)),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FreebIconButton(Icons.Filled.SaveAlt, Adaptive.adaptiveIcon(scale), {}, false)
                Box(
                    Modifier.size(68.dp).clip(CircleShape).background(Color.White).clickable { },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Send, null, tint = Color.Black, modifier = Modifier.size(28.dp)) }
                FreebIconButton(Icons.Filled.Share, Adaptive.adaptiveIcon(scale), {})
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    ResponsivePage {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Adaptive.safeContentPadding(it), vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FreebIconButton(Icons.Filled.ArrowBack, Adaptive.adaptiveIcon(it, true), onBack)
            FreebIconButton(Icons.Filled.MoreVert, Adaptive.adaptiveIcon(it, true), {})
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = Adaptive.safeContentPadding(it)),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listOf(
                Icons.Filled.PersonOutline,
                Icons.Filled.NotificationsNone,
                Icons.Filled.Lock,
                Icons.Filled.HeadsetMic,
                Icons.Filled.Tune,
                Icons.Filled.Explore,
                Icons.Filled.Menu,
                Icons.Filled.MoreVert
            )) { icon ->
                SettingsRow(icon)
            }
        }
    }
}

@Composable
private fun ResponsivePage(content: @Composable BoxWithConstraintsScope.(Float) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)
        content(scale)
    }
}

@Composable
private fun HomeScaffold(content: @Composable BoxWithConstraintsScope.(Float) -> Unit) {
    ResponsivePage(content)
}

private object ModifierHelper {
    fun topStart(scale: Float): Modifier =
        Modifier.padding(start = Adaptive.safeContentPadding(scale), top = Adaptive.safeContentPadding(scale))
}

@Composable
private fun PageTopBar(
    left: ImageVector,
    right: List<ImageVector>,
    onLeft: () -> Unit,
    onRight: (Int) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Adaptive.safeContentPadding(scale), vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FreebIconButton(left, Adaptive.adaptiveIcon(scale, true), onLeft)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                right.forEachIndexed { index, icon ->
                    FreebIconButton(icon, Adaptive.adaptiveIcon(scale, true), { onRight(index) })
                }
            }
        }
    }
}

@Composable
private fun ActionTile(icon: ImageVector) {
    Box(
        Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun ChatRow(index: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(if (index % 3 == 0) Panel else Color.Transparent)
            .clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(56.dp, index)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Bar(120.dp, 13.dp, if (index % 3 == 0) 1f else .75f)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Bar(170.dp, 9.dp, .42f)
                Dot(7.dp, if (index % 2 == 0) Accent else Color.White.copy(.35f))
            }
        }
        Icon(Icons.Filled.MoreVert, null, tint = Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun StoryBubble(index: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(if (index % 2 == 0) Accent else Color.White)
                .padding(3.dp).clip(CircleShape).clickable(onClick = onClick).background(Color.Black, CircleShape)
                .padding(3.dp)
        ) { Avatar(58.dp, index) }
        Bar(42.dp, 7.dp, .65f, Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun StoryCard(index: Int, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1.7f).clip(RoundedCornerShape(26.dp)).clickable(onClick = onClick)
            .background(Brush.linearGradient(listOf(Color(0xFF141414), Color(0xFF3A3A3A), Color(0xFF0A0A0A))))
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Avatar(36.dp, index + 5); Dot(6.dp, Accent) }
            FreebIconButton(Icons.Filled.MoreVert, 36.dp, {})
        }
        Box(
            Modifier.align(Alignment.Center).size(46.dp).background(Color.White.copy(.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(25.dp)) }
    }
}

@Composable
private fun ChipVisual(index: Int) {
    Box(
        Modifier.size(width = 68.dp, height = 34.dp).clip(CircleShape)
            .background(if (index == 0) Color.White else Panel)
            .border(1.dp, Line, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(16.dp).background(if (index == 0) Color.Black else Color.White.copy(.42f), CircleShape))
    }
}

@Composable
private fun DiscoveryCard(index: Int, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(if (index % 3 == 0) .92f else 1.35f)
            .clip(RoundedCornerShape(28.dp)).clickable(onClick = onClick)
            .background(Brush.linearGradient(listOf(Color(0xFF222222), Color(0xFF080808), Color(0xFF303030))))
    ) {
        Row(
            Modifier.align(Alignment.BottomStart).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(40.dp, index + 10)
            Bar(130.dp, 11.dp, .56f)
        }
        Box(Modifier.align(Alignment.Center).size(56.dp).background(Color.White.copy(.14f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        FreebIconButton(Icons.Filled.Share, 40.dp, {}, modifier = ModifierHelper.topStart(1f))
    }
}

@Composable
private fun Avatar(size: androidx.compose.ui.unit.Dp, seed: Int, modifier: Modifier = Modifier) {
    val colors = listOf(
        listOf(Color(0xFF2E2E2E), Color(0xFF7D7D7D)),
        listOf(Color(0xFF111111), Color(0xFF4C4C4C)),
        listOf(Color(0xFF4A4A4A), Color(0xFF151515)),
        listOf(Color(0xFF767676), Color(0xFF252525))
    )[seed % 4]
    Box(
        modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(.16f), CircleShape)
    )
}

@Composable
private fun StatVisual() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(16.dp).background(Color.White, CircleShape))
        Bar(30.dp, 7.dp, .55f)
    }
}

@Composable
private fun PillVisual(icon: ImageVector) {
    Box(
        Modifier.size(width = 56.dp, height = 38.dp).clip(CircleShape)
            .background(Panel).border(1.dp, Line, CircleShape),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)) }
}

@Composable
private fun TabIcon(icon: ImageVector, selected: Boolean) {
    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White.copy(if (selected) 1f else .38f), modifier = Modifier.size(22.dp))
        if (selected) Box(Modifier.align(Alignment.BottomCenter).size(4.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun ProfileGrid() {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        repeat(4) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) { cell ->
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).background(
                            Brush.linearGradient(listOf(
                                Color(0xFF101010),
                                Color(0xFF262626),
                                Color(0xFF080808)
                            ))
                        )
                    )
                }
            }
            if (it != 3) Box(Modifier.size(1.dp))
        }
    }
}

@Composable
private fun MessageBubble(index: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (index % 3 == 0) Arrangement.End else Arrangement.Start
    ) {
        Box(
            Modifier.size(width = if (index % 2 == 0) 172.dp else 122.dp, height = if (index % 3 == 0) 58.dp else 46.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(if (index % 3 == 0) Color.White else Panel)
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Bar(90.dp, 9.dp, if (index % 2 == 0) .75f else .5f, color = if (index % 3 == 0) Color.Black.copy(.25f) else Color.White.copy(.45f))
        }
    }
}

@Composable
private fun ComposerBar(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(CircleShape).background(Color(0xE60B0B0B)).border(1.dp, Line, CircleShape)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FreebIconButton(Icons.Filled.Add, 40.dp, {})
        FreebIconButton(Icons.Filled.CameraAlt, 40.dp, {})
        Box(Modifier.weight(1f).size(38.dp))
        FreebIconButton(Icons.Filled.HeadsetMic, 40.dp, {})
        FreebIconButton(Icons.Filled.Send, 40.dp, {}, filled = true)
    }
}

@Composable
private fun SettingsRow(icon: ImageVector) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel)
            .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FreebIconButton(icon, 42.dp, {})
        Box(Modifier.weight(1f).padding(horizontal = 12.dp))
        FreebIconButton(Icons.Filled.MoreVert, 36.dp, {})
    }
}

@Composable
private fun Bar(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier,
    color: Color = Muted
) {
    Box(modifier.size(width = width, height = height).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha)))
}

@Composable
private fun Dot(size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(Modifier.size(size).background(color, CircleShape))
}
