package com.alterok.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF0B1925)
private val Navy = Color(0xFF122638)
private val Panel = Color(0xFF192F40)
private val Line = Color(0xFF4A6470)
private val Paper = Color(0xFFD9CBA9)
private val PaperDark = Color(0xFFBBAA83)
private val Cyan = Color(0xFF8FC6C9)
private val Amber = Color(0xFFDDB16C)
private val Muted = Color(0xFF9BB0B9)
private val Red = Color(0xFFD87968)
private val Mono = FontFamily.Monospace

class MainActivity : ComponentActivity() {
    private fun launchApp(packageId: String, fallback: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageId)
        if (intent != null) startActivity(intent)
        else openUrl(fallback)
    }
    private fun openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    private fun openFiles() = startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(217, 203, 169)
        window.navigationBarColor = android.graphics.Color.rgb(11, 25, 37)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContent {
            var tab by remember { mutableStateOf("HOME") }
            MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Cyan)) {
                Column(Modifier.fillMaxSize().background(Ink)) {
                    Header()
                    Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Navigation(tab, { tab = it }, Modifier.width(126.dp).fillMaxHeight())
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Label("${listOf("HOME", "DESIGN", "CONTROL", "SYSTEM").indexOf(tab) + 1} / $tab", Amber, 13)
                                Spacer(Modifier.weight(1f))
                                Label("ALTEROK / WORKSTATION     REV 0.2", Muted, 10)
                            }
                            Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                                when (tab) {
                                    "HOME" -> HomeScreen({ tab = it }, ::openFiles, { launchApp("org.krita", "https://play.google.com/store/apps/details?id=org.krita") })
                                    "DESIGN" -> DesignScreen(::openFiles, { launchApp("org.krita", "https://play.google.com/store/apps/details?id=org.krita") }, { openUrl("https://blenderandroid.github.io/") }, { openUrl("https://materializr.com/") })
                                    "CONTROL" -> ControlScreen()
                                    else -> SystemScreen(::openFiles, { startActivity(Intent(Settings.ACTION_SETTINGS)) })
                                }
                            }
                            Row(Modifier.fillMaxWidth().background(Navy).border(1.dp, Line).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Label("●  LOCAL TERMINAL", Cyan, 10)
                                Spacer(Modifier.weight(1f))
                                Label("EXTERNAL LINKS: NOT CONFIGURED", Amber, 10)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun Label(text: String, color: Color = Paper, size: Int = 12, bold: Boolean = false) {
    Text(text, color = color, fontFamily = Mono, fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        lineHeight = (size + 4).sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
}

@Composable private fun Header() {
    Row(Modifier.fillMaxWidth().height(42.dp).background(Paper).border(1.dp, PaperDark)
        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Label("◢ ALTEROK / MOBILE", Ink, 17, true)
        Spacer(Modifier.width(16.dp))
        Label("///", Navy, 16, true)
        Spacer(Modifier.weight(1f))
        Label("PAPER CIRCUIT INDUSTRIAL  •  v0.2", Navy, 11, true)
        Spacer(Modifier.width(12.dp))
        Label("□  ▣  ×", Ink, 14, true)
    }
}

@Composable private fun Navigation(active: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.background(Navy).border(1.dp, Line).padding(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Label("SYS / 00", Amber, 11, true)
        Spacer(Modifier.height(3.dp))
        listOf("HOME", "DESIGN", "CONTROL", "SYSTEM").forEachIndexed { index, name ->
            val selected = active == name
            Box(Modifier.fillMaxWidth().border(1.dp, if (selected) PaperDark else Line)
                .background(if (selected) Paper else Ink).clickable { onSelect(name) }.padding(10.dp)) {
                Label("0${index + 1} $name", if (selected) Ink else Cyan, 11, selected)
            }
        }
        Spacer(Modifier.weight(1f))
        Label("LOCAL", Cyan, 10)
        Label("NO ROBOT LINK", Amber, 9)
        Label("REV 02", Muted, 9)
    }
}

@Composable private fun Frame(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.border(1.dp, Line).background(Panel)) {
        Row(Modifier.fillMaxWidth().background(Navy).border(1.dp, Line).padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Label("▸ $title", Cyan, 11, true)
            Spacer(Modifier.weight(1f))
            Label("□ ×", Muted, 10)
        }
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
    }
}

@Composable private fun ConsoleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    Box(modifier.border(1.dp, if (primary) PaperDark else Line)
        .background(if (primary) Paper else Navy).clickable(onClick = onClick)
        .padding(horizontal = 10.dp, vertical = 9.dp), contentAlignment = Alignment.CenterStart) {
        Label(text, if (primary) Ink else Paper, 11, primary)
    }
}

@Composable private fun Stat(label: String, value: String, valueColor: Color = Cyan) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Label(label, Muted, 11)
        Label(value, valueColor, 11, true)
    }
}

