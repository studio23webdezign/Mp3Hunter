package com.studio23webdezign.mp3hunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.ui.DorkSearchPanel

private val Night = Color(0xFF090E17)
private val SurfaceDark = Color(0xFF121C2A)
private val Lime = Color(0xFFC7F36B)
private val TextMuted = Color(0xFF9BA9BA)
private val Cyan = Color(0xFF69D9D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DorkHunterApp() }
    }
}

@Composable
private fun DorkHunterApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Lime,
            secondary = Cyan,
            background = Night,
            surface = SurfaceDark,
            surfaceVariant = Color(0xFF1B293A),
            onPrimary = Night,
            onBackground = Color(0xFFF5F7FA),
            onSurface = Color(0xFFF5F7FA),
            onSurfaceVariant = Color(0xFFD2DBE6)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF142438), Night, Night)
                        )
                    )
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = Lime
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Dork search",
                            tint = Night,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "DORK HUNTER",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            "ADVANCED INDEX SEARCH",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF243B2A)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Lime, modifier = Modifier.size(14.dp))
                            Text("READY", color = Lime, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1A3040), Color(0xFF172333), Color(0xFF182337))
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Cyan, modifier = Modifier.size(15.dp))
                            Text("SEARCH THE OPEN WEB", color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                        }
                        Text(
                            "Find what search engines have indexed.",
                            color = Color.White,
                            fontSize = 20.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Build precise Google dorks from one search. Explore file types, exact phrases, domains, exclusions and more.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DORK SEARCH", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                    Text("QUERY BUILDER  •  GOOGLE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))

                DorkSearchPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 10.dp)
                )
            }
        }
    }
}
