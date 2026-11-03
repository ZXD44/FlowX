# 🤖 MCF-ZXD44 BOT-GTA

> **แอปพลิเคชันระบบอัตโนมัติความแม่นยำสูงสำหรับ Android (GTA Mobile)**  
> พัฒนาด้วย Native Kotlin, Jetpack Compose, ML Kit Computer Vision และ Android Accessibility API

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://android.com)
[![Architecture](https://img.shields.io/badge/Architecture-arm64--v8a-blue.svg)](https://developer.android.com/ndk)
[![Version](https://img.shields.io/badge/Version-1.2.7-orange.svg)](archive/releases/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

---

## 📌 บทนำและภาพรวม (Introduction)

**MCF-ZXD44** ถูกพัฒนาขึ้นเพื่อจำลองการสั่งงานหน้าจอของมนุษย์อย่างแม่นยำและเสถียรที่สุด สำหรับมินิเกมและระบบกิจกรรมซ้ำซ้อนในเกม GTA Mobile เช่น **ระบบขุดแร่ (Mining)** และ **ระบบตกปลา (Fishing)**

ตัวแอปทำงานเป็นอิสระเหนือเกม (HUD Overlay) โดยไม่ต้องเชื่อมต่อผ่าน API ของเซิร์ฟเวอร์เกม ไม่ต้องเจาะระบบไฟล์ตัวเกม และไม่จำเป็นต้อง Root เครื่อง 

---

## ✨ ความสามารถหลัก (Key Features)

### 1. ⛏️ โหมดขุดแร่อัจฉริยะ (Smart Mining Engine)
- **High-Speed Minigame Solver**: ตรวจจับกล่องคำถามมินิเกมและตัวเลขเป้าหมายสีเหลือง
- **Parallel OCR Processing**: ประมวลผลปุ่มตัวเลขสีขาวทุกปุ่มพร้อมกันด้วย Google ML Kit Vision เพิ่มความเร็วในการตอบสนอง
- **Locked Target Memory**: บันทึกพิกัดเป้าหมาย/ตำแหน่งนิ้วขุดอัตโนมัติเมื่อกดติด เพื่อข้ามสเต็ปการสแกนซ้ำซ้อน
- **Auto Alert Dismiss**: ตรวจจับปุ่มยืนยันสีฟ้า (เช่น ข้อความเตือน หรือผลลัพธ์) แล้วกดเคลียร์ให้อัตโนมัติ

### 2. 🎣 โหมดตกปลา (Adaptive Fishing Engine)
- **White Blob & Contour Detection**: สแกนหาทุ่นและปุ่มวงกลมสีขาวบนหน้าจอ
- **OCR Keyword Recognition**: ตรวจสอบสถานะเกมแบบเรียลไทม์ เช่น *"เริ่มใหม่"*, *"พลาด"*, หรือ *"กระเป๋าเต็ม"*
- **Bag Full Protection**: ระบบหยุดทำงานอัตโนมัติเมื่อกระเป๋าเต็ม เพื่อป้องกันการสูญเสียเหยื่อและลดพฤติกรรมผิดสังเกต

### 3. 🛡️ กลไกความปลอดภัยและการพรางตัว (Anti-Ban & Stealth)
- **Humanized Gesture**: สุ่ม Offset ตำแหน่งคลิก (±3 ถึง ±12 พิกเซล)
- **Dynamic Timing & Duration**: สุ่มหน่วงเวลาการแตะ (40ms - 70ms) เหมือนปลายนิ้วคนจริง
- **Anti-Ban Break Pattern**: พักบอทเป็นช่วงๆ ตามระยะเวลาที่ตั้งค่า
- **No-Root & Sandboxed**: ปลอดภัย ทำงานบน Service มาตรฐานของ Android

### 4. 💎 ส่วนต่อประสานผู้ใช้ลอยตัว (Cyberpunk HUD Overlay)
- **Floating HUD Controller**: ควบคุม สลับโหมด เริ่ม/หยุด ได้โดยไม่ต้องสลับแอป
- **Live Status Stream**: รายงานสถานะ Loop, พิกัดตรวจจับ, และเวลาโควต้า/โทเคนแบบสดๆ
- **Glassmorphism / Neumorphic Theme**: ดีไซน์ล้ำสมัย สวยงาม ไม่บดบังวิสัยทัศน์ขณะเล่น

---

## 🏗️ สถาปัตยกรรมระบบ (System Architecture)

```
┌────────────────────────────────────────────────────────┐
│               FloatingMenuService (UI HUD)             │
│   • หน้าต่างควบคุมลอยตัว เหนือเกม                       │
│   • สลับโหมด (Mining / Fishing) และแสดงสถานะสด         │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                 BotCore (Engine Loop)                  │
│   • ตัวควบคุมลูปหลักของระบบ                            │
│   • ประสานงานระหว่างภาพถ่ายหน้าจอและการตัดสินใจ        │
│   • จัดการ Speed Multiplier และระบบหน่วงเวลา           │
└─────────────┬───────────────────────────┬──────────────┘
              │                           │
              ▼                           ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│      MiningManager       │  │      FishingManager      │
│ • ตัวแก้โจทย์ตัวเลขมินิเกม │  │ • วิเคราะห์ทุ่นตกปลา     │
│ • Parallel Vision OCR    │  │ • ตรวจสอบคีย์เวิร์ดหน้าจอ │
│ • เคลียร์ Alert สีฟ้า    │  │ • ตรวจจับกระเป๋าเต็ม      │
└─────────────┬────────────┘  └───────────┬──────────────┘
              │                           │
              └─────────────┬─────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│             GestureControlService (Android)            │
│   • จับภาพหน้าจอระดับฮาร์ดแวร์ (MediaProjection/API30) │
│   • จำลอง Touch Gesture (AccessibilityNode / Path)     │
└────────────────────────────────────────────────────────┘
```

---

## 📂 โครงสร้างไดเรกทอรีโปรเจกต์ (Project Structure)

```text
BOT-GTA/
├── app/                      # โค้ดโปรเจกต์ Android ทั้งหมด
│   ├── src/main/java/com/botgta/autobot/
│   │   ├── core/             # BotCore และวงรอบการทำงานหลัก
│   │   ├── logic/            # ตรรกะตรวจจับ MiningManager & FishingManager
│   │   ├── services/         # GestureControlService & FloatingMenuService
│   │   ├── ui/               # ส่วนติดต่อผู้ใช้ Jetpack Compose & Activities
│   │   └── data/             # ความปลอดภัย, สิทธิ์ และเครือข่าย
│   └── src/main/res/         # เลย์เอาต์ XML, เวกเตอร์, ธีม และ Drawables
├── assets/                   # คลังทรัพยากร
│   ├── branding/             # โลโก้, ฟอนต์, ไอคอน
│   ├── debug/                # ข้อมูลและภาพสำหรับทดสอบระบบ Detect
│   ├── docs-images/          # ภาพประกอบเอกสาร
│   └── slips/                # ตัวอย่างสลิปสำหรับทดสอบระบบยืนยัน
├── docs/                     # เอกสารอธิบายการทำงานและตรรกะระบบ
│   ├── technical/            # เอกสารเชิงลึกสำหรับนักพัฒนา
│   ├── getting-started.md    # คู่มือการติดตั้งและใช้งานสำหรับผู้ใช้
│   └── logic-module.md       # ตรรกะการวิเคราะห์ภาพของแต่ละโมดูล
├── archive/                  # ที่เก็บไฟล์ Release APK และ Backups
├── scripts/                  # Shell Scripts อำนวยความสะดวกในการทดสอบ/Deploy
├── build.gradle.kts          # รหัสคอมไพล์โปรเจกต์
└── README.md                 # เอกสารแนะนำนี้
```

---

## 🚀 ข้อกำหนดและการติดตั้ง (Getting Started)

### ความต้องการของระบบ (Prerequisites)
1. อุปกรณ์ Android เวอร์ชั่น 8.0 (API Level 26) ขึ้นไป (แนะนำ Android 11+ เพื่อประสิทธิภาพการจับภาพหน้าจอสูงสุด)
2. สถาปัตยกรรม CPU: **arm64-v8a** (รองรับ Snapdragon 8 Gen 2 / Gen 3 และอุปกรณ์ 64-bit รุ่นใหม่)
3. เปิดสิทธิ์การเข้าถึง:
   - **Accessibility Service** (เพื่อจำลองการคลิก)
   - **Display over other apps** (เพื่อแสดงผลเมนูลอย)
   - **Screen Capture Permission** (เพื่อประมวลผลมินิเกม)

### ขั้นตอนการคอมไพล์ (Build from Source)
```bash
# คอมไพล์ไฟล์ APK โหมด Debug
./gradlew assembleDebug

# คอมไพล์ไฟล์ APK โหมด Release พร้อมลงนาม
./gradlew assembleRelease
```
*ไฟล์ APK ที่สร้างเสร็จแล้วจะอยู่ที่: `app/build/outputs/apk/`*

---

## 🔒 นโยบายความปลอดภัยและข้อจำกัดความรับผิดชอบ (Disclaimer)
โปรเจกต์นี้จัดทำขึ้นเพื่อการศึกษา วิจัยด้าน Computer Vision บนระบบปฏิบัติการ Android และการทำงานร่วมกับ Accessibility Services เท่านั้น ผู้พัฒนาไม่สนับสนุนการนำไปใช้ในทางที่ละเมิดข้อตกลงการใช้งาน (Terms of Service) ของผู้ให้บริการเกมใดๆ ทั้งสิ้น ผู้ใช้งานต้องยอมรับความเสี่ยงที่อาจเกิดขึ้นด้วยตนเอง
