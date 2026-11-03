<div align="center">

# 🤖 FlowX Mobile Macro Engine
### บอทมาโครระบบอัตโนมัติความแม่นยำสูง สำหรับ GTA: San Andreas Mobile (SA-MP)
**ออกแบบและรองรับเป็นพิเศษสำหรับเซิร์ฟเวอร์ GTA Morgan City ([pay-morgan.com](https://pay-morgan.com/))**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://android.com)
[![Target Server](https://img.shields.io/badge/Target%20Server-GTA%20Morgan%20City-purple.svg)](https://pay-morgan.com/)
[![Architecture](https://img.shields.io/badge/Architecture-arm64--v8a-blue.svg)](https://developer.android.com/ndk)
[![Version](https://img.shields.io/badge/Version-1.2.8-orange.svg)](https://github.com/ZXD44/FlowX/releases)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

</div>

---

<div align="center">

## 🎮 ข้อมูลเกมและเซิร์ฟเวอร์เป้าหมาย (Game & Server Details)

| รายละเอียด | ข้อมูลที่รองรับ |
| :--- | :--- |
| **ตัวเกมที่รองรับ** | **GTA: San Andreas Mobile (SA-MP Android)** |
| **เซิร์ฟเวอร์เป้าหมาย** | **GTA Morgan City** (คอมมูนิตี้และระบบ RolePlay ยอดนิยม) |
| **เว็บไซต์หลักของเซิร์ฟ** | [https://pay-morgan.com/](https://pay-morgan.com/) |
| **กิจกรรมที่รองรับ** | • **ระบบขุดแร่ (Mining)** — แก้โจทย์ตัวเลขมินิเกมอัตโนมัติ<br>• **ระบบตกปลา (Fishing)** — จับจังหวะทุ่นและดึงเบ็ดอัตโนมัติ |
| **รูปแบบการทำงาน** | **HUD Overlay + Computer Vision (ML Kit)** แตะจออัตโนมัติโดยไม่ดัดแปลงไฟล์เกม ไม่ต้องรูท |

</div>

---

<div align="center">

## 📌 ภาพรวมการทำงาน (Overview)

</div>

**FlowX** คือแอปพลิเคชันมาโครช่วยเล่นระดับแอดวานซ์ที่พัฒนาขึ้นเพื่อเซิร์ฟเวอร์ **GTA Morgan City** โดยเฉพาะ ช่วยทุ่นแรงการทำกิจกรรมซ้ำซ้อนอย่างการขุดแร่และตกปลา โดยใช้ระบบประมวลผลภาพบนหน้าจอแบบเรียลไทม์ (Real-time Computer Vision) จับพิกัดและส่งคำสั่งสัมผัสผ่าน Android Accessibility API ทำงานเป็นเลเยอร์ลอยตัวอิสระเหนือตัวเกม ปลอดภัย ไม่ต้องแก้ไขตัวเกม (No APK Modding) และไม่ต้องเชื่อมต่อ API ภายนอกใดๆ ทั้งสิ้น 

---

<div align="center">

## ✨ ความสามารถหลัก (Key Features)

</div>

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

<div align="center">

## 🏗️ สถาปัตยกรรมระบบ (System Architecture)

</div>

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

<div align="center">

## 📂 โครงสร้างไดเรกทอรีโปรเจกต์ (Project Structure)

</div>

```text
FlowX/
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

<div align="center">

## 🚀 ข้อกำหนดและการติดตั้ง (Getting Started)

</div>

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

<div align="center">

## ❓ คำถามที่พบบ่อย (Q&A)

</div>

<details>
<summary><b>1. บอทตัวนี้ต้อง Root เครื่องหรือไม่?</b></summary>
<br>
ไม่ต้อง Root เครื่องครับ FlowX ทำงานผ่าน Android Accessibility Service และ MediaProjection API มาตรฐานของระบบ Android 100% ปลอดภัยต่อตัวเครื่อง
</details>

<details>
<summary><b>2. มีการแก้ไขหรือดัดแปลงไฟล์ตัวเกม GTA หรือไม่?</b></summary>
<br>
ไม่มีการแก้ไขไฟล์เกม (No Mod APK / No Data Patch) ตัวบอททำงานเป็นแอปแยกอิสระในรูปแบบ HUD Overlay ลอยอยู่เหนือหน้าจอเกมเท่านั้น
</details>

<details>
<summary><b>3. รองรับเกมและเซิร์ฟเวอร์อะไรบ้าง?</b></summary>
<br>
พัฒนาและปรับแต่งอัลกอริทึมการอ่านค่าสี/ตัวเลขมาเฉพาะสำหรับเซิร์ฟเวอร์ <b>GTA Morgan City</b> (<a href="https://pay-morgan.com/">pay-morgan.com</a>) บนแพลตฟอร์ม Android
</details>

<details>
<summary><b>4. ระบบ Anti-Ban ทำงานอย่างไร?</b></summary>
<br>
ระบบใช้เทคนิคจำลองพฤติกรรมมนุษย์ (Humanization) โดยการสุ่มค่าตำแหน่งสัมผัส ±3 ถึง ±12 พิกเซล และสุ่มระยะเวลานิ้วแตะค้าง 40ms - 70ms ไม่ซ้ำกันในแต่ละครั้ง เพื่อป้องกันการตรวจจับรูปแบบการคลิกซ้ำซ้อน
</details>

<details>
<summary><b>5. หากเปิดแอปแล้วหน้าต่างบอทไม่ขึ้น ต้องทำอย่างไร?</b></summary>
<br>
ตรวจสอบว่าได้เปิดสิทธิ์ <b>"แสดงทับแอปอื่น (Display over other apps)"</b> และ <b>"Accessibility Service"</b> ให้กับ FlowX ในการตั้งค่าของเครื่องแล้วหรือยัง
</details>

---

<div align="center">

## ✍️ ผู้พัฒนาและลายเซ็นดิจิทัล (Author & Signature)

Developed with ❤️ by **ZXD44** (ZirconX)  
*Next-Gen Mobile Automation & Computer Vision Engineering*

[![GitHub](https://img.shields.io/badge/GitHub-ZXD44-181717?style=flat&logo=github)](https://github.com/ZXD44)
[![Repository](https://img.shields.io/badge/Project-FlowX-blue?style=flat)](https://github.com/ZXD44/FlowX)

```text
  ███████╗██╗      ██████╗ ██╗    ██╗██╗  ██╗
  ██╔════╝██║     ██╔═══██╗██║    ██║╚██╗██╔╝
  █████╗  ██║     ██║   ██║██║ █╗ ██║ ╚███╔╝ 
  ██╔══╝  ██║     ██║   ██║██║███╗██║ ██╔██╗ 
  ██║     ███████╗╚██████╔╝╚███╔███╔╝██╔╝ ██╗
  ╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═╝
              Crafted by ZXD44
```

</div>
