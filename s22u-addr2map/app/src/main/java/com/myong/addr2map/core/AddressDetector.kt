package com.myong.addr2map.core

object AddressDetector {

    private const val MIN_LEN = 5
    private const val MAX_LEN = 120

    private val urlRegex = Regex("""(?i)(https?://|www\.|\b\S+\.(com|net|org|kr|io)\b)""")
    private val emailRegex = Regex("""\S+@\S+\.\S+""")
    private val phoneRegex = Regex("""^\+?\d[\d\s\-]{7,}$""")
    private val digitRegex = Regex("""\d""")
    private val addrTokenRegex =
        Regex("""(특별시|광역시|특별자치도|특별자치시|[가-힣]+시|[가-힣]+도|[가-힣]+군|[가-힣]+구|[가-힣]+읍|[가-힣]+면|[가-힣]+동|[가-힣]+리|[가-힣]+로|[가-힣]+길|번지|아파트|빌딩|타워|[0-9]+가)""")

    fun isLikelyAddress(raw: String?): Boolean {
        val s = raw?.trim().orEmpty()
        if (s.length < MIN_LEN || s.length > MAX_LEN) return false
        if (urlRegex.containsMatchIn(s)) return false
        if (emailRegex.containsMatchIn(s)) return false
        if (phoneRegex.matches(s)) return false

        val tokenCount = addrTokenRegex.findAll(s).count()
        if (tokenCount == 0) return false

        val hasDigit = digitRegex.containsMatchIn(s)
        return hasDigit || tokenCount >= 2
    }
}
