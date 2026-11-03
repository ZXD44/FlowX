package com.botgta.autobot.data

object TranslationManager {
    
    enum class Lang { TH, EN }

    var currentLang = Lang.TH

    fun getString(key: String): String {
        return when (currentLang) {
            Lang.TH -> translationsTH[key] ?: key
            Lang.EN -> translationsEN[key] ?: key
        }
    }

    private val translationsTH = mapOf(
        // Tabs
        "tab_home" to "หน้าแรก",
        "tab_quest" to "โหมดงาน",
        "tab_gear" to "ตั้งค่า",
        
        // Home Screen
        "welcome_back" to "ยินดีต้อนรับ",
        "mana_remaining" to "เวลาคงเหลือ",
        "mana_unlimited" to "ไม่จำกัด",
        "status_online" to "ออนไลน์",
        "status_expired" to "หมดเวลา",
        "current_quest" to "โหมดปัจจุบัน: ",
        "btn_get_free_key" to "รับเวลาฟรี",
        "btn_enter_key" to "กรอกรหัส",
        "security_runes" to "การตรวจสอบความปลอดภัย",
        "overlay_magic" to "แสดงทับแอปอื่น",
        "accessibility_rune" to "การช่วยการเข้าถึง",
        "status_active" to "พร้อม",
        "status_pending" to "ยังไม่เปิด",
        "shield_protecting" to "ระบบรักษาความปลอดภัยทำงานปกติ",
        
        // Reward Center
        "reward_center" to "ศูนย์รับรางวัล",
        "shrinkme_desc" to "ดูโฆษณาสั้น >> รับเวลา +1 ชม.",
        "website_desc" to "เยี่ยมชมเว็บ >> รับเวลา +1 ชม.",
        "buy_key_title" to "ซื้อคีย์เติมเวลา",
        "buy_key_desc" to "สนับสนุนผู้พัฒนา เพื่อให้บอทพัฒนาต่อไปได้",
        "reward_info" to "เลือกช่องทาง >> ทำตามเงื่อนไข >> กลับมารับเวลาในแอป",
        "btn_close" to "ปิด",
        
        // Enter Key
        "enter_key_title" to "เติมรหัสรับเวลา",
        "enter_key_placeholder" to "ใส่รหัส ZXD-VIP-XXXX",
        "enter_key_hint" to "รับรหัสได้จากกิจกรรมหรือเว็บไซต์",
        "btn_confirm" to "ยืนยัน",
        "btn_cancel" to "ยกเลิก",
        
        // Quest Selection
        "choose_quest" to "เลือกกิจกรรมที่ต้องการ",
        "quest_desc" to "เลือกโหมดเพื่อให้บอทเริ่มทำงานอัตโนมัติ",
        "fishing_title" to "ตกปลาอัตโนมัติ",
        "fishing_desc" to "หาปลาอัตโนมัติ พร้อมระบบแก้แคปช่า",
        "mining_title" to "ขุดเหมืองอัตโนมัติ",
        "mining_desc" to "ขุดแร่ด้วยระบบสแกนภาพแม่นยำสูง",
        
        // Settings
        "system_config" to "การตั้งค่าระบบ",
        "config_desc" to "ปรับแต่งการทำงานให้เข้ากับการใช้งานของคุณ",
        "display_themes" to "การแสดงผลและธีม",
        "lantern_mode" to "โหมดมืด (Dark Mode)",
        "show_scanner" to "แสดงพื้นที่สแกนบนจอ",
        "behavior_speed" to "ความเร็วและพฤติกรรม",
        "action_speed" to "ความเร็วการทำงาน: ",
        "humanize_actions" to "ระบบเลียนแบบมนุษย์",
        "humanize_desc" to "สุ่มจังหวะการกดเพื่อความปลอดภัย",
        "anti_afk_rune" to "ระบบพักอัตโนมัติ (Anti-AFK)",
        "anti_afk_desc" to "สุ่มหยุดพักเพื่อลดความเสี่ยง",
        "app_info" to "ข้อมูลแอปพลิเคชัน",
        "curr_version" to "เวอร์ชันปัจจุบัน: ",
        "btn_check_update" to "ตรวจสอบการอัปเดต",
        "language_setting" to "ภาษา (Language)",
        "admin_unlimited" to "สิทธิ์เวลาไม่จำกัด (Admin)",
        "admin_unlimited_desc" to "เปิดเพื่อให้เวลาไม่จำกัด / ปิดเพื่อทดสอบโฆษณา",
        
        // Bot Overlay
        "overlay_title" to "แผงควบคุมบอท",
        "overlay_ready" to "พร้อมทำงาน",
        "overlay_running" to "กำลังทำงาน...",
        "btn_deploy" to "เริ่มทำงาน",
        "btn_recall" to "หยุดทำงาน",
        "mana_label" to "เวลา: ",
        "log_start" to "เริ่มการทำงาน...",
        "log_stop" to "หยุดการทำงาน",
        "log_no_mana" to "เวลาหมด! กรุณาเติมเวลา",
        
        // Notifications
        "notif_success_ad" to "รับรางวัลสำเร็จ! เพิ่มเวลาใช้งานแล้ว",
        "notif_success_web" to "เยี่ยมชมสำเร็จ! เพิ่มเวลาใช้งานแล้ว",
        "notif_limit_reached" to "โควตาวันนี้เต็มแล้ว! รีเซ็ต 00:00 น. 🌙",
        "notif_web_count" to "สำเร็จ! (สะสมวันนี้ %d/3 ครั้ง)",
        "notif_key_activated" to "เติมรหัสสำเร็จ! +12 ชม.",
        "notif_key_invalid" to "รหัสไม่ถูกต้อง หรือหมดอายุแล้ว",
        "notif_key_format" to "รูปแบบรหัสผิด (ZXD-VIP-XXXX)",
        "notif_key_cooldown" to "กรุณารอสักครู่ก่อนรับรางวัลอีกครั้ง",
        "notif_mana_full" to "เวลาสะสมเต็มแล้ว (สูงสุด 24 ชม.)",
        "notif_bot_started" to "เริ่มทำงานเรียบร้อย",
        "notif_bot_stopped" to "หยุดทำงานเรียบร้อย",
        "notif_grant_permission" to "กรุณาเปิดสิทธิ์การใช้งานก่อน",
        "notif_update_required" to "จำเป็นต้องอัปเดตแอปเวอร์ชันล่าสุด",
        "notif_recovered" to "ระบบทำการกู้คืนเวลาใช้งานให้แล้ว",
        
        // Update Dialog
        "update_title" to "พบเวอร์ชันใหม่!",
        "update_changelog" to "บันทึกการเปลี่ยนแปลง:",
        "btn_update_now" to "อัปเดตทันที",
        "update_downloading" to "กำลังดาวน์โหลด...",
        "update_latest" to "แอปเป็นเวอร์ชันล่าสุดแล้ว"
    )

