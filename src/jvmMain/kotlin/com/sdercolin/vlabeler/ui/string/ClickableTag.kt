package com.sdercolin.vlabeler.ui.string

/**
 * โครงสร้างข้อมูลสำหรับจัดเก็บแท็ก (Tag) ที่สามารถโต้ตอบได้
 * โดยจะจับคู่ชื่อแท็กเข้ากับคำสั่งการทำงาน (Action) ที่จะเกิดขึ้นเมื่อผู้ใช้คลิก
 *
 * @property tag ชื่อของแท็กอ้างอิงที่ฝังอยู่ในข้อความ
 * @property onClick ฟังก์ชันคำสั่งที่จะถูกเรียกใช้งานเมื่อมีการคลิกที่แท็กนั้น
 */
class ClickableTag(
    val tag: String,
    val onClick: () -> Unit,
)

/**
 * โครงสร้างข้อมูลสำหรับจัดเก็บชุดข้อความที่ผ่านการประมวลผลและพร้อมแสดงผล
 *
 * @property text ข้อความบริสุทธิ์ที่ถูกถอดรูปแบบแท็กออกแล้ว
 * @property clickables รายการคู่ข้อมูล (Pair) ระหว่างส่วนของข้อความที่สามารถคลิกได้ กับคำสั่งการทำงานที่ผูกไว้
 */
class ClickableString(
    val text: String,
    val clickables: List<Pair<String, () -> Unit>>,
)

/**
 * ฟังก์ชันสำหรับสกัดแยกส่วนที่สามารถคลิกได้ (Clickable Tags) ออกจากชุดข้อความดิบ
 * ตัวอย่างเช่น หากกำหนดข้อความต้นทางเป็น "ข้อความ1@click1{ข้อความ2}ข้อความ3"
 * ผลลัพธ์ที่ได้คือข้อความที่ถูกกรองเหลือเพียง "ข้อความ1ข้อความ2ข้อความ3"
 * พร้อมกับดึง "ข้อความ2" ไปผูกกับฟังก์ชัน onClick1 อย่างเป็นระบบ
 *
 * @param text ชุดข้อความดิบที่มีการฝังแท็กไว้ภายใน
 * @param tags รายการแท็กและคำสั่งทั้งหมดที่ระบบเปิดให้ใช้งานได้
 * @return ออบเจกต์ ClickableString ที่พร้อมสำหรับการนำไปแสดงผลบนหน้าจอผู้ใช้งาน (UI)
 */
fun extractClickables(text: String, tags: List<ClickableTag>): ClickableString {
    // กำหนดรูปแบบนิพจน์ปกติ (Regular Expression) เพื่อค้นหาข้อความที่ตรงตามเงื่อนไข 
    // ตัวอย่างเช่น @tagName{Inner Text}
    val pattern = Regex("""@(\w+)\{([^}]+)}""")
    val result = pattern.findAll(text)
    
    // ดำเนินการแทนที่รูปแบบแท็กทั้งหมดด้วยข้อความที่อยู่ด้านในปีกกา 
    // เพื่อนำไปประกอบเป็นข้อความธรรมดาที่สามารถอ่านได้ต่อเนื่อง
    val restoredText = result.fold(text) { acc, matchResult ->
        val tag = matchResult.groupValues[1] // ดึงชื่อแท็ก (tagName)
        val clickable = tags.find { it.tag == tag }
        
        if (clickable != null) {
            // หากพบแท็กที่ตรงกัน ให้แทนที่โค้ดทั้งหมดด้วยข้อความด้านใน
            acc.replace(matchResult.value, matchResult.groupValues[2])
        } else {
            // หากไม่พบแท็กที่ตรงกัน ให้คงข้อความเดิมไว้
            acc
        }
    }
    
    // ดำเนินการสร้างและส่งคืนออบเจกต์ ClickableString
    return ClickableString(
        text = restoredText,
        clickables = result.mapNotNull { matchResult ->
            val tag = matchResult.groupValues[1]
            val clickable = tags.find { it.tag == tag }
            
            if (clickable != null) {
                // ผูกข้อความด้านในเข้ากับฟังก์ชัน onClick ที่เตรียมไว้
                matchResult.groupValues[2] to clickable.onClick
            } else {
                null // เพิกเฉยต่อแท็กที่ระบบไม่รู้จัก
            }
        }.toList(),
    )
}
