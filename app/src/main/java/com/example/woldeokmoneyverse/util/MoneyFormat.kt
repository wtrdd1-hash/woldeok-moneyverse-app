package com.example.woldeokmoneyverse.util

private val MONEY_PATTERN = Regex("^([+-]?)([0-9]+)(?:\\.([0-9]+))?$")

fun formatMoneyAmount(raw: String?): String {
    val source = raw?.replace(",", "")?.trim().orEmpty()
    val match = MONEY_PATTERN.matchEntire(source) ?: return raw?.trim().orEmpty().ifEmpty { "0" }
    val sign = when (match.groupValues[1]) {
        "-" -> "-"
        else -> ""
    }
    val integer = match.groupValues[2].trimStart('0').ifEmpty { "0" }
    val grouped = integer.reversed().chunked(3).joinToString(",").reversed()
    val fraction = match.groupValues[3].trimEnd('0')
    return buildString {
        append(sign)
        append(grouped)
        if (fraction.isNotEmpty()) {
            append('.')
            append(fraction)
        }
    }
}

fun formatMoneyAmount(value: Number): String = formatMoneyAmount(value.toString())

fun formatWld(raw: String?): String = "${formatMoneyAmount(raw)} WLD"
fun formatWld(value: Number): String = "${formatMoneyAmount(value)} WLD"

fun canonicalPositiveMoneyInput(value: String): String? {
    val raw = value.replace(",", "").trim()
    if (!Regex("^[0-9]+$").matches(raw)) return null
    val normalized = raw.trimStart('0').ifEmpty { "0" }
    return normalized.takeUnless { it == "0" }
}