    private val translationsEN = mapOf(
        // Tabs
        "tab_home" to "Home",
        "tab_quest" to "Activities",
        "tab_gear" to "Settings",
        
        // Home Screen
        "welcome_back" to "Welcome back",
        "mana_remaining" to "Time Remaining",
        "mana_unlimited" to "Unlimited",
        "status_online" to "Online",
        "status_expired" to "Expired",
        "current_quest" to "Current Mode: ",
        "btn_get_free_key" to "Get Free Time",
        "btn_enter_key" to "Enter Key",
        "security_runes" to "Security & Status",
        "overlay_magic" to "Display over apps",
        "accessibility_rune" to "Accessibility Service",
        "status_active" to "Ready",
        "status_pending" to "Inactive",
        "shield_protecting" to "Security system is active.",
        
        // Reward Center
        "reward_center" to "Reward Center",
        "shrinkme_desc" to "Short ads >> Get +1h time",
        "website_desc" to "Visit web >> Get +1h time",
        "buy_key_title" to "Buy Access Key",
        "buy_key_desc" to "Support development to keep the project alive.",
        "reward_info" to "Choose path >> Follow steps >> Get reward time.",
        "btn_close" to "Close",
        
        // Enter Key
        "enter_key_title" to "Enter Access Key",
        "enter_key_placeholder" to "Enter ZXD-VIP-XXXX",
        "enter_key_hint" to "Keys can be found in our community.",
        "btn_confirm" to "Confirm",
        "btn_cancel" to "Cancel",
        
        // Quest Selection
        "choose_quest" to "Choose Activity",
        "quest_desc" to "Select a mode for automatic operation.",
        "fishing_title" to "Auto Fishing",
        "fishing_desc" to "Automated fishing with captcha bypass.",
        "mining_title" to "Auto Mining",
        "mining_desc" to "Precision mining using image scanning.",
        
        // Settings
        "system_config" to "System Settings",
        "config_desc" to "Adjust app behavior to your needs.",
        "display_themes" to "Display & Themes",
        "lantern_mode" to "Dark Mode",
        "show_scanner" to "Show Scanner Area",
        "behavior_speed" to "Speed & Behavior",
        "action_speed" to "Execution Speed: ",
        "humanize_actions" to "Humanize Actions",
        "humanize_desc" to "Randomize clicks for safety.",
        "anti_afk_rune" to "Anti-AFK System",
        "anti_afk_desc" to "Takes random breaks to evade detection.",
        "app_info" to "App Information",
        "curr_version" to "Current Version: ",
        "btn_check_update" to "Check for Updates",
        "language_setting" to "App Language",
        "admin_unlimited" to "Admin Unlimited Time",
        "admin_unlimited_desc" to "Enable for unlimited / Disable for ad testing",
        
        // Bot Overlay
        "overlay_title" to "Bot Control Panel",
        "overlay_ready" to "Ready",
        "overlay_running" to "Running...",
        "btn_deploy" to "START",
        "btn_recall" to "STOP",
        "mana_label" to "Time: ",
        "log_start" to "Starting operation...",
        "log_stop" to "Stopping operation",
        "log_no_mana" to "Out of time! Please recharge.",
        
        // Notifications
        "notif_success_ad" to "Reward received! Time added.",
        "notif_success_web" to "Visit success! Time added.",
        "notif_limit_reached" to "Daily limit reached.",
        "notif_web_count" to "Website visits today: %d/3",
        "notif_key_activated" to "Key activated! +12h time.",
        "notif_key_invalid" to "Invalid or expired key.",
        "notif_key_format" to "Invalid key format (ZXD-VIP-XXXX)",
        "notif_key_cooldown" to "Please wait before claiming again.",
        "notif_mana_full" to "Max time reached (24 hours).",
        "notif_bot_started" to "Bot started successfully.",
        "notif_bot_stopped" to "Bot stopped successfully.",
        "notif_grant_permission" to "Please grant permissions first.",
        "notif_update_required" to "Update to latest version required.",
        "notif_recovered" to "Access time has been recovered.",
        
        // Update Dialog
        "update_title" to "New Version Available!",
        "update_changelog" to "Changelog:",
        "btn_update_now" to "Update Now",
        "update_downloading" to "Downloading...",
        "update_latest" to "App is up to date"
    )
}
