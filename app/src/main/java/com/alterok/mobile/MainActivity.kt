package com.alterok.mobile

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF0B1924)
private val Panel = Color(0xFF1A3040)
private val Border = Color(0xFF536879)
private val Paper = Color(0xFFDCCDA8)
private val Cyan = Color(0xFF91C8CB)
private val Amber = Color(0xFFD5AB68)
private val Red = Color(0xFFDA7C70)
private val Faint = Color(0xFF94AAB3)
private val Grid = Color(0xFF203849)

private data class StrokeData(val points: List<Offset>, val color: Color, val width: Float)

class MainActivity : ComponentActivity() {
    private fun openApp(pkg: String, fallback: String) {
        val app = packageManager.getLaunchIntentForPackage(pkg)
        startActivity(app ?: Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
    }
    private fun openFiles() {
        startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11,25,36)
        window.navigationBarColor = android.graphics.Color.rgb(11,25,36)
        setContent {
            var screen by remember { mutableStateOf("DESIGN") }
            var notice by remember { mutableStateOf("READY / LOCAL WORKSTATION") }
            val context = LocalContext.current
            MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Cyan)) {
                Column(Modifier.fillMaxSize().background(Ink).windowInsetsPadding(WindowInsets.safeDrawing)) {
                    Row(Modifier.fillMaxWidth().background(Paper).height(42.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("◢ ALTEROK / MOBILE  ///", color = Ink, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text("PAPER CIRCUIT INDUSTRIAL  •  REV 03", color = Ink, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.width(142.dp).fillMaxHeight().border(1.dp, Border).background(Panel).padding(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(" SYS / 00   ▰▰▰", color = Amber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            listOf("HOME", "DESIGN", "CONTROL", "SYSTEM").forEachIndexed { i, item ->
                                ConsoleButton("0${i+1} $item", screen == item, Modifier.fillMaxWidth()) { screen = item }
                            }
                            Spacer(Modifier.weight(1f))
                            Text("● LOCAL", color = Cyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("○ ROBOT LINK OFF", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("REVISION 0.3", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("0${listOf("HOME","DESIGN","CONTROL","SYSTEM").indexOf(screen)+1} / $screen", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                                Text("ALTEROK / ENGINEERING TERMINAL", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                            Box(Modifier.weight(1f)) {
                                when(screen) {
                                    "DESIGN" -> DesignScreen(onNotice = { notice = it }, onKrita = { openApp("org.krita", "https://play.google.com/store/apps/details?id=org.krita") }, onFiles = { openFiles() }, onBlender = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://blenderandroid.github.io/"))) }, onCad = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://materializr.com/"))) })
                                    "HOME" -> HomeScreen(onDesign = { screen = "DESIGN" }, onFiles = { openFiles() }, onKrita = { openApp("org.krita", "https://play.google.com/store/apps/details?id=org.krita") })
                                    "CONTROL" -> ControlScreen()
                                    else -> SystemScreen(onSettings = { startActivity(Intent(Settings.ACTION_SETTINGS)) }, onFiles = { openFiles() })
                                }
                            }
                            Row(Modifier.fillMaxWidth().height(25.dp).border(1.dp, Border).background(Panel).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("● $notice", color = Cyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1)
                                Text("EXTERNAL LINKS / UNCONFIGURED", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ConsoleButton(label: String, active: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.height(36.dp).border(1.dp, if(active) Paper else Border).background(if(active) Paper else Ink).clickable(onClick = onClick).padding(horizontal = 9.dp), contentAlignment = Alignment.CenterStart) {
        Text(label, color = if(active) Ink else Cyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable private fun Instrument(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.border(1.dp, Border).background(Panel)) {
        Row(Modifier.fillMaxWidth().height(26.dp).background(Ink).border(BorderStroke(0.5.dp, Border)).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("▸ $title", color = Cyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("□ ×", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
    }
}

@Composable private fun DataLine(name: String, value: String, valueColor: Color = Cyan) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, color = Faint, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text(value, color = valueColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
    }
}

@Composable private fun DesignScreen(onNotice: (String) -> Unit, onKrita: () -> Unit, onFiles: () -> Unit, onBlender: () -> Unit, onCad: () -> Unit) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<StrokeData>() }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Cyan) }
    var penWidth by remember { mutableFloatStateOf(3f) }
    var showGrid by remember { mutableStateOf(true) }
    var canvasSize by remember { mutableStateOf(IntSize(1000, 600)) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Instrument("TOOL BAY", Modifier.width(182.dp).fillMaxHeight()) {
            Text("DRAW / MODEL / EXPORT", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            listOf("PEN / CYAN" to Cyan, "PEN / AMBER" to Amber, "PEN / PAPER" to Paper, "ERASER / INK" to Ink).forEach { (label, color) ->
                ConsoleButton(label, selectedColor == color, Modifier.fillMaxWidth()) { selectedColor = color }
            }
            Text("STROKE / ${penWidth.toInt()} PX", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Slider(value = penWidth, onValueChange = { penWidth = it }, valueRange = 1f..12f, modifier = Modifier.fillMaxWidth())
            ConsoleButton(if(showGrid) "☑ GRID" else "□ GRID", false, Modifier.fillMaxWidth()) { showGrid = !showGrid }
            ConsoleButton("↶ UNDO", false, Modifier.fillMaxWidth()) { if(strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }
            ConsoleButton("CLEAR CANVAS", false, Modifier.fillMaxWidth()) { strokes.clear(); current = emptyList() }
            Spacer(Modifier.height(4.dp))
            Text("EXTERNAL SUITE", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            ConsoleButton("KRITA", false, Modifier.fillMaxWidth(), onKrita)
            ConsoleButton("BLENDER BUILDS", false, Modifier.fillMaxWidth(), onBlender)
            ConsoleButton("MATERIALIZR WEB", false, Modifier.fillMaxWidth(), onCad)
            ConsoleButton("PROJECT FILES", false, Modifier.fillMaxWidth(), onFiles)
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Instrument("SKETCHPAD / LIVE DRAWING", Modifier.fillMaxWidth().weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ORTHO / XY   •   PEN INPUT", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Text("STROKES ${strokes.size}", color = Cyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
                Canvas(Modifier.fillMaxWidth().weight(1f).border(1.dp, Border).background(Ink).onSizeChanged { canvasSize = it }.pointerInput(selectedColor, penWidth) {
                    detectDragGestures(onDragStart = { start -> current = listOf(start) }, onDragEnd = {
                        if(current.isNotEmpty()) strokes.add(StrokeData(current, selectedColor, penWidth))
                        current = emptyList()
                    }, onDragCancel = { current = emptyList() }) { change, _ ->
                        change.consume()
                        current = current + change.position
                    }
                }) {
                    if(showGrid) {
                        val step = 28.dp.toPx()
                        var x = 0f
                        while(x < size.width) { drawLine(Grid, Offset(x,0f), Offset(x,size.height), 1f); x += step }
                        var y = 0f
                        while(y < size.height) { drawLine(Grid, Offset(0f,y), Offset(size.width,y), 1f); y += step }
                    }
                    (strokes.toList() + if(current.isNotEmpty()) listOf(StrokeData(current, selectedColor, penWidth)) else emptyList()).forEach { stroke ->
                        if(stroke.points.size > 1) {
                            val path = Path().apply { moveTo(stroke.points.first().x, stroke.points.first().y); stroke.points.drop(1).forEach { lineTo(it.x,it.y) } }
                            drawPath(path, stroke.color, style = Stroke(width = stroke.width, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        } else if(stroke.points.isNotEmpty()) drawCircle(stroke.color, stroke.width / 2f, stroke.points.first())
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ConsoleButton("SAVE PNG", true, Modifier.width(120.dp)) {
                        try {
                            val file = exportSketch(context, strokes.toList(), canvasSize)
                            onNotice("SAVED / $file")
                        } catch (e: Exception) { onNotice("EXPORT FAILED / ${e.message ?: "UNKNOWN"}") }
                    }
                    Text("LOCAL SKETCHPAD  /  PNG EXPORT", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
            }
            Instrument("WORKSPACE / STATUS", Modifier.fillMaxWidth()) {
                DataLine("DRAWING ENGINE", "LOCAL / ACTIVE")
                DataLine("MODEL VIEWER", "NOT IMPLEMENTED", Amber)
                DataLine("OUTPUT", "PICTURES / ALTEROK")
            }
        }
        Instrument("INSPECTOR", Modifier.width(172.dp).fillMaxHeight()) {
            Text("SKETCH PROPERTIES", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            DataLine("WIDTH", "${canvasSize.width} PX")
            DataLine("HEIGHT", "${canvasSize.height} PX")
            DataLine("STROKES", "${strokes.size}")
            DataLine("GRID", if(showGrid) "VISIBLE" else "HIDDEN")
            Spacer(Modifier.height(12.dp))
            Text("OPERATOR NOTES", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text("Draw directly with your pen or finger. Export a transparent PNG to Pictures/ALTEROK. CAD tools launch separately.", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 15.sp)
        }
    }
}

private fun exportSketch(context: android.content.Context, strokes: List<StrokeData>, size: IntSize): String {
    val w = size.width.coerceAtLeast(1)
    val h = size.height.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    strokes.forEach { s ->
        paint.color = android.graphics.Color.argb((s.color.alpha*255).toInt(), (s.color.red*255).toInt(), (s.color.green*255).toInt(), (s.color.blue*255).toInt())
        paint.strokeWidth = s.width
        if(s.points.size > 1) {
            val path = android.graphics.Path().apply { moveTo(s.points[0].x, s.points[0].y); s.points.drop(1).forEach { lineTo(it.x,it.y) } }
            canvas.drawPath(path, paint)
        } else if(s.points.size == 1) canvas.drawPoint(s.points[0].x, s.points[0].y, paint)
    }
    val filename = "ALTEROK_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ALTEROK")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("MediaStore insert failed")
    try { context.contentResolver.openOutputStream(uri)?.use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) } ?: error("Cannot open output") }
    catch(e: Exception) { context.contentResolver.delete(uri, null, null); throw e }
    finally { bitmap.recycle() }
    return filename
}

@Composable private fun HomeScreen(onDesign: () -> Unit, onFiles: () -> Unit, onKrita: () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Instrument("SYSTEM OVERVIEW", Modifier.fillMaxWidth()) {
                DataLine("ANDROID", "ACTIVE")
                DataLine("ENGINEERING TERMINAL", "READY")
                DataLine("ROBOT CONNECTION", "NOT CONFIGURED", Amber)
                DataLine("ACTIVE PROJECT", "NONE SELECTED", Amber)
            }
            Instrument("QUICK LAUNCH", Modifier.fillMaxWidth()) {
                ConsoleButton("OPEN DESIGN / SKETCHPAD", true, Modifier.fillMaxWidth(), onDesign)
                ConsoleButton("KRITA / EXTERNAL APP", false, Modifier.fillMaxWidth(), onKrita)
                ConsoleButton("PROJECT FILES", false, Modifier.fillMaxWidth(), onFiles)
            }
            Instrument("OPERATOR NOTES", Modifier.fillMaxWidth()) {
                Text("ALTEROK is a local Android engineering terminal. Sketchpad exports PNG files. Robot control and native CAD are not yet implemented.", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
        Instrument("BLUEPRINT / TERMINAL IDENT", Modifier.weight(1f).fillMaxHeight()) {
            BlueprintEmblem(Modifier.fillMaxWidth().weight(1f))
            Text("ALTEROK SYSTEMS   //   MOBILE STATION", color = Paper, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            Text("REV 03  /  LOCAL DESIGN & CONTROL", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
    }
}

@Composable private fun BlueprintEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Ink).border(1.dp, Border)) {
        val step = 26.dp.toPx()
        var x = 0f; while(x < size.width) { drawLine(Grid, Offset(x,0f), Offset(x,size.height)); x += step }
        var y = 0f; while(y < size.height) { drawLine(Grid, Offset(0f,y), Offset(size.width,y)); y += step }
        val cx = size.width / 2f; val cy = size.height / 2f
        val s = minOf(size.width, size.height) * 0.32f
        val shape = Path().apply { moveTo(cx,cy-s); lineTo(cx+s*0.95f,cy+s*0.8f); lineTo(cx+s*0.2f,cy+s*0.45f); lineTo(cx,cy-s*0.25f); lineTo(cx-s*0.2f,cy+s*0.45f); lineTo(cx-s*0.95f,cy+s*0.8f); close() }
        drawPath(shape, Amber, style = Stroke(width = 3f))
        drawLine(Cyan, Offset(cx-s*1.2f,cy), Offset(cx+s*1.2f,cy), 1f)
        drawLine(Cyan, Offset(cx,cy-s*1.2f), Offset(cx,cy+s*1.2f), 1f)
        drawCircle(Amber, s*1.18f, Offset(cx,cy), style = Stroke(width = 1f))
    }
}

@Composable private fun ControlScreen() {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Instrument("ROBOT CONNECTIONS", Modifier.fillMaxWidth()) {
                listOf("SO-101 ARM", "TARS / 95", "HIVE CLUSTER", "STINGRAY VTOL").forEach { DataLine(it, "NOT CONFIGURED", Amber) }
            }
            Instrument("CONTROL INTERLOCK", Modifier.fillMaxWidth()) {
                Text("■ CONTROL LOCKED", color = Red, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                Text("No verified transport or safety interlock is installed. Motion commands are disabled.", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
        Instrument("DEVICE / SCHEMATIC BAY", Modifier.weight(1.1f).fillMaxHeight()) {
            BlueprintEmblem(Modifier.fillMaxWidth().weight(1f))
            Text("NO LIVE TELEMETRY / NO VIDEO STREAM", color = Amber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

@Composable private fun SystemScreen(onSettings: () -> Unit, onFiles: () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Instrument("ANDROID UTILITIES", Modifier.weight(1f)) {
            ConsoleButton("OPEN ANDROID SETTINGS", true, Modifier.fillMaxWidth(), onSettings)
            ConsoleButton("OPEN PROJECT FILE", false, Modifier.fillMaxWidth(), onFiles)
            Text("Android, Play Store and installed apps remain unchanged.", color = Faint, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
        Instrument("TERMINAL INFO", Modifier.weight(1f)) {
            DataLine("APPLICATION", "ALTEROK MOBILE")
            DataLine("INTERFACE", "PAPER CIRCUIT")
            DataLine("VERSION", "0.3.0")
            DataLine("ROBOT LINK", "NOT CONFIGURED", Amber)
            DataLine("PERFORMANCE", "NOT MONITORED", Amber)
        }
    }
}
