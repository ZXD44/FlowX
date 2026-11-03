# 🤖 Agent System Configuration & Context Guide

แฟ้มนี้สร้างขึ้นเพื่อให้ AI Agent / Developer เข้าใจบริบท กฎเกณฑ์ สถาปัตยกรรม และทักษะที่เกี่ยวข้องของโปรเจกต์ **FlowX** ได้อย่างสมบูรณ์แบบในทันที

---

## 📌 1. บริบทของโปรเจกต์ (Project Mission & Context)
- **ชื่อโปรเจกต์**: FlowX (`com.botgta.autobot`)
- **แพลตฟอร์ม**: Android Application (Kotlin, Jetpack Compose, XML ViewBinding, ML Kit, Accessibility)
- **เป้าหมายหลัก**: เป็นแอปพลิเคชันระบบอัตโนมัติความเร็วและความแม่นยำสูง สำหรับเล่นมินิเกมและภารกิจซ้ำซ้อนในเกม GTA Mobile ผ่านหน้าต่างลอยตัว (HUD Overlay) โดยไม่ผ่าน API เซิร์ฟเวอร์เกม และไม่ต้องการสิทธิ์ Root

---

## 🏛️ 2. สถาปัตยกรรมระดับโค้ด (Code Architecture)

```
app/src/main/java/com/botgta/autobot/
├── core/
│   ├── BotCore.kt             # เอนจินควบคุมลูปหลัก ลูปตรวจสอบความปลอดภัย และการตั้งค่าความเร็ว
│   └── UpdateManager.kt       # ระบบเช็คเวอร์ชันและดาวน์โหลด APK อัปเดต
├── logic/
│   ├── MiningManager.kt       # ตรรกะแก้โจทย์มินิเกมขุดแร่, Parallel ML Kit OCR, ตรวจจับปุ่มสีฟ้า
│   └── FishingManager.kt      # ตรรกะตรวจจับทุ่นปลา (White Blob) และตรวจจับคีย์เวิร์ดหน้าจอ
├── services/
│   ├── GestureControlService.kt # Accessibility Service จำลองการแตะหน้าจอ และจับภาพหน้าจอ
│   └── FloatingMenuService.kt   # บริการหน้าต่างเมนูลอยตัว (Overlay HUD) สลับโหมดและแสดงสถานะ
├── ui/
│   ├── LauncherActivity.kt    # จุดเริ่มต้นแอป ตรวจสอบสิทธิ์ และแสดงแดชบอร์ดหลัก
│   ├── screens/               # หน้าจอ UI Compose (HomeScreen, SettingsScreen, ActivitySelectionScreen)
│   ├── components/            # UI Components สไตล์ Glassmorphism และ Neumorphic
│   ├── SlipVerificationActivity.kt # หน้ายืนยันสลิปเพื่อปลดล็อกการใช้งาน (ML Kit OCR)
│   └── RewardActivity.kt      # หน้าระบบเวลา/โทเคน
└── data/
    ├── SecureStorage.kt       # เข้ารหัสจัดเก็บการตั้งค่าและคีย์
    ├── SecurityUtils.kt       # ตรวจสอบความถูกต้องของ Package และ Signature
    └── NetworkUtils.kt        # ฟังก์ชันติดต่อเครือข่าย
```

---

## ⚖️ 3. กฎเหล็กของโปรเจกต์ (Engineering Rules & Constraints)

1. **64-bit Architecture Constraint**:
   - บังคับ `ndk { abiFilters.add("arm64-v8a") }` เท่านั้น ห้ามใส่ 32-bit (armeabi-v7a) เนื่องจากอุปกรณ์รุ่นใหม่ เช่น Snapdragon 8 Gen 3 หรือ Vivo/Oppo Installer จะปฏิเสธการติดตั้ง
   - บังคับใช้ `jniLibs.useLegacyPackaging = true` เพื่อรองรับ 16KB Page Size ของ Android 15/16

2. **Accessibility & Humanization Invariants**:
   - การแตะหน้าจอห้ามใช้พิกัดคงที่ (Static Coordinates) เด็ดขาด ต้องมี Random Jitter ±3 ถึง ±12 px
   - ห้ามแตะด้วยระยะเวลาคงที่ ต้องสุ่มความยาวการแตะระหว่าง 40ms ถึง 70ms เพื่อพรางตัวจากการตรวจจับ

3. **High-Performance Vision Standards**:
   - การส่งภาพให้ Google ML Kit ต้องทำการ Crop เฉพาะพื้นที่ความสนใจ (ROI) ก่อนเสมอ ห้ามส่งภาพเต็มหน้าจอเข้าไปใน OCR Loop
   - การอ่านปุ่มตัวเลือกหลายปุ่มในมินิเกม ต้องใช้ Coroutines รันแบบขนาน (Parallel async) เพื่อลด Latency

4. **Code Quality Rules**:
   - ฟังก์ชันตรรกะต้องคงไว้ซึ่ง Early Return / Guard Clauses
   - ความซับซ้อน Cyclomatic Complexity ไม่ควรเกิน 7 ต่อฟังก์ชันธุรกิจ
   - ห้ามสร้างโฟลเดอร์ขยะประเภท `utils/` หรือ `common/` ให้วางฟังก์ชันตัวช่วยไว้ใน Domain Feature หรือโมดูลที่เกี่ยวข้องโดยตรง

---

## 🛠️ 4. ทักษะที่เกี่ยวข้องในการพัฒนา (Developer Skills & Knowledge Map)

| ทักษะ / เทคโนโลยี | สิ่งที่ต้องรู้ในโปรเจกต์นี้ |
|---|---|
| **Android AccessibilityService** | การสร้าง `GestureDescription`, การวาด `Path`, การส่ง `dispatchGesture()` แบบ Non-blocking |
| **MediaProjection & Screen Capture** | การเรียก `takeScreenshot()` ใน Android 11+ (API 30+) ผ่าน AccessibilityService |
| **Google ML Kit Text Recognition** | การแปลง `Bitmap` สู่ `InputImage`, การทำ Grayscale + Thresholding เพื่อเพิ่มความคมชัดของข้อความ |
| **Jetpack Compose + Glassmorphism** | การใช้ Custom Modifier วาดเงามืด/เงาสว่าง (Neumorphism), Blur Effect, และ Gradient Panels |
| **Gradle & ProGuard** | การตั้งค่า R8 Obfuscation และ ProGuard Rules ใน `proguard-rules.pro` เพื่อป้องกันการถูกแกะโค้ด |

---

## 🔄 5. ขั้นตอนการตรวจสอบและคอมไพล์ (Verification Commands)
```bash
# ตรวจสอบการคอมไพล์ Debug
./gradlew assembleDebug

# ตรวจสอบการคอมไพล์ Release
./gradlew assembleRelease

# ตรวจสอบ Lint และความถูกต้อง
./gradlew check
```
