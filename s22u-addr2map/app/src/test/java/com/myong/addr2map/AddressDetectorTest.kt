package com.myong.addr2map

import com.myong.addr2map.core.AddressDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressDetectorTest {

    @Test
    fun positives() {
        assertTrue(AddressDetector.isLikelyAddress("서울특별시 강남구 테헤란로 123"))
        assertTrue(AddressDetector.isLikelyAddress("대구 달서구 월배로12길 34"))
        assertTrue(AddressDetector.isLikelyAddress("경기도 성남시 분당구 정자동 178-1"))
        assertTrue(AddressDetector.isLikelyAddress("부산광역시 해운대구 우동 1408"))
    }

    @Test
    fun negatives() {
        assertFalse(AddressDetector.isLikelyAddress(null))
        assertFalse(AddressDetector.isLikelyAddress(""))
        assertFalse(AddressDetector.isLikelyAddress("집"))
        assertFalse(AddressDetector.isLikelyAddress("https://map.kakao.com/?q=x"))
        assertFalse(AddressDetector.isLikelyAddress("hello world this is a test"))
        assertFalse(AddressDetector.isLikelyAddress("abc@test.com"))
        assertFalse(AddressDetector.isLikelyAddress("010-1234-5678"))
        assertFalse(AddressDetector.isLikelyAddress("12345"))
    }
}
