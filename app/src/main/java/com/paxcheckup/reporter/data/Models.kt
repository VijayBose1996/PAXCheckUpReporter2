package com.paxcheckup.reporter.data

import java.util.Date
import java.util.UUID

enum class TestStatus {
    NOT_TESTED, PASS, FAIL, SKIPPED, PARTIAL
}

data class TestItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: TestCategory,
    val status: TestStatus = TestStatus.NOT_TESTED,
    val notes: String = "",
    val timestamp: Date? = null,
    val requiresManualInput: Boolean = false
)

enum class TestCategory(val displayName: String, val iconName: String) {
    DISPLAY("Display & Touch", "screen"),
    KEYPAD("Keypad & Buttons", "keyboard"),
    CARD_READER("Card Reader", "credit_card"),
    PRINTER("Printer", "print"),
    SCANNER("Scanner", "qr_code_scanner"),
    CAMERA("Camera", "photo_camera"),
    COMMUNICATION("Communication", "network_wifi"),
    BATTERY("Battery", "battery_full"),
    LED_SOUND("LED & Sound", "volume_up"),
    DEVICE_INFO("Device Info", "info"),
    ONE_KEY_DETECT("One-Key Detect", "auto_fix")
}

data class TestSession(
    val id: String = UUID.randomUUID().toString(),
    val deviceModel: String = "",
    val serialNumber: String = "",
    val technicianName: String = "",
    val location: String = "",
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val testItems: List<TestItem> = emptyList(),
    val overallStatus: TestStatus = TestStatus.NOT_TESTED
) {
    val passCount: Int get() = testItems.count { it.status == TestStatus.PASS }
    val failCount: Int get() = testItems.count { it.status == TestStatus.FAIL }
    val skipCount: Int get() = testItems.count { it.status == TestStatus.SKIPPED }
    val notTestedCount: Int get() = testItems.count { it.status == TestStatus.NOT_TESTED }
    val totalCount: Int get() = testItems.size
}

