package com.example.woldeokmoneyverse.data.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationServiceTest {

    @Test
    fun testDetectLanguage() {
        assertEquals(SupportedLanguage.KO, TranslationService.detectLanguage("안녕하세요, 반갑습니다!"))
        assertEquals(SupportedLanguage.EN, TranslationService.detectLanguage("Hello, welcome to Moneyverse!"))
        assertEquals(SupportedLanguage.JA, TranslationService.detectLanguage("こんにちは、よろしくお願いいたします。"))
        assertEquals(SupportedLanguage.ZH, TranslationService.detectLanguage("你好，很高兴认识你。"))
    }

    @Test
    fun testTranslateExactPhrases() {
        val resultEn = TranslationService.translate("안녕하세요", SupportedLanguage.EN)
        assertTrue(resultEn.isTranslated)
        assertEquals("Hello", resultEn.translatedText)

        val resultJa = TranslationService.translate("안녕하세요", SupportedLanguage.JA)
        assertTrue(resultJa.isTranslated)
        assertEquals("こんにちは", resultJa.translatedText)

        val resultZh = TranslationService.translate("안녕하세요", SupportedLanguage.ZH)
        assertTrue(resultZh.isTranslated)
        assertEquals("你好", resultZh.translatedText)
    }

    @Test
    fun testFinancialTermsPreserved() {
        val input = "WLD 송금 완료했습니다. 확인 부탁드립니다."
        val result = TranslationService.translate(input, SupportedLanguage.EN)
        assertTrue(result.isTranslated)
        // 금융 고유 심볼 WLD가 온전히 보존되어야 함
        assertTrue(result.translatedText.contains("WLD"))
    }

    @Test
    fun testSameLanguageReturnsOriginal() {
        val input = "안녕하세요"
        val result = TranslationService.translate(input, SupportedLanguage.KO)
        assertFalse(result.isTranslated)
        assertEquals(input, result.translatedText)
    }
}
