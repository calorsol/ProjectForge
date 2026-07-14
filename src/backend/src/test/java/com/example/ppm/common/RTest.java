package com.example.ppm.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RTest {
    @Test
    void okWrapsDataWithSuccessCode() {
        R<String> result = R.ok("task");

        assertEquals(0, result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("task", result.getData());
    }

    @Test
    void failKeepsErrorCodeAndHasNoData() {
        R<Void> result = R.fail(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }
}
