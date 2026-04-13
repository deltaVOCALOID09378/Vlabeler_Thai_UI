@file:OptIn(ExperimentalSerializationApi::class)

package com.sdercolin.vlabeler.ui.string

import androidx.compose.runtime.Composable
import com.sdercolin.vlabeler.exception.LocalizedStringDeserializedException
import com.sdercolin.vlabeler.util.stringifyJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * ชุดข้อความรองรับหลายภาษาที่ถูกจัดเก็บในรูปแบบ JSON สำหรับใช้งานร่วมกับเลเบลเลอร์ (Labelers) และปลั๊กอิน (Plugins)
 * โดยระบบรองรับการรับค่าข้อมูล 2 รูปแบบ ดังต่อไปนี้:
 * 1. ข้อมูลรูปแบบข้อความเดี่ยว (String) ซึ่งระบบจะนำไปผูกเข้ากับภาษาเริ่มต้น [Language.default] โดยอัตโนมัติ
 * 2. ข้อมูลรูปแบบชุดพจนานุกรม (Map) ที่ใช้จับคู่รหัสภาษากับข้อความ (ตัวอย่างเช่น {"en": "Hello", "th": "สวัสดี"})
 * ระบบจะดึงข้อความมาใช้งานเมื่อรหัสภาษาที่กำลังเปิดใช้ ตรงกับรหัสที่ขึ้นต้นในระบบ (ตัวอย่างเช่น หากระบบปัจจุบันใช้รหัส "en-US" 
 * ระบบจะดึงข้อความจากคีย์ "en" มาใช้งาน) 
 * ข้อควรระวัง: ระบบจะไม่อนุญาตให้ใช้งานชุดข้อมูลรูปแบบ Map ที่ไม่มีการกำหนดค่าสำหรับภาษาเริ่มต้น [Language.default] ไว้ภายใน
 */
@Serializable(with = LocalizedJsonStringSerializer::class)
data class LocalizedJsonString(
    val localized: Map<String, String>,
) {
    /**
     * ดำเนินการค้นหาและคืนค่าข้อความ โดยอ้างอิงจากรหัสภาษาที่ถูกระบุ
     */
    private fun getByLanguage(language: Language): String? {
        val requiredCode = language.code
        localized.forEach { (code, value) ->
            if (requiredCode.startsWith(code)) {
                return value
            }
        }
        return null
    }

    /**
     * ดึงค่าข้อความตามภาษาที่เจาะจง หากไม่พบข้อมูลของภาษานั้น ระบบจะบังคับใช้ข้อความของภาษาเริ่มต้นแทนเสมอ
     */
    fun getCertain(language: Language) = getByLanguage(language) ?: requireNotNull(getByLanguage(Language.default))

    /**
     * ฟังก์ชันคอมโพส (Composable) สำหรับดึงข้อความมาแสดงผลบนหน้าจอ โดยอ้างอิงจากภาษาที่กำลังเปิดใช้งานอยู่ ณ ปัจจุบัน
     */
    @Composable
    fun get() = getCertain(LocalLanguage.current)

    /**
     * ตรวจสอบความถูกต้องของโครงสร้างข้อมูล เพื่อป้องกันข้อผิดพลาดในการประมวลผล
     */
    fun validate(): LocalizedJsonString {
        if (localized.isEmpty()) {
            throw LocalizedStringDeserializedException("ชุดข้อมูลข้อความว่างเปล่า (Empty localized string)")
        }
        getByLanguage(Language.default)
            ?: throw LocalizedStringDeserializedException(
                "ไม่พบข้อความสำหรับภาษาเริ่มต้น ${Language.default.code} " +
                    "ภายในข้อมูล JSON: ${localized.stringifyJson()}",
            )
        return this
    }
}

/**
 * ฟังก์ชันส่วนขยาย (Extension Function) สำหรับแปลงข้อความธรรมดา ให้กลายเป็นออบเจกต์ LocalizedJsonString ในรูปแบบภาษาเริ่มต้น
 */
fun String.toLocalized() = LocalizedJsonString(mapOf(Language.default.code to this))

/**
 * คลาสสำหรับจัดการกระบวนการแปลงข้อมูล (Serialization/Deserialization) ของ LocalizedJsonString
 */
@Serializer(LocalizedJsonString::class)
object LocalizedJsonStringSerializer : KSerializer<LocalizedJsonString> {

    /**
     * กระบวนการแปลงออบเจกต์ LocalizedJsonString ให้กลับไปเป็นโครงสร้าง JSON
     */
    override fun serialize(encoder: Encoder, value: LocalizedJsonString) {
        if (value.localized.size == 1) {
            // กรณีที่มีเพียงภาษาเดียว (ภาษาเริ่มต้น) ให้เข้ารหัสเป็นข้อความ (String) ปกติ
            encoder.encodeString(value.localized.values.first())
        } else {
            // กรณีที่มีหลายภาษา ให้เข้ารหัสเป็นโครงสร้างออบเจกต์ (JSON Object)
            val map = value.localized.mapValues { (_, v) -> JsonPrimitive(v) }
            encoder.encodeSerializableValue(JsonObject.serializer(), JsonObject(map))
        }
    }

    /**
     * กระบวนการอ่านโครงสร้าง JSON เพื่อสร้างเป็นออบเจกต์ LocalizedJsonString
     */
    override fun deserialize(decoder: Decoder): LocalizedJsonString {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        
        // ลองทำการอ่านในรูปแบบข้อความเดี่ยว (String) ก่อน
        runCatching { element.jsonPrimitive.content }
            .getOrNull()
            ?.let { return LocalizedJsonString(mapOf(Language.default.code to it)) }
            
        // หากไม่ใช่ข้อความเดี่ยว ให้ทำการอ่านในรูปแบบชุดพจนานุกรม (Map)
        return element.jsonObject.toMap()
            .mapValues { it.value.jsonPrimitive.content }
            .let { LocalizedJsonString(it) }
            .validate() // ดำเนินการตรวจสอบความถูกต้องของข้อมูลทันทีที่อ่านเสร็จ
    }
}
