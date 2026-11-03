#!/bin/bash
FILE="app/src/main/java/com/botgta/autobot/ui/LauncherActivity.kt"

# Replace background gradient
sed -i 's/val bgGradient = Brush.verticalGradient(/val bgGradient = Brush.verticalGradient(/g' $FILE # placeholder

# Replace colors
sed -i 's/Color(0xFF0F172A)/Color(0xFFE0E5EC)/g' $FILE
sed -i 's/Color(0xFF1E1B4B)/Color(0xFFE0E5EC)/g' $FILE
sed -i 's/Color(0xFFA855F7)/Color(0xFF3B82F6)/g' $FILE
sed -i 's/Color(0xFF6B21A8)/Color(0xFF2563EB)/g' $FILE
sed -i 's/Color(0xFFBEF264)/Color(0xFF3B82F6)/g' $FILE
sed -i 's/Color(0xFF1E293B)/Color(0xFFE0E5EC)/g' $FILE
sed -i 's/Color(0xFF1E1E1E)/Color(0xFFE0E5EC)/g' $FILE
sed -i 's/Color(0xFF111827)/Color(0xFFE0E5EC)/g' $FILE

# Replace Text Colors
sed -i 's/color = Color.White/color = Color(0xFF333333)/g' $FILE
sed -i 's/color = Color.Gray/color = Color(0xFF7A8B99)/g' $FILE
sed -i 's/color = Color.LightGray/color = Color(0xFF7A8B99)/g' $FILE
