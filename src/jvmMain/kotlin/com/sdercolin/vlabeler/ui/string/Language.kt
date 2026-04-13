package com.sdercolin.vlabeler.ui.string

import androidx.compose.runtime.compositionLocalOf

// ตัวแปรสำหรับจัดเก็บสถานะภาษาปัจจุบันที่ระบบกำลังใช้งาน (กำหนดค่าเริ่มต้นเป็นภาษาเริ่มต้นของระบบ)
var currentLanguage: Language = Language.default

// สร้าง CompositionLocal สำหรับการจัดการภาษา เพื่อให้สามารถเข้าถึงและอัปเดตภาษาบนหน้าจอ (UI) ได้อย่างราบรื่น
val LocalLanguage = compositionLocalOf { Language.default }

/**
 * โครงสร้างข้อมูล (Enum) สำหรับการระบุภาษาทั้งหมดที่ระบบรองรับ
 *
 * @property code รหัสมาตรฐานของภาษา (Language Code)
 * @property displayName ชื่อภาษาที่ใช้สำหรับแสดงผลบนหน้าจอ
 */
enum class Language(val code: String, private val displayName: String) : Text {
    English("en", "English"),
    ChineseSimplified("zh-Hans", "简体中文"),
    Japanese("ja", "日本語"),
    Korean("ko", "한국어"),
    Thai("th", "ภาษาไทย"), // เพิ่มข้อมูลการรองรับภาษาไทย
    ;

    // การกำหนดรูปแบบการแสดงผลข้อความ เมื่อมีการเรียกใช้ตัวแปรภาษา
    override val text: String
        get() = displayName

    companion object {

        // กำหนดให้ภาษาอังกฤษ (English) เป็นภาษาเริ่มต้นของระบบ
        val default = English

        /**
         * ฟังก์ชันสำหรับค้นหาและจับคู่รหัสภาษา
         * โดยจะตรวจสอบจากรหัสภาษาที่ถูกส่งเข้ามาเปรียบเทียบกับรหัสภาษาที่ระบบรองรับ
         *
         * @param languageTag รหัสภาษาที่ต้องการค้นหา (เช่น "en-US", "th", "ja-JP")
         * @return ออบเจกต์ Language ที่ตรงกับรหัส หากไม่พบจะคืนค่าเป็น null
         */
        fun find(languageTag: String): Language? {
            // ดำเนินการวนซ้ำเพื่อตรวจสอบในทุกภาษาที่ระบบมี
            for (value in entries) {
                // แยกส่วนประกอบของรหัสภาษา (เช่น "zh-Hans" แยกเป็น "zh" และ "zh-Hans")
                val codeLevels = value.code.split("-").scan("") { acc, s ->
                    if (acc.isEmpty()) s else "$acc-$s"
                }.filter { it.isNotEmpty() }
                
                // ดำเนินการเปรียบเทียบรหัสภาษา โดยเริ่มจากรหัสเต็มก่อน เพื่อความแม่นยำสูงสุด
                for (code in codeLevels.reversed()) {
                    if (languageTag.startsWith(code)) {
                        return value // หากรหัสตรงกัน ให้ส่งคืนค่าภาษานั้นกลับไป
                    }
                }
            }
            return null // กรณีที่ไม่พบภาษาที่ตรงกันเลย
        }
    }
}
