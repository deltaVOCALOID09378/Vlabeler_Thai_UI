package strings

import com.sdercolin.vlabeler.exception.LocalizedStringDeserializedException
import com.sdercolin.vlabeler.ui.string.Language
import com.sdercolin.vlabeler.ui.string.LocalizedJsonString
import com.sdercolin.vlabeler.util.parseJson
import com.sdercolin.vlabeler.util.stringifyJson
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ชุดการทดสอบสำหรับการทำงานของคลาส [LocalizedJsonString]
 */
class LocalizedJsonStringTest {

    @Test
    fun testPlainString() {
        // กำหนดข้อความธรรมดาในรูปแบบสตริงของ JSON
        val plainString = "\"Hello World\""
        // แปลงข้อความดังกล่าวให้เป็นออบเจกต์ LocalizedJsonString
        val parsed = plainString.parseJson<LocalizedJsonString>()

        // ตรวจสอบว่าผลลัพธ์ตรงกับที่คาดหวังไว้ โดยระบบจะต้องใช้รหัสภาษาเริ่มต้น (Default Language)
        val expected = LocalizedJsonString(mapOf(Language.default.code to plainString.trim('"')))
        assertEquals(expected, parsed)
        // ตรวจสอบความถูกต้องของการแปลงออบเจกต์กลับเป็นโครงสร้าง JSON
        assertEquals(plainString, parsed.stringifyJson())
    }

    @Test
    fun testLocalized() {
        // กำหนดโครงสร้าง JSON ที่มีการระบุข้อความในหลายภาษา (เพิ่มภาษาไทยเข้าไป)
        val localizedString = """
            {
                "en": "Hello World",
                "zh": "你好，世界",
                "th": "สวัสดีชาวโลก"
            }
        """.trimIndent()
        // แปลงข้อมูล JSON ให้เป็นออบเจกต์ LocalizedJsonString
        val parsed = localizedString.parseJson<LocalizedJsonString>()

        // กำหนดรูปแบบผลลัพธ์ที่ถูกต้องเพื่อใช้ในการเปรียบเทียบ
        val expected = LocalizedJsonString(
            mapOf(
                "en" to "Hello World",
                "zh" to "你好，世界",
                "th" to "สวัสดีชาวโลก",
            ),
        )
        // ตรวจสอบความถูกต้องของออบเจกต์ที่ถูกแปลง
        assertEquals(expected, parsed)
        // ตรวจสอบความถูกต้องของการแปลงออบเจกต์กลับเป็นโครงสร้าง JSON
        assertEquals(localizedString, parsed.stringifyJson())
        // ตรวจสอบการทำงานของการดึงค่าข้อความภาษาไทย
        assertEquals("สวัสดีชาวโลก", parsed.getCertain(Language.Thai)) // สมมติว่ามีการกำหนด Language.Thai ไว้ในระบบ
    }

    @Test
    fun testLocalizedStringMissingDefault() {
        // กำหนดโครงสร้าง JSON ที่ไม่มีการระบุภาษาเริ่มต้น (Default Language) ไว้ภายใน
        val localizedStringMissingDefault = """
            {
                "zh": "你好，世界",
                "ja": "こんにちは世界",
                "ko": "안녕 세상",
                "th": "สวัสดีชาวโลก"
            }
        """.trimIndent()

        // ตรวจสอบว่าระบบได้ทำการแสดงข้อผิดพลาด (Exception) ออกมาอย่างถูกต้องหรือไม่
        assertThrows<LocalizedStringDeserializedException> {
            localizedStringMissingDefault.parseJson<LocalizedJsonString>()
        }
    }
}
