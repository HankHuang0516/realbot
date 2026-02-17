package com.hank.clawlive

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for message request format
 * Verifies that API request bodies are correctly formatted
 */
class MessageRequestFormatTest {

    private val gson = Gson()

    @Test
    fun testSingleEntityMessageRequest() {
        // Simulate what MainActivity.sendMessageToEntity creates
        val request = mapOf<String, Any>(
            "deviceId" to "test-device-id",
            "entityId" to 0,  // Should be Int, not String
            "text" to "Hello",
            "source" to "android_main"
        )

        val json = gson.toJson(request)

        // Verify entityId is serialized as number, not string
        assertTrue("entityId should be a number in JSON", json.contains("\"entityId\":0"))
        assertFalse("entityId should NOT be a string", json.contains("\"entityId\":\"0\""))

        println("Single entity request: $json")
    }

    @Test
    fun testBroadcastMessageRequest() {
        // Simulate what MainActivity.broadcastMessage creates
        val entityIds = listOf(0, 1, 2)
        val request = mapOf<String, Any>(
            "deviceId" to "test-device-id",
            "entityId" to entityIds,  // Should be List<Int>
            "text" to "Hello everyone",
            "source" to "android_broadcast"
        )

        val json = gson.toJson(request)

        // Verify entityId is serialized as array
        assertTrue("entityId should be an array", json.contains("\"entityId\":[0,1,2]"))

        println("Broadcast request: $json")
    }

    @Test
    fun testWidgetMessageRequest() {
        // Simulate what MessageActivity.sendMessageToSelectedEntities creates
        val selectedIds = listOf(0)
        val entityIdValue: Any = if (selectedIds.size == 1) {
            selectedIds.first()
        } else {
            selectedIds
        }

        val request = mapOf<String, Any>(
            "deviceId" to "test-device-id",
            "entityId" to entityIdValue,
            "text" to "Widget message",
            "source" to "android_widget"
        )

        val json = gson.toJson(request)

        // For single entity, entityId should be a number
        assertTrue("entityId should be a number for single entity", json.contains("\"entityId\":0"))

        println("Widget request (single): $json")
    }

    @Test
    fun testWidgetMultiEntityRequest() {
        // Simulate widget broadcast to multiple entities
        val selectedIds = listOf(0, 2)
        val entityIdValue: Any = if (selectedIds.size == 1) {
            selectedIds.first()
        } else {
            selectedIds
        }

        val request = mapOf<String, Any>(
            "deviceId" to "test-device-id",
            "entityId" to entityIdValue,
            "text" to "Widget broadcast",
            "source" to "android_widget"
        )

        val json = gson.toJson(request)

        // For multiple entities, entityId should be an array
        assertTrue("entityId should be an array for multiple entities", json.contains("\"entityId\":[0,2]"))

        println("Widget request (multi): $json")
    }

    @Test
    fun testRequestFieldsArePresent() {
        val request = mapOf<String, Any>(
            "deviceId" to "abc-123",
            "entityId" to 1,
            "text" to "Test message",
            "source" to "test"
        )

        val json = gson.toJson(request)

        assertTrue("Should contain deviceId", json.contains("deviceId"))
        assertTrue("Should contain entityId", json.contains("entityId"))
        assertTrue("Should contain text", json.contains("text"))
        assertTrue("Should contain source", json.contains("source"))
    }
}
