package com.example.woldeokmoneyverse.data.service

import java.util.concurrent.ConcurrentHashMap

enum class SupportedLanguage(val code: String, val label: String, val nativeName: String) {
    KO("ko", "한국어", "한국어"),
    EN("en", "English", "English"),
    JA("ja", "日本語", "日本語"),
    ZH("zh", "简体中文", "中文");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: KO
        }
    }
}

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLang: SupportedLanguage,
    val targetLang: SupportedLanguage,
    val isTranslated: Boolean
)

object TranslationService {

    private val cache = ConcurrentHashMap<String, String>()

    // 금융 및 핀테크 전문 용어 보호 패턴 (번역 시 왜곡 방지)
    private val financialProtectedTerms = listOf(
        "WLD", "EXP", "APR", "APY", "Net Worth", "Cash", "Bank", "BFF",
        "OAuth", "TOTP", "2FA", "CSRF", "UUID", "HALTED", "Safe Harbor",
        "10x", "5x", "Long", "Short", "SAFE", "VC", "M&A", "POW", "POS"
    )

    // 주요 핀테크·커뮤니티 구문 4대 언어 사전
    private val phraseCorpus: Map<String, Map<SupportedLanguage, String>> = mapOf(
        // 커뮤니티 & 소통
        "안녕하세요" to mapOf(
            SupportedLanguage.EN to "Hello",
            SupportedLanguage.JA to "こんにちは",
            SupportedLanguage.ZH to "你好"
        ),
        "반갑습니다" to mapOf(
            SupportedLanguage.EN to "Nice to meet you",
            SupportedLanguage.JA to "はじめまして",
            SupportedLanguage.ZH to "很高兴认识你"
        ),
        "오늘 주식 시장 상황 어떤가요?" to mapOf(
            SupportedLanguage.EN to "How is the stock market today?",
            SupportedLanguage.JA to "今日の株式市場の状況はどうですか？",
            SupportedLanguage.ZH to "今天的股市行情怎么样？"
        ),
        "WLD 송금 완료했습니다. 확인 부탁드립니다." to mapOf(
            SupportedLanguage.EN to "WLD transfer completed. Please check.",
            SupportedLanguage.JA to "WLD送金が完了しました。ご確認お願いします。",
            SupportedLanguage.ZH to "WLD转账已完成。请查收。"
        ),
        "저축 포켓에 입금했습니다." to mapOf(
            SupportedLanguage.EN to "Deposited into the Saving Pocket.",
            SupportedLanguage.JA to "貯蓄ポケットに入金しました。",
            SupportedLanguage.ZH to "已存入储蓄口袋。"
        ),
        "새로운 프로젝트에 투자하고 싶습니다." to mapOf(
            SupportedLanguage.EN to "I would like to invest in the new venture project.",
            SupportedLanguage.JA to "新しいベンチャープロジェクトに投資したいです。",
            SupportedLanguage.ZH to "我想投资新的创业项目。"
        ),
        "공지사항을 확인해 주세요." to mapOf(
            SupportedLanguage.EN to "Please check the official announcements.",
            SupportedLanguage.JA to "お知らせをご確認ください。",
            SupportedLanguage.ZH to "请查看官方公告。"
        ),
        "서버 정기 점검이 완료되었습니다." to mapOf(
            SupportedLanguage.EN to "Routine server maintenance has been completed.",
            SupportedLanguage.JA to "定期サーバーメンテナンスが完了しました。",
            SupportedLanguage.ZH to "服务器例行维护已完成。"
        ),

        // 영문 원문 대응
        "Hello" to mapOf(
            SupportedLanguage.KO to "안녕하세요",
            SupportedLanguage.JA to "こんにちは",
            SupportedLanguage.ZH to "你好"
        ),
        "Thank you" to mapOf(
            SupportedLanguage.KO to "감사합니다",
            SupportedLanguage.JA to "ありがとうございます",
            SupportedLanguage.ZH to "谢谢"
        ),
        "Great market update!" to mapOf(
            SupportedLanguage.KO to "멋진 시장 속보네요!",
            SupportedLanguage.JA to "素晴らしい市場情報ですね！",
            SupportedLanguage.ZH to "非常棒的市场行情快讯！"
        ),
        "Let's trade WLD together." to mapOf(
            SupportedLanguage.KO to "함께 WLD를 거래합시다.",
            SupportedLanguage.JA to "一緒にWLDを取引しましょう。",
            SupportedLanguage.ZH to "让我们一起交易 WLD 吧。"
        )
    )

