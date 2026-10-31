package com.alterok.mobile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Paper = Color(0xFFD9CEAF)
private val Navy = Color(0xFF172A3B)
private val Panel = Color(0xFF101C27)
private val Cyan = Color(0xFF81C4C7)
private val Amber = Color(0xFFD3A456)
private val Muted = Color(0xFF9CAAB2)

class MainActivity : ComponentActivity() {
    private fun openApp(pkg: String, fallback: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) startActivity(intent)
        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
    }
    private fun openFiles() {
        startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var tab by remember { mutableStateOf("HOME") }
            MaterialTheme(colorScheme = darkColorScheme(background=Panel, surface=Panel, primary=Cyan)) {
                Column(Modifier.fillMaxSize().background(Panel)) {
                    Row(Modifier.fillMaxWidth().background(Paper).padding(horizontal=18.dp, vertical=12.dp),
                        horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                        Text("ALTEROK  /  MOBILE", color=Navy, fontWeight=FontWeight.Black, fontFamily=FontFamily.Monospace)
                        Text("PAPER CIRCUIT INDUSTRIAL   •   V0.1", color=Navy, fontFamily=FontFamily.Monospace)
                    }
                    Row(Modifier.fillMaxWidth().background(Navy).padding(8.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf("HOME","DESIGN","CONTROL","SYSTEM").forEach { name ->
                            Button(onClick={tab=name}, shape=RoundedCornerShape(0.dp),
                                colors=ButtonDefaults.buttonColors(containerColor=if(tab==name) Paper else Panel)) {
                                Text(name, color=if(tab==name) Navy else Cyan, fontFamily=FontFamily.Monospace)
                            }
                        }
                    }
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        Text("01  /  $tab", color=Amber, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold)
                        when(tab) {
                            "HOME" -> {
                                Text("ENGINEERING WORKSTATION", color=Paper, style=MaterialTheme.typography.headlineMedium)
                                Text("DESIGN • CONTROL • SYSTEM", color=Cyan, fontFamily=FontFamily.Monospace)
                                PanelCard("QUICK ACCESS", "Open the design suite or connect to your engineering tools.")
                                Button(onClick={tab="DESIGN"}) { Text("OPEN DESIGN BAY") }
                            }
                            "DESIGN" -> {
                                Text("DESIGN BAY", color=Paper, style=MaterialTheme.typography.headlineMedium)
                                PanelCard("KRITA", "Pen sketching • concept art • textures")
                                Action("LAUNCH KRITA") { openApp("org.krita","https://play.google.com/store/apps/details?id=org.krita") }
                                PanelCard("BLENDER ANDROID", "Experimental 3D game asset workflow")
                                Action("OPEN BLENDER BUILDS") { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://blenderandroid.github.io/"))) }
                                PanelCard("MATERIALIZR", "Mechanical parts • STEP / STL export")
                                Action("OPEN MATERIALIZR") { openApp("com.materializr.app","https://materializr.com/") }
                                Action("OPEN PROJECT FILE") { openFiles() }
                            }
                            "CONTROL" -> {
                                Text("CONTROL DECK", color=Paper, style=MaterialTheme.typography.headlineMedium)
                                PanelCard("LINK NOT CONFIGURED", "Hive / TARS / Stingray endpoints will be added after pairing.")
                                Text("NO ACTIVE HARDWARE CONNECTION", color=Amber, fontFamily=FontFamily.Monospace)
                            }
                            else -> {
                                Text("SYSTEM", color=Paper, style=MaterialTheme.typography.headlineMedium)
                                PanelCard("ANDROID INTACT", "Play Store and installed applications remain available.")
                                Action("OPEN ANDROID SETTINGS") { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
                                Action("OPEN PROJECT FILE") { openFiles() }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun PanelCard(title:String, detail:String) {
    Column(Modifier.fillMaxWidth().border(1.dp, Color(0xFF435563)).background(Color(0xFF192936)).padding(14.dp)) {
        Text(title, color=Cyan, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(detail, color=Muted)
    }
}
@Composable private fun Action(label:String, onClick:()->Unit) {
    Button(onClick=onClick, shape=RoundedCornerShape(0.dp),
        colors=ButtonDefaults.buttonColors(containerColor=Paper)) {
        Text(label, color=Navy, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold)
    }
}
