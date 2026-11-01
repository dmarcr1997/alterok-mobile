package com.alterok.mobile

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.roundToInt

private val bpBg = Color(0xFF0B1924)
private val bpPanel = Color(0xFF1A3040)
private val bpBorder = Color(0xFF536879)
private val bpPaper = Color(0xFFDCCDA8)
private val bpCyan = Color(0xFF91C8CB)
private val bpAmber = Color(0xFFD5AB68)
private val bpFaint = Color(0xFF94AAB3)

private data class BlueprintObject(val kind: String, val points: List<Offset>, val label: String = "")
private val toolNames = listOf("SKETCH", "LINE", "RECT", "CIRCLE", "DIM", "LABEL")

@Composable
fun BlueprintScreen(onNotice: (String) -> Unit, onKrita: () -> Unit, onFiles: () -> Unit, onBlender: () -> Unit, onCad: () -> Unit) {
    val ctx = LocalContext.current
    val objects = remember { mutableStateListOf<BlueprintObject>() }
    var current by remember { mutableStateOf<BlueprintObject?>(null) }
    var tool by remember { mutableStateOf("LINE") }
    var snap by remember { mutableStateOf(true) }
    var grid by remember { mutableStateOf(true) }
    var project by remember { mutableStateOf("UNTITLED") }
    var annotation by remember { mutableStateOf("NOTE 01") }
    var drawingNumber by remember { mutableStateOf("AK-001") }
    var units by remember { mutableStateOf("mm") }
    var mmPerGrid by remember { mutableStateOf("10") }
    var size by remember { mutableStateOf(IntSize(900, 540)) }
    var revision by remember { mutableStateOf("A") }
    var status by remember { mutableStateOf("NEW DRAWING") }
    val gridPx = 24f
    fun snapped(p: Offset): Offset = if (snap && tool != "SKETCH") Offset((p.x / gridPx).roundToInt() * gridPx, (p.y / gridPx).roundToInt() * gridPx) else p
    fun save() {
        try {
            val root = JSONObject().put("format", "alterok-blueprint-v1").put("project", project).put("number", drawingNumber)
                .put("revision", revision).put("units", units).put("mmPerGrid", mmPerGrid)
                .put("width", size.width).put("height", size.height)
            val arr = JSONArray()
            objects.forEach { o ->
                val pts = JSONArray()
                o.points.forEach { pts.put(JSONArray().put(it.x.toDouble()).put(it.y.toDouble())) }
                arr.put(JSONObject().put("kind", o.kind).put("label", o.label).put("points", pts))
            }
            root.put("objects", arr)
            File(ctx.filesDir, "blueprint-current.json").writeText(root.toString(2))
            status = "PROJECT SAVED"; onNotice(status)
        } catch (e: Exception) { onNotice("SAVE ERROR / ${e.message}") }
    }
    fun load() {
        try {
            val root = JSONObject(File(ctx.filesDir, "blueprint-current.json").readText())
            val arr = root.getJSONArray("objects")
            val restored = mutableListOf<BlueprintObject>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i); val a = o.getJSONArray("points")
                val pts = (0 until a.length()).map { k -> val p = a.getJSONArray(k); Offset(p.getDouble(0).toFloat(), p.getDouble(1).toFloat()) }
                restored.add(BlueprintObject(o.getString("kind"), pts, o.optString("label")))
            }
            objects.clear(); objects.addAll(restored)
            project = root.optString("project", "UNTITLED"); drawingNumber = root.optString("number", "AK-001")
            revision = root.optString("revision", "A"); units = root.optString("units", "mm")
            mmPerGrid = root.optString("mmPerGrid", "10")
            status = "PROJECT LOADED"; onNotice(status)
        } catch (e: Exception) { onNotice("LOAD ERROR / ${e.message}") }
    }
    fun export(png: Boolean) {
        try {
            val w = size.width.coerceAtLeast(1); val h = size.height.coerceAtLeast(1)
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val name = "ALTEROK_${stamp}.${if (png) "png" else "svg"}"
            val mime = if (png) "image/png" else "image/svg+xml"
            val collection = if (png) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val folder = if (png) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_DOWNLOADS
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name); put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$folder/ALTEROK")
            }
            val uri = ctx.contentResolver.insert(collection, values) ?: error("Unable to create export")
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    if (png) {
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val c = android.graphics.Canvas(bmp)
                        renderBlueprint(c, w.toFloat(), h.toFloat(), objects.toList(), true, project, drawingNumber, revision, units, mmPerGrid)
                        check(bmp.compress(Bitmap.CompressFormat.PNG, 100, out)); bmp.recycle()
                    } else out.write(makeSvg(w, h, objects.toList(), project, drawingNumber, revision, units, mmPerGrid).toByteArray(Charsets.UTF_8))
                } ?: error("Unable to write export")
            } catch (e: Exception) { ctx.contentResolver.delete(uri, null, null); throw e }
            status = "EXPORTED / $name"; onNotice(status)
        } catch (e: Exception) { onNotice("EXPORT ERROR / ${e.message}") }
    }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Column(Modifier.width(166.dp).fillMaxHeight().border(1.dp, bpBorder).background(bpPanel).verticalScroll(rememberScrollState()).padding(6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("01 / DRAFT TOOLS", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            toolNames.forEach { name -> BlueprintButton(name, tool == name) { tool = name } }
            HorizontalDivider(color = bpBorder)
            BlueprintButton(if (snap) "[X] SNAP GRID" else "[ ] SNAP GRID", snap) { snap = !snap }
            BlueprintButton(if (grid) "[X] SHOW GRID" else "[ ] SHOW GRID", grid) { grid = !grid }
            BlueprintButton("UNDO") { if (objects.isNotEmpty()) objects.removeAt(objects.lastIndex) }
            BlueprintButton("CLEAR") { objects.clear(); current = null }
            HorizontalDivider(color = bpBorder)
            Text("02 / PROJECT", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            BlueprintButton("SAVE EDITABLE") { save() }
            BlueprintButton("OPEN SAVED") { load() }
            BlueprintButton("EXPORT PNG") { export(true) }
            BlueprintButton("EXPORT SVG") { export(false) }
            HorizontalDivider(color = bpBorder)
            Text("EXTERNAL SUITE", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            BlueprintButton("KRITA", onClick = onKrita)
            BlueprintButton("BLENDER", onClick = onBlender)
            BlueprintButton("MATERIALIZR", onClick = onCad)
            BlueprintButton("FILES", onClick = onFiles)
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth().background(bpPanel).border(1.dp, bpBorder).padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BLUEPRINT EDITOR / $tool", color = bpCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("${objects.size} OBJECTS  |  $status", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1)
            }
            Canvas(Modifier.fillMaxWidth().weight(1f).background(bpBg).border(1.dp, bpBorder).onSizeChanged { size = it }
                .pointerInput(tool, snap, annotation) {
                    var start = Offset.Zero
                    detectDragGestures(onDragStart = { p ->
                        start = snapped(p)
                        current = BlueprintObject(tool, listOf(start), if (tool == "LABEL") annotation else "")
                    }, onDrag = { change, _ ->
                        change.consume()
                        val p = snapped(change.position)
                        current = if (tool == "SKETCH") current?.copy(points = (current?.points ?: listOf(start)) + p)
                        else BlueprintObject(tool, listOf(start, p), if (tool == "LABEL") annotation else "")
                    }, onDragEnd = {
                        current?.let { objects.add(it) }; current = null
                    }, onDragCancel = { current = null })
                }) {
                drawIntoCanvas { c -> renderBlueprint(c.nativeCanvas, size.width, size.height, objects.toList() + listOfNotNull(current), grid, project, drawingNumber, revision, units, mmPerGrid) }
            }
            Row(Modifier.fillMaxWidth().background(bpPanel).padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("STYLUS / DRAG TO PLACE GEOMETRY", color = bpCyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Text("GRID: $mmPerGrid $units  /  24 PX", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
        }
        Column(Modifier.width(183.dp).fillMaxHeight().border(1.dp, bpBorder).background(bpPanel).verticalScroll(rememberScrollState()).padding(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("03 / TITLE BLOCK", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            BlueprintField("PROJECT", project) { project = it }
            BlueprintField("DRAWING NO.", drawingNumber) { drawingNumber = it }
            BlueprintField("REVISION", revision) { revision = it }
            BlueprintField("UNITS", units) { units = it }
            BlueprintField("UNITS PER GRID", mmPerGrid) { mmPerGrid = it }
            HorizontalDivider(color = bpBorder)
            Text("04 / ANNOTATION", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            BlueprintField("LABEL TEXT", annotation) { annotation = it }
            Text("Select LABEL, then drag on canvas to place. DIM creates a dimension leader with a measured value based on the grid setting.", color = bpFaint, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
            HorizontalDivider(color = bpBorder)
            Text("DRAFTING NOTES", color = bpAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text("Concept scale only. Dimensions assume the configured grid scale; not verified for fabrication. Save stores the editable drawing locally. SVG is vector output.", color = bpFaint, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable private fun BlueprintButton(text: String, active: Boolean = false, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(31.dp).border(1.dp, if (active) bpPaper else bpBorder).background(if (active) bpPaper else bpBg).clickable(onClick = onClick).padding(horizontal = 7.dp), contentAlignment = Alignment.CenterStart) {
        Text(text, color = if (active) bpBg else bpCyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1)
    }
}
@Composable private fun BlueprintField(title: String, value: String, change: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = bpFaint, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
        OutlinedTextField(value = value, onValueChange = change, singleLine = true, modifier = Modifier.fillMaxWidth().height(51.dp), textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = bpPaper), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = bpCyan, unfocusedBorderColor = bpBorder))
    }
}
private fun renderBlueprint(c: android.graphics.Canvas, w: Float, h: Float, objects: List<BlueprintObject>, grid: Boolean, project: String, number: String, rev: String, units: String, perGrid: String) {
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2f; style = Paint.Style.STROKE; color = android.graphics.Color.rgb(145,200,203) }
    c.drawColor(android.graphics.Color.rgb(11,25,36))
    if (grid) {
        p.color = android.graphics.Color.rgb(33,57,74); p.strokeWidth = 1f
        var x = 0f; while (x < w) { c.drawLine(x,0f,x,h,p); x += 24f }
        var y = 0f; while (y < h) { c.drawLine(0f,y,w,y,p); y += 24f }
    }
    p.color = android.graphics.Color.rgb(145,200,203); p.strokeWidth = 2f
    objects.forEach { o ->
        if (o.points.isEmpty()) return@forEach
        val a = o.points.first(); val b = o.points.last()
        p.style = Paint.Style.STROKE; p.color = android.graphics.Color.rgb(145,200,203); p.strokeWidth = 2f
        when (o.kind) {
            "SKETCH" -> if (o.points.size > 1) { val path = AndroidPath().apply { moveTo(a.x,a.y); o.points.drop(1).forEach { lineTo(it.x,it.y) } }; c.drawPath(path,p) }
            "LINE" -> c.drawLine(a.x,a.y,b.x,b.y,p)
            "RECT" -> c.drawRect(minOf(a.x,b.x),minOf(a.y,b.y),maxOf(a.x,b.x),maxOf(a.y,b.y),p)
            "CIRCLE" -> c.drawCircle(a.x,a.y,hypot(b.x-a.x,b.y-a.y),p)
            "DIM" -> {
                p.color = android.graphics.Color.rgb(213,171,104)
                c.drawLine(a.x,a.y,b.x,b.y,p)
                c.drawCircle(a.x,a.y,3f,p); c.drawCircle(b.x,b.y,3f,p)
                val scale = perGrid.toFloatOrNull() ?: 10f
                val length = hypot(b.x-a.x,b.y-a.y) / 24f * scale
                p.style = Paint.Style.FILL; p.textSize = 15f
                c.drawText("%.1f %s".format(Locale.US,length,units), (a.x+b.x)/2f+6f, (a.y+b.y)/2f-8f,p)
            }
            "LABEL" -> { p.style = Paint.Style.FILL; p.textSize = 18f; c.drawText(o.label,a.x,a.y,p) }
        }
    }
    // The title block is visual metadata, not included in object coordinates.
    val tw = minOf(w * .55f, 325f); val th = 61f
    p.style = Paint.Style.FILL; p.color = android.graphics.Color.rgb(26,48,64)
    c.drawRect(w-tw-4f,h-th-4f,w-4f,h-4f,p)
    p.style = Paint.Style.STROKE; p.color = android.graphics.Color.rgb(213,171,104); p.strokeWidth = 1f
    c.drawRect(w-tw-4f,h-th-4f,w-4f,h-4f,p)
    p.style = Paint.Style.FILL; p.textSize = 12f
    c.drawText("ALTEROK / ${project.take(25)}",w-tw+4f,h-th+13f,p)
    p.color = android.graphics.Color.rgb(145,200,203); p.textSize = 11f
    c.drawText("DWG ${number.take(16)}  |  REV ${rev.take(6)}",w-tw+4f,h-th+32f,p)
    c.drawText("CONCEPT / GRID $perGrid $units  /  NTS",w-tw+4f,h-th+49f,p)
}
private fun makeSvg(w: Int, h: Int, objects: List<BlueprintObject>, project: String, number: String, rev: String, units: String, perGrid: String): String {
    fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    val s = StringBuilder("""<svg xmlns="http://www.w3.org/2000/svg" width="$w" height="$h" viewBox="0 0 $w $h"><rect width="100%" height="100%" fill="#0b1924"/>""")
    objects.forEach { o ->
        if (o.points.isEmpty()) return@forEach
        val a=o.points.first(); val b=o.points.last()
        val attrs="stroke=\"#91c8cb\" stroke-width=\"2\" fill=\"none\""
        when(o.kind) {
            "SKETCH" -> s.append("<polyline points=\"${o.points.joinToString(" ") { "${it.x},${it.y}" }}\" $attrs/>")
            "LINE" -> s.append("<line x1=\"${a.x}\" y1=\"${a.y}\" x2=\"${b.x}\" y2=\"${b.y}\" $attrs/>")
            "RECT" -> s.append("<rect x=\"${minOf(a.x,b.x)}\" y=\"${minOf(a.y,b.y)}\" width=\"${kotlin.math.abs(b.x-a.x)}\" height=\"${kotlin.math.abs(b.y-a.y)}\" $attrs/>")
            "CIRCLE" -> s.append("<circle cx=\"${a.x}\" cy=\"${a.y}\" r=\"${hypot(b.x-a.x,b.y-a.y)}\" $attrs/>")
            "DIM" -> {
                val length=hypot(b.x-a.x,b.y-a.y)/24f*(perGrid.toFloatOrNull()?:10f)
                s.append("<line x1=\"${a.x}\" y1=\"${a.y}\" x2=\"${b.x}\" y2=\"${b.y}\" stroke=\"#d5ab68\"/>")
                s.append("<text x=\"${(a.x+b.x)/2+6}\" y=\"${(a.y+b.y)/2-8}\" fill=\"#d5ab68\" font-size=\"15\">${"%.1f".format(Locale.US,length)} ${esc(units)}</text>")
            }
            "LABEL" -> s.append("<text x=\"${a.x}\" y=\"${a.y}\" fill=\"#91c8cb\" font-size=\"18\">${esc(o.label)}</text>")
        }
    }
    s.append("<g font-family=\"monospace\" font-size=\"12\" fill=\"#d5ab68\"><text x=\"12\" y=\"22\">${esc(project)} / ${esc(number)} / REV ${esc(rev)}</text></g></svg>")
    return s.toString()
}