    // 어휘 단위 사전 (Lexicon)
    private val wordLexicon: Map<String, Map<SupportedLanguage, String>> = mapOf(
        "주식" to mapOf(SupportedLanguage.EN to "Stocks", SupportedLanguage.JA to "株式", SupportedLanguage.ZH to "股票"),
        "시장" to mapOf(SupportedLanguage.EN to "Market", SupportedLanguage.JA to "市場", SupportedLanguage.ZH to "市场"),
        "사업" to mapOf(SupportedLanguage.EN to "Business", SupportedLanguage.JA to "事業", SupportedLanguage.ZH to "商业"),
        "투자" to mapOf(SupportedLanguage.EN to "Investment", SupportedLanguage.JA to "投資", SupportedLanguage.ZH to "投资"),
        "저축" to mapOf(SupportedLanguage.EN to "Savings", SupportedLanguage.JA to "貯蓄", SupportedLanguage.ZH to "储蓄"),
        "대출" to mapOf(SupportedLanguage.EN to "Loan", SupportedLanguage.JA to "ローン", SupportedLanguage.ZH to "贷款"),
        "국채" to mapOf(SupportedLanguage.EN to "Treasury Bonds", SupportedLanguage.JA to "国債", SupportedLanguage.ZH to "国债"),
        "직업" to mapOf(SupportedLanguage.EN to "Career", SupportedLanguage.JA to "職業", SupportedLanguage.ZH to "职业"),
        "과제" to mapOf(SupportedLanguage.EN to "Task", SupportedLanguage.JA to "課題", SupportedLanguage.ZH to "任务"),
        "보상" to mapOf(SupportedLanguage.EN to "Reward", SupportedLanguage.JA to "報酬", SupportedLanguage.ZH to "奖励"),
        "공지" to mapOf(SupportedLanguage.EN to "Notice", SupportedLanguage.JA to "お知らせ", SupportedLanguage.ZH to "公告"),
        "게시글" to mapOf(SupportedLanguage.EN to "Post", SupportedLanguage.JA to "投稿", SupportedLanguage.ZH to "帖子"),
        "댓글" to mapOf(SupportedLanguage.EN to "Comment", SupportedLanguage.JA to "コメント", SupportedLanguage.ZH to "评论"),
        "쪽지" to mapOf(SupportedLanguage.EN to "Message", SupportedLanguage.JA to "メッセージ", SupportedLanguage.ZH to "私信"),
        "계정" to mapOf(SupportedLanguage.EN to "Account", SupportedLanguage.JA to "アカウント", SupportedLanguage.ZH to "账户"),
        "보안" to mapOf(SupportedLanguage.EN to "Security", SupportedLanguage.JA to "セキュリティ", SupportedLanguage.ZH to "安全"),
        "완료" to mapOf(SupportedLanguage.EN to "Completed", SupportedLanguage.JA to "完了", SupportedLanguage.ZH to "完成"),
        "성공" to mapOf(SupportedLanguage.EN to "Success", SupportedLanguage.JA to "成功", SupportedLanguage.ZH to "成功"),
        "실패" to mapOf(SupportedLanguage.EN to "Failed", SupportedLanguage.JA to "失敗", SupportedLanguage.ZH to "失败"),
        "확인" to mapOf(SupportedLanguage.EN to "Confirm", SupportedLanguage.JA to "確認", SupportedLanguage.ZH to "确认"),
        "취소" to mapOf(SupportedLanguage.EN to "Cancel", SupportedLanguage.JA to "キャンセル", SupportedLanguage.ZH to "取消")
    )

