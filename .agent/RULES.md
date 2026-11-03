# 📜 Project Rules & Invariants for Agentic Workflows

เอกสารข้อบังคับและข้อกำหนดมาตรฐานในการแก้ไขและพัฒนาโค้ดสำหรับโปรเจกต์นี้

## 1. การเปลี่ยนแปลงสถาปัตยกรรมระดับระบบ
- **ห้ามแก้ไข Target SDK หรือ NDK Filter เป็น 32-bit**: ต้องคง `arm64-v8a` ไว้เสมอ
- **ห้ามปิด `useLegacyPackaging`**: จำเป็นต่อการรันบน Android 15+ (16KB Page Size)
- **รักษาคีย์การลงนาม**: เก็บการอ้างอิง `release.jks` ใน `signingConfigs` ให้คงเดิมเสมอ

## 2. การควบคุมความแม่นยำในการสัมผัสหน้าจอ (Gesture & Timing)
- ทุกการส่งสัมผัสผ่าน `GestureControlService` จะต้องผ่านการคำนวณ Random Offset เสมอ
- ห้ามใช้คำสั่ง `Thread.sleep()` ใน Main Thread หรือ Coroutines หลัก ให้ใช้ `delay()` ของ Kotlin Coroutines เท่านั้น

## 3. มารยาทการเขียนโค้ด (Clean Code Standard)
- ไม่เขียนคอมเมนต์อธิบายกลไกภาษาที่เห็นได้ชัดเจน (เช่น `// increment index` หรือ `// return value`)
- โค้ดต้องสื่อความหมายในตัวเอง (Self-documenting code)
- จัดการข้อผิดพลาดอย่างชัดเจนเสมอ ห้ามใช้ `catch (e: Exception) {}` ว่างเปล่าโดยไม่มีการบันทึก Log หรือการ Fallback
