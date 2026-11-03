#!/bin/bash

# FlowX Deployment Tool (Updated Path-Safe Version)
# รวมฟังก์ชัน Build และ Upload ไว้ในที่เดียว

# ย้ายตำแหน่งไปที่ Root ของโปรเจกต์ก่อนเริ่มทำงาน
cd "$(dirname "$0")/.." || exit

# โหลด Token จากไฟล์ .env
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

clear
echo "=============================================="
echo "          🛡️ FLOWX DEPLOYMENT TOOL 🛡️"
echo "=============================================="
echo "1) Build APK เท่านั้น (เก็บไว้ในเครื่อง)"
echo "2) Build และ Upload ขึ้น GitHub (ปล่อยอัปเดต)"
echo "3) Build และติดตั้งไปยังมือถือ (USB Debug)"
echo "4) ติดตั้งไฟล์ล่าสุดจากโฟลเดอร์ (ไม่ต้อง Build ใหม่)"
echo "5) ออกจากโปรแกรม"
echo "----------------------------------------------"
read -p "กรุณาเลือกหัวข้อ (1-5): " choice

case $choice in
    1|2|3)
        OLD_V=""
        NEW_V=""
        if [ "$choice" == "2" ]; then
            OLD_V=$(grep "versionName =" app/build.gradle.kts | head -1 | awk -F'"' '{print $2}')
            IFS='.' read -r v1 v2 v3 <<< "$OLD_V"
            [ -z "$v1" ] && v1=1
            [ -z "$v2" ] && v2=0
            [ -z "$v3" ] && v3=0
            
            v3=$((v3 + 1))
            if [ $v3 -gt 9 ]; then
                v3=0
                v2=$((v2 + 1))
            fi
            if [ $v2 -gt 9 ]; then
                v2=0
                v1=$((v1 + 1))
            fi

            NEW_V="$v1.$v2.$v3"
            echo "📈 กำลังอัปเกรดเวอร์ชันอัตโนมัติ: $OLD_V -> $NEW_V"
            sed -i "s/versionName = \"$OLD_V\"/versionName = \"$NEW_V\"/g" app/build.gradle.kts
        fi

        echo "🚀 เริ่มขั้นตอนการ Build APK (Release Mode)..."
        ./gradlew clean
        ./gradlew assembleRelease
        
        if [ $? -ne 0 ]; then
            echo "❌ Build ล้มเหลว! กำลังยกเลิกการอัปเกรด..."
            if [ "$choice" == "2" ]; then
                sed -i "s/versionName = \"$NEW_V\"/versionName = \"$OLD_V\"/g" app/build.gradle.kts
                echo "⏪ คืนค่าเวอร์ชันเป็น $OLD_V"
            fi
            exit 1
        fi
        
        # ปรับ Path การเก็บไฟล์ APK ให้ไปที่โฟลเดอร์ archive/releases
        mkdir -p archive/releases
        VERSION_NAME=$(grep "versionName =" app/build.gradle.kts | head -1 | awk -F'"' '{print $2}')
        [ -z "$VERSION_NAME" ] && VERSION_NAME="1.0.0"
        CUR_DATE=$(date +%Y%m%d_%H%M)
        TARGET_NAME="FlowX_v${VERSION_NAME}.apk"
        
        cp app/build/outputs/apk/release/app-release.apk archive/releases/$TARGET_NAME
        echo "✅ Build สำเร็จ: $TARGET_NAME"
        
        if [ "$choice" == "2" ]; then
            if [ -z "$GITHUB_TOKEN" ]; then
                echo "❌ ไม่พบ GITHUB_TOKEN ใน .env"
                exit 1
            fi

            OWNER="ZXD44"
            REPO="FlowX"
            TAG_NAME="v$VERSION_NAME"
            
            echo "📤 กำลังอัปโหลด $TARGET_NAME ไปยัง GitHub..."
            
            RELEASE_JSON=$(curl -s -X POST \
              -H "Authorization: token $GITHUB_TOKEN" \
              -H "Accept: application/vnd.github.v3+json" \
              https://api.github.com/repos/$OWNER/$REPO/releases \
              -d "{
                \"tag_name\": \"$TAG_NAME\",
                \"name\": \"FlowX $TAG_NAME — Official Release\",
                \"body\": \"### มีอะไรใหม่ใน FlowX $TAG_NAME\\n- เปลี่ยนชื่อแบรนด์และโฉมใหม่เป็น FlowX\\n- เพิ่มความแม่นยำระบบ Parallel OCR ในการแก้โจทย์ตัวเลข\\n- ปรับจังหวะแตะหน้าจอแบบ Humanization ให้เนียนและปลอดภัยยิ่งขึ้น\\n- ปรับปรุงความเสถียรของหน้าต่าง Overlay HUD\\n\\n📥 **ไฟล์ติดตั้ง:** \`$TARGET_NAME\`\",
                \"draft\": false,
                \"prerelease\": false
              }")

            RELEASE_ID=$(echo $RELEASE_JSON | jq -r '.id')
            if [ "$RELEASE_ID" == "null" ] || [ -z "$RELEASE_ID" ]; then
                echo "❌ สร้าง Release ล้มเหลว"
                exit 1
            fi

            UPLOAD_URL="https://uploads.github.com/repos/$OWNER/$REPO/releases/$RELEASE_ID/assets?name=$TARGET_NAME"
            UPLOAD_RESPONSE=$(curl -s -X POST \
              -H "Authorization: token $GITHUB_TOKEN" \
              -H "Content-Type: application/vnd.android.package-archive" \
              --data-binary @"archive/releases/$TARGET_NAME" \
              "$UPLOAD_URL")

            echo "✅ อัปโหลดขึ้น GitHub สำเร็จ!"
        fi

        if [ "$choice" == "3" ]; then
            ADB_CMD="adb"
            if ! command -v adb &> /dev/null; then
                ADB_CMD="$HOME/Android/Sdk/platform-tools/adb"
            fi
            
            if [ -f "$ADB_CMD" ] || command -v adb &> /dev/null; then
                echo "🚀 กำลังติดตั้งลงมือถือ..."
                $ADB_CMD install -r "archive/releases/$TARGET_NAME"
                $ADB_CMD shell monkey -p com.botgta.autobot -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
                echo "✅ ติดตั้งและเปิดแอปเรียบร้อย"
            fi
        fi
        ;;
    4)
        LATEST_APK=$(ls -t archive/releases/*.apk 2>/dev/null | head -1)
        if [ ! -z "$LATEST_APK" ]; then
            echo "📲 ติดตั้งไฟล์ล่าสุด: $LATEST_APK"
            adb install -r "$LATEST_APK"
            adb shell monkey -p com.botgta.autobot -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
        fi
        ;;
    5)
        echo "👋 ออกจากโปรแกรม"
        exit 0
        ;;
    *)
        echo "❌ ตัวเลือกไม่ถูกต้อง"
        ;;
esac