@Composable private fun Blueprint(modifier: Modifier = Modifier, caption: String = "PROJECT VIEW / NO MODEL LOADED") {
    Box(modifier.background(Ink).border(1.dp, Line)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 25.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Line.copy(alpha = .22f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Line.copy(alpha = .22f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
            val cx = size.width * .5f
            val cy = size.height * .46f
            drawLine(Cyan.copy(alpha = .5f), Offset(cx - 45f, cy), Offset(cx + 45f, cy), 1.5f)
            drawLine(Cyan.copy(alpha = .5f), Offset(cx, cy - 45f), Offset(cx, cy + 45f), 1.5f)
            drawCircle(Amber.copy(alpha = .7f), 20.dp.toPx(), Offset(cx, cy), style = Stroke(1.5f))
        }
        Box(Modifier.align(Alignment.BottomStart).background(Navy).padding(6.dp)) {
            Label(caption, Amber, 10)
        }
    }
}

@Composable private fun HomeScreen(go: (String) -> Unit, files: () -> Unit, krita: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Frame("SYSTEM OVERVIEW", Modifier.weight(1f)) {
                Stat("ANDROID OS", "RUNNING")
                Stat("NETWORK", "SEE ANDROID SETTINGS", Amber)
                Stat("ROBOT LINK", "NOT CONFIGURED", Amber)
                Stat("PROJECT", "NOT SELECTED", Amber)
            }
            Frame("QUICK LAUNCH", Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConsoleButton("✎  KRITA", krita, Modifier.weight(1f))
                    ConsoleButton("▤  FILES", files, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConsoleButton("DESIGN BAY", { go("DESIGN") }, Modifier.weight(1f), true)
                    ConsoleButton("CONTROL", { go("CONTROL") }, Modifier.weight(1f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Frame("ACTIVE PROJECT", Modifier.weight(1.25f)) {
                Blueprint(Modifier.fillMaxWidth().height(170.dp))
                Label("Open a project file to begin. No project data is loaded.", Muted, 11)
                ConsoleButton("OPEN PROJECT FILE", files)
            }
            Frame("SYSTEM NOTES", Modifier.weight(1f)) {
                Label("ALTEROK / ENGINEERING TERMINAL", Paper, 13, true)
                Label("Design tools are available locally. Robotics integrations are reserved for future releases.", Muted, 11)
                Spacer(Modifier.height(6.dp))
                Label("01  /  DESIGN", Amber, 11)
                Label("02  /  CONTROL", Amber, 11)
                Label("03  /  SYSTEM", Amber, 11)
            }
        }
    }
}

@Composable private fun DesignScreen(files: () -> Unit, krita: () -> Unit, blender: () -> Unit, cad: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Frame("TOOL BAY", Modifier.width(185.dp)) {
            Label("DRAW / MODEL / EXPORT", Amber, 10)
            ConsoleButton("✎  KRITA", krita, Modifier.fillMaxWidth(), true)
            ConsoleButton("◇  BLENDER BUILDS", blender, Modifier.fillMaxWidth())
            ConsoleButton("▣  MATERIALIZR WEB", cad, Modifier.fillMaxWidth())
            ConsoleButton("▤  PROJECT FILES", files, Modifier.fillMaxWidth())
            Label("External tools launch separately. No embedded CAD engine is included.", Muted, 10)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Frame("DESIGN VIEWPORT / PREVIEW", Modifier.fillMaxWidth()) {
                Blueprint(Modifier.fillMaxWidth().height(245.dp), "NO MODEL LOADED  /  GRID REFERENCE")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConsoleButton("OPEN FILE", files)
                    Label("VIEWPORT IS A PLACEHOLDER", Amber, 10)
                }
            }
            Frame("WORKSPACE", Modifier.fillMaxWidth()) {
                Stat("ACTIVE FILE", "NONE")
                Stat("DRAWING ENGINE", "EXTERNAL APP")
                Stat("EXPORT", "VIA SELECTED APP")
            }
        }
        Frame("INSPECTOR", Modifier.width(170.dp)) {
            Label("PROJECT", Amber, 10)
            Stat("NAME", "UNSET")
            Stat("FORMAT", "UNSET")
            Stat("LAYERS", "N/A")
            Spacer(Modifier.height(12.dp))
            Label("SHORTCUTS", Amber, 10)
            Label("KRITA / SKETCH", Muted, 10)
            Label("BLENDER / 3D", Muted, 10)
            Label("MATERIALIZR / CAD", Muted, 10)
        }
    }
}

@Composable private fun ControlScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Frame("ROBOT CONNECTIONS", Modifier.weight(1f)) {
                listOf("SO-101 ARM", "TARS / 95", "HIVE CLUSTER", "STINGRAY VTOL").forEach {
                    Stat(it, "NOT CONFIGURED", Amber)
                }
            }
            Frame("DEVICE VIEW", Modifier.weight(1.5f)) {
                Blueprint(Modifier.fillMaxWidth().height(160.dp), "NO VIDEO STREAM / DEVICE NOT CONNECTED")
            }
        }
        Frame("CONTROL SAFETY / STATUS", Modifier.fillMaxWidth()) {
            Label("NO CONTROL CHANNEL ESTABLISHED", Amber, 13, true)
            Label("Telemetry, motion commands, camera streams, and emergency-stop controls are intentionally disabled until a verified device integration is implemented.", Muted, 11)
            Box(Modifier.background(Red.copy(alpha = .15f)).border(1.dp, Red).padding(8.dp)) {
                Label("CONTROL LOCKED", Red, 12, true)
            }
        }
    }
}

@Composable private fun SystemScreen(files: () -> Unit, settings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Frame("ANDROID UTILITIES", Modifier.weight(1f)) {
            ConsoleButton("OPEN ANDROID SETTINGS", settings, Modifier.fillMaxWidth(), true)
            ConsoleButton("OPEN PROJECT FILE", files, Modifier.fillMaxWidth())
            Label("The tablet remains a standard Android device. Play Store and other installed apps are unaffected.", Muted, 11)
        }
        Frame("TERMINAL INFO", Modifier.weight(1f)) {
            Stat("APPLICATION", "ALTEROK MOBILE")
            Stat("INTERFACE", "PAPER CIRCUIT")
            Stat("VERSION", "0.2.0")
            Stat("HARDWARE LINK", "NOT CONFIGURED", Amber)
            Stat("DEVICE METRICS", "NOT COLLECTED", Amber)
        }
    }
}