    /**
     * 입력 텍스트의 언어를 추정 감지합니다.
     */
    fun detectLanguage(text: String): SupportedLanguage {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SupportedLanguage.KO

        var hangulCount = 0
        var kanaCount = 0
        var hanziCount = 0
        var latinCount = 0

        for (char in trimmed) {
            when {
                char in '\uAC00'..'\uD7AF' || char in '\u1100'..'\u11FF' -> hangulCount++
                char in '\u3040'..'\u309F' || char in '\u30A0'..'\u30FF' -> kanaCount++
                char in '\u4E00'..'\u9FFF' -> hanziCount++
                char in 'A'..'Z' || char in 'a'..'z' -> latinCount++
            }
        }

        return when {
            hangulCount > 0 -> SupportedLanguage.KO
            kanaCount > 0 -> SupportedLanguage.JA
            hanziCount > kanaCount && hanziCount > hangulCount -> SupportedLanguage.ZH
            latinCount > 0 -> SupportedLanguage.EN
            else -> SupportedLanguage.KO
        }
    }

    /**
     * 금융 특화 번역을 수행합니다.
     * 원문 언어와 대상 언어가 동일할 경우 원문을 그대로 반환합니다.
     */
    fun translate(text: String, targetLang: SupportedLanguage): TranslationResult {
        val sourceLang = detectLanguage(text)
        if (sourceLang == targetLang || text.isBlank()) {
            return TranslationResult(
                originalText = text,
                translatedText = text,
                sourceLang = sourceLang,
                targetLang = targetLang,
                isTranslated = false
            )
        }

        val cacheKey = "${sourceLang.code}->${targetLang.code}:$text"
        val cached = cache[cacheKey]
        if (cached != null) {
            return TranslationResult(
                originalText = text,
                translatedText = cached,
                sourceLang = sourceLang,
                targetLang = targetLang,
                isTranslated = true
            )
        }

        // 1. 정확 구문 매치 확인
        val phraseMatch = phraseCorpus[text.trim()]?.get(targetLang)
        if (phraseMatch != null) {
            cache[cacheKey] = phraseMatch
            return TranslationResult(
                originalText = text,
                translatedText = phraseMatch,
                sourceLang = sourceLang,
                targetLang = targetLang,
                isTranslated = true
            )
        }

        // 2. 금융 전문 용어 및 어휘 치환 기반 하이브리드 번역
        var translated = text

        // 금융 용어는 보호 (치환되지 않도록 토큰화 처리)
        val protectedTokens = mutableMapOf<String, String>()
        financialProtectedTerms.forEachIndexed { index, term ->
            if (translated.contains(term, ignoreCase = true)) {
                val token = "__FIN_TERM_${index}__"
                protectedTokens[token] = term
                translated = translated.replace(Regex("(?i)\\b$term\\b"), token)
            }
        }

        // 어휘 치환
        wordLexicon.forEach { (word, targetMap) ->
            val replacement = targetMap[targetLang]
            if (replacement != null && translated.contains(word)) {
                translated = translated.replace(word, replacement)
            }
        }

        // 관용적 접미사 및 문장 패턴 마무리
        translated = formatByTargetLanguage(translated, sourceLang, targetLang)

        // 보호된 금융 용어 복원
        protectedTokens.forEach { (token, term) ->
            translated = translated.replace(token, term)
        }

        cache[cacheKey] = translated
        return TranslationResult(
            originalText = text,
            translatedText = translated,
            sourceLang = sourceLang,
            targetLang = targetLang,
            isTranslated = true
        )
    }

    private fun formatByTargetLanguage(text: String, source: SupportedLanguage, target: SupportedLanguage): String {
        return when (target) {
            SupportedLanguage.EN -> {
                if (source == SupportedLanguage.KO && !text.endsWith(".")) {
                    text.replace("합니다", "ed").replace("입니다", " is")
                } else text
            }
            SupportedLanguage.JA -> {
                if (source == SupportedLanguage.KO) {
                    text.replace("합니다", "します").replace("입니다", "です")
                } else text
            }
            SupportedLanguage.ZH -> {
                if (source == SupportedLanguage.KO) {
                    text.replace("합니다", "了").replace("입니다", "是")
                } else text
            }
            SupportedLanguage.KO -> text
        }
    }
}