object TestDefinitions {
    fun getAllTests(): List<TestItem> = listOf(
        // DISPLAY
        TestItem(name = "LCD Display", description = "Check LCD display for dead pixels, color accuracy, and brightness", category = TestCategory.DISPLAY),
        TestItem(name = "Touch Screen", description = "Test single touch responsiveness and accuracy", category = TestCategory.DISPLAY),
        TestItem(name = "Multi-Touch", description = "Test multi-touch gestures (pinch, zoom)", category = TestCategory.DISPLAY),
        TestItem(name = "Screen Colors", description = "Verify red, green, blue, white, black display correctly", category = TestCategory.DISPLAY),

        // KEYPAD
        TestItem(name = "Power Button", description = "Test power button press and release", category = TestCategory.KEYPAD),
        TestItem(name = "Volume Up", description = "Test volume up button functionality", category = TestCategory.KEYPAD),
        TestItem(name = "Volume Down", description = "Test volume down button functionality", category = TestCategory.KEYPAD),
        TestItem(name = "Numeric Keys", description = "Test all numeric keypad buttons (0-9)", category = TestCategory.KEYPAD),
        TestItem(name = "Function Keys", description = "Test function keys (F1, F2, F3, etc.)", category = TestCategory.KEYPAD),
        TestItem(name = "Enter/Cancel", description = "Test enter and cancel buttons", category = TestCategory.KEYPAD),
        TestItem(name = "Back Button", description = "Test back button functionality", category = TestCategory.KEYPAD),

        // CARD READER
        TestItem(name = "Magstripe Reader", description = "Swipe magstripe card and verify read", category = TestCategory.CARD_READER),
        TestItem(name = "EMV Chip Insert", description = "Insert EMV chip card and verify read", category = TestCategory.CARD_READER),
        TestItem(name = "EMV Chip Contactless", description = "Tap contactless card and verify read", category = TestCategory.CARD_READER),
        TestItem(name = "ICC Card", description = "Test ICC card slot", category = TestCategory.CARD_READER),
        TestItem(name = "PICC Card", description = "Test PICC contactless reader", category = TestCategory.CARD_READER),

        // PRINTER
        TestItem(name = "Printer Paper Feed", description = "Check paper feed mechanism", category = TestCategory.PRINTER),
        TestItem(name = "Printer Print Quality", description = "Print test receipt and check quality", category = TestCategory.PRINTER),
        TestItem(name = "Printer Cut", description = "Test automatic paper cutter", category = TestCategory.PRINTER),
        TestItem(name = "Bluetooth Printer", description = "Test external Bluetooth printer connection", category = TestCategory.PRINTER),

        // SCANNER
        TestItem(name = "Barcode Scan", description = "Scan a 1D barcode and verify result", category = TestCategory.SCANNER),
        TestItem(name = "QR Code Scan", description = "Scan a QR code and verify result", category = TestCategory.SCANNER),
        TestItem(name = "Scan Trigger", description = "Test scan trigger button", category = TestCategory.SCANNER),

        // CAMERA
        TestItem(name = "Front Camera", description = "Test front camera capture and image quality", category = TestCategory.CAMERA),
        TestItem(name = "Rear Camera", description = "Test rear camera capture and image quality", category = TestCategory.CAMERA),
        TestItem(name = "Camera Flash", description = "Test camera flash/LED", category = TestCategory.CAMERA),
        TestItem(name = "Camera Auto Focus", description = "Test auto focus functionality", category = TestCategory.CAMERA),

        // COMMUNICATION
        TestItem(name = "Wi-Fi", description = "Test Wi-Fi connection and signal strength", category = TestCategory.COMMUNICATION),
        TestItem(name = "Mobile Data (4G/5G)", description = "Test cellular data connection", category = TestCategory.COMMUNICATION),
        TestItem(name = "Bluetooth", description = "Test Bluetooth pairing and connection", category = TestCategory.COMMUNICATION),
        TestItem(name = "GPS", description = "Test GPS location fix", category = TestCategory.COMMUNICATION),
        TestItem(name = "Ethernet (if available)", description = "Test wired Ethernet connection", category = TestCategory.COMMUNICATION),
        TestItem(name = "Network Stability", description = "Test network stability over time", category = TestCategory.COMMUNICATION),

        // BATTERY
        TestItem(name = "Battery Level", description = "Check current battery percentage", category = TestCategory.BATTERY),
        TestItem(name = "Battery Health", description = "Check battery health status", category = TestCategory.BATTERY),
        TestItem(name = "Charging", description = "Test charging when connected to power", category = TestCategory.BATTERY),
        TestItem(name = "Battery Discharge", description = "Monitor battery discharge rate", category = TestCategory.BATTERY),

        // LED & SOUND
        TestItem(name = "Speaker", description = "Test speaker audio output", category = TestCategory.LED_SOUND),
        TestItem(name = "Buzzer/Beep", description = "Test buzzer/beep sound", category = TestCategory.LED_SOUND),
        TestItem(name = "LED Indicators", description = "Test all LED indicators (charging, status, etc.)", category = TestCategory.LED_SOUND),
        TestItem(name = "Vibration", description = "Test vibration motor", category = TestCategory.LED_SOUND),

        // DEVICE INFO
        TestItem(name = "Device Model", description = "Verify device model information", category = TestCategory.DEVICE_INFO),
        TestItem(name = "Serial Number", description = "Verify serial number", category = TestCategory.DEVICE_INFO),
        TestItem(name = "OS Version", description = "Check Android OS version", category = TestCategory.DEVICE_INFO),
        TestItem(name = "App Version", description = "Check PAX CheckUp app version", category = TestCategory.DEVICE_INFO),
        TestItem(name = "Storage Available", description = "Check available internal storage", category = TestCategory.DEVICE_INFO),
        TestItem(name = "SD Card", description = "Test SD card slot and read/write", category = TestCategory.DEVICE_INFO),

        // ONE-KEY DETECT
        TestItem(name = "One-Key Full Detect", description = "Run full automatic hardware detection", category = TestCategory.ONE_KEY_DETECT)
    )
}
