package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ฟังก์ชันคอมโพส (Composable) สำหรับสร้างเมนูตัวเลือกภาษา
 * รองรับการแสดงผลหน้าต่างแบบเลื่อนลง (Dropdown) เพื่อให้ผู้ใช้งานสามารถเลือกภาษาไทยหรือภาษาอื่น ๆ ได้
 *
 * @param currentLanguage ชื่อภาษาปัจจุบันที่ระบบกำลังใช้งานอยู่
 * @param onLanguageSelected คำสั่งที่จะถูกเรียกใช้งานเมื่อผู้ใช้ทำการคลิกเลือกภาษาใหม่
 */
@Composable
fun LanguageSelectionMenu(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    // ตัวแปรสำหรับควบคุมสถานะการเปิดและปิดของเมนูตัวเลือก
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(16.dp)) {
        // ปุ่มกดหลักสำหรับแสดงผลภาษาปัจจุบันและใช้เพื่อเปิดเมนู
        Button(onClick = { isMenuExpanded = true }) {
            Text(text = "ภาษาปัจจุบัน: $currentLanguage")
        }
        
        // ส่วนของเมนูแบบเลื่อนลงที่จะแสดงรายการภาษาทั้งหมด
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            // ตัวเลือกสำหรับภาษาอังกฤษ
            DropdownMenuItem(onClick = {
                onLanguageSelected("English")
                isMenuExpanded = false
            }) {
                Text("English")
            }
            
            // ตัวเลือกสำหรับภาษาไทย
            DropdownMenuItem(onClick = {
                onLanguageSelected("ภาษาไทย")
                isMenuExpanded = false
            }) {
                Text("ภาษาไทย")
            }
        }
    }
}
