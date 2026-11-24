# Biome Compass (Fabric, Minecraft Java 26.3)

กดปุ่ม `B` -> เลือกไบโอม (รวมไบโอมจากม็อดอื่น, มีช่องค้นหา) -> เซิร์ฟเวอร์หาไบโอมที่ใกล้ที่สุด
(ตัวเดียวกับ `/locate biome`) -> แสดงลูกศรทิศทางและระยะทางบน Action Bar

## Build บน GitHub
1. สร้าง repo แล้วอัปโหลดไฟล์ทั้งหมดในโฟลเดอร์นี้ (รวมโฟลเดอร์ `.github`)
2. ไปที่แท็บ **Actions** -> workflow `build` จะรันเอง (หรือกด Run workflow)
3. เมื่อเสร็จ โหลดไฟล์ jar ได้ที่ **Artifacts** -> `biome-compass`

## ติดตั้ง
Minecraft 26.3 + Fabric Loader 0.19.5+ + Fabric API 0.161.0+26.3 + Java 25
วางไฟล์ jar ในโฟลเดอร์ `mods` (บนเซิร์ฟเวอร์ต้องลงฝั่ง server ด้วย)
