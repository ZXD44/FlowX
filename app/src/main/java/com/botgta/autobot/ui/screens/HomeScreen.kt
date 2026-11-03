package com.botgta.autobot.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.R
import com.botgta.autobot.data.SecureStorage
import com.botgta.autobot.data.TranslationManager
import com.botgta.autobot.ui.components.TransactionRow
import com.botgta.autobot.ui.components.neumorphic

@Composable
fun HomeScreen(
    isOverlayGranted: Boolean,
    isAccessibilityGranted: Boolean,
    selectedJob: String,
    tokenTimeSeconds: Long,
    onEnterKey: (String) -> Unit,
    onGetFreeKey: () -> Unit,
    onShowGoogleAds: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantAccessibility: () -> Unit,
    limeGreen: Color,
    cardGradient: Brush,
    isDarkMode: Boolean,
    webVisits: Int,
    isAuthorized: Boolean = false
) {
    val t = TranslationManager
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val secondaryInk = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF555555)
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)
    val context = androidx.compose.ui.platform.LocalContext.current

    var showKeyDialog by remember { mutableStateOf(false) }
    var showAdSelectionDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf("") }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            containerColor = paperColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.neumorphic(radius = 12.dp, elevation = 8.dp, isDarkMode = isDarkMode),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(60.dp).background(highlightColor, RoundedCornerShape(8.dp)).border(2.dp, inkColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = inkColor, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text("เลือกช่องทางชำระเงิน", color = inkColor, fontWeight = FontWeight.Black, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RewardOption("ช่องทาง: PromptPay", "โอนเงิน 20.- ผ่านเว็บ ZirconX", Icons.Default.QrCodeScanner, highlightColor, inkColor, isDarkMode) {
                        showPaymentDialog = false
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.zirconx.my"))
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } catch (e: Exception) { }
                    }
                    RewardOption("ช่องทาง: True Wallet", "โอนเงิน 20.- ผ่านลิงก์ Wallet", Icons.Default.AccountBalanceWallet, highlightColor, inkColor, isDarkMode) {
                        showPaymentDialog = false
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://tmn.app.link/4e6SQXIKUxb"))
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } catch (e: Exception) { }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 2.dp, color = inkColor.copy(alpha = 0.1f))
                    
                    RewardOption("แจ้งโอนเงินด้วยสลิป", "ตรวจสอบและเติมเวลาทันที", Icons.Default.ReceiptLong, highlightColor, inkColor, isDarkMode) {
                        showPaymentDialog = false
                        val intent = android.content.Intent(context, com.botgta.autobot.ui.SlipVerificationActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPaymentDialog = false }) { Text(t.getString("btn_close"), color = secondaryInk, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) } }
        )
    }

    if (showAdSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showAdSelectionDialog = false },
            containerColor = paperColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.neumorphic(radius = 12.dp, elevation = 8.dp, isDarkMode = isDarkMode),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(60.dp).background(highlightColor, RoundedCornerShape(8.dp)).border(2.dp, inkColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CardGiftcard, null, tint = inkColor, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text(t.getString("reward_center"), color = inkColor, fontWeight = FontWeight.Black, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RewardOption("ShrinkMe.io", t.getString("shrinkme_desc"), Icons.Default.Link, highlightColor, inkColor, isDarkMode) {
                        showAdSelectionDialog = false
                        onGetFreeKey()
                    }
                    RewardOption("ZirconX Website", if (webVisits >= 3) "โควตาเต็ม (รีเซ็ต 00:00 น.)" else "${t.getString("website_desc")} ($webVisits/3)", Icons.Default.Public, highlightColor, inkColor, isDarkMode) {
                        showAdSelectionDialog = false
                        onShowGoogleAds()
                    }
                    RewardOption(t.getString("buy_key_title"), t.getString("buy_key_desc"), Icons.Default.Star, highlightColor, inkColor, isDarkMode) {
                        showAdSelectionDialog = false
                        showPaymentDialog = true
                    }
                    Text(text = if (webVisits >= 3) "คุณรับรางวัลวันนี้ครบแล้ว กลับมาใหม่หลังเที่ยงคืน" else t.getString("reward_info"), color = secondaryInk, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAdSelectionDialog = false }) { Text(t.getString("btn_close"), color = secondaryInk, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) } }
        )
    }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            containerColor = paperColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.neumorphic(radius = 12.dp, elevation = 8.dp, isDarkMode = isDarkMode),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(60.dp).background(highlightColor, RoundedCornerShape(8.dp)).border(2.dp, inkColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.VpnKey, null, tint = inkColor, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text(t.getString("enter_key_title"), color = inkColor, fontWeight = FontWeight.Black, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it.uppercase() },
                        placeholder = { Text(t.getString("enter_key_placeholder"), color = secondaryInk.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = inkColor, unfocusedBorderColor = inkColor, focusedTextColor = inkColor, unfocusedTextColor = inkColor),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = t.getString("enter_key_hint"), color = secondaryInk, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                Button(onClick = { showKeyDialog = false; onEnterKey(inputKey); inputKey = "" }, colors = ButtonDefaults.buttonColors(containerColor = highlightColor), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
                    Text(t.getString("btn_confirm"), color = inkColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = { TextButton(onClick = { showKeyDialog = false }) { Text(t.getString("btn_cancel"), color = secondaryInk, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(20.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(52.dp).neumorphic(radius = 8.dp, elevation = 4.dp, isDarkMode = isDarkMode), shape = RoundedCornerShape(8.dp), color = highlightColor, border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Face, null, tint = inkColor, modifier = Modifier.size(32.dp)) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(text = t.getString("welcome_back"), color = secondaryInk, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "FlowX", color = inkColor, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            if (isAuthorized) {
                                Spacer(Modifier.width(8.dp))
                                Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, inkColor)) {
                                    Text("ADMIN", color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth().height(180.dp).neumorphic(radius = 16.dp, elevation = 8.dp, isDarkMode = isDarkMode), 
                shape = RoundedCornerShape(16.dp), 
                color = highlightColor, 
                border = androidx.compose.foundation.BorderStroke(3.dp, inkColor)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t.getString("mana_remaining"), color = inkColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Surface(color = paperColor, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
                                val statusKey = if (tokenTimeSeconds > 0) "status_online" else "status_expired"
                                Text(text = "● ${t.getString(statusKey)}", color = inkColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (tokenTimeSeconds >= 999999L) t.getString("mana_unlimited") else {
                                val h = tokenTimeSeconds / 3600; val m = (tokenTimeSeconds % 3600) / 60; val s = tokenTimeSeconds % 60
                                String.format("%02d:%02d:%02d", h, m, s)
                            }, 
                            color = inkColor, 
                            fontSize = 48.sp, 
                            fontFamily = FontFamily.Monospace, 
                            fontWeight = FontWeight.Black, 
                            letterSpacing = (-2).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = inkColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t.getString("current_quest") + selectedJob, color = inkColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionButton(title = t.getString("btn_get_free_key"), subtitle = "Web: $webVisits/3 ครั้ง", icon = Icons.Default.CardGiftcard, color = paperColor, inkColor = inkColor, modifier = Modifier.weight(1f), isDarkMode = isDarkMode, onClick = { showAdSelectionDialog = true })
                QuickActionButton(title = t.getString("btn_enter_key"), subtitle = "Enter Key", icon = Icons.Default.VpnKey, color = paperColor, inkColor = inkColor, modifier = Modifier.weight(1f), isDarkMode = isDarkMode, onClick = { showKeyDialog = true })
            }
        }

        item { Text(text = t.getString("security_runes"), color = inkColor, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp)) }

        item { TransactionRow(icon = Icons.Default.Layers, title = t.getString("overlay_magic"), status = if (isOverlayGranted) t.getString("status_active") else t.getString("status_pending"), isOk = isOverlayGranted, onClick = onGrantOverlay, isDarkMode = isDarkMode) }
        item { TransactionRow(icon = Icons.Default.AccessibilityNew, title = t.getString("accessibility_rune"), status = if (isAccessibilityGranted) t.getString("status_active") else t.getString("status_pending"), isOk = isAccessibilityGranted, onClick = onGrantAccessibility, isDarkMode = isDarkMode) }
        
        item {
            Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).neumorphic(radius = 12.dp, elevation = 4.dp, isDarkMode = isDarkMode), color = paperColor, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = inkColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text = t.getString("shield_protecting"), color = secondaryInk, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RewardOption(title: String, subtitle: String, icon: ImageVector, highlightColor: Color, inkColor: Color, isDarkMode: Boolean, onClick: () -> Unit) {
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().neumorphic(radius = 8.dp, elevation = 4.dp, isDarkMode = isDarkMode), shape = RoundedCornerShape(8.dp), color = paperColor, border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(highlightColor, RoundedCornerShape(6.dp)).border(2.dp, inkColor, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = inkColor, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Text(subtitle, color = inkColor.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = inkColor)
        }
    }
}

@Composable
fun QuickActionButton(title: String, subtitle: String, icon: ImageVector, color: Color, inkColor: Color, isDarkMode: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(80.dp).neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode), shape = RoundedCornerShape(12.dp), color = color, border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)) {
        Box(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = inkColor.copy(alpha = 0.1f), modifier = Modifier.size(40.dp).align(Alignment.CenterEnd))
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(title, color = inkColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Text(subtitle, color = inkColor.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
