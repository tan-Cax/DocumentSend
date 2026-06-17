package com.example.documentsend.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class StorageUtilsTest {

    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "storage_test_${System.nanoTime()}")
        tempDir.mkdirs()
    }

    @After
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    // ==================== formatFileSize ====================

    @Test
    fun `formatFileSize should format bytes correctly`() {
        assertEquals("0 B", StorageUtils.formatFileSize(0))
        assertEquals("512 B", StorageUtils.formatFileSize(512))
        assertEquals("1023 B", StorageUtils.formatFileSize(1023))
    }

    @Test
    fun `formatFileSize should format KB correctly`() {
        assertEquals("1.0 KB", StorageUtils.formatFileSize(1024))
        assertEquals("1.5 KB", StorageUtils.formatFileSize(1536))
        assertEquals("10.0 KB", StorageUtils.formatFileSize(10240))
    }

    @Test
    fun `formatFileSize should format MB correctly`() {
        assertEquals("1.0 MB", StorageUtils.formatFileSize(1024L * 1024))
        assertEquals("5.5 MB", StorageUtils.formatFileSize((5.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatFileSize should format GB correctly`() {
        assertEquals("1.00 GB", StorageUtils.formatFileSize(1024L * 1024 * 1024))
        assertEquals("2.50 GB", StorageUtils.formatFileSize((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    // ==================== getUniqueFile ====================

    @Test
    fun `getUniqueFile should return same file if not exists`() {
        val result = StorageUtils.getUniqueFile(tempDir, "newfile.txt")

        assertEquals("newfile.txt", result.name)
        assertEquals(tempDir, result.parentFile)
    }

    @Test
    fun `getUniqueFile should add timestamp if file exists`() {
        File(tempDir, "existing.txt").createNewFile()

        val result = StorageUtils.getUniqueFile(tempDir, "existing.txt")

        assertNotEquals("existing.txt", result.name)
        assertTrue(result.name.startsWith("existing_"))
        assertTrue(result.name.endsWith(".txt"))
    }

    @Test
    fun `getUniqueFile should handle file without extension`() {
        File(tempDir, "noext").createNewFile()

        val result = StorageUtils.getUniqueFile(tempDir, "noext")

        assertNotEquals("noext", result.name)
        assertTrue(result.name.startsWith("noext_"))
    }

    // ==================== getPartialFile ====================

    @Test
    fun `getPartialFile should append partial suffix`() {
        val result = StorageUtils.getPartialFile(tempDir, "test.txt")

        assertEquals("test.txt.partial", result.name)
        assertEquals(tempDir, result.parentFile)
    }

    @Test
    fun `getPartialFile should handle nested paths`() {
        val result = StorageUtils.getPartialFile(tempDir, "file.dat")

        assertEquals("file.dat.partial", result.name)
    }

    // ==================== deletePartial ====================

    @Test
    fun `deletePartial should delete existing partial file`() {
        val partial = File(tempDir, "test.txt.partial")
        partial.createNewFile()
        assertTrue(partial.exists())

        StorageUtils.deletePartial(tempDir, "test.txt")

        assertFalse(partial.exists())
    }

    @Test
    fun `deletePartial should not throw for non-existing partial file`() {
        StorageUtils.deletePartial(tempDir, "nonexistent.txt")
    }

    // ==================== hasPartialFile ====================

    @Test
    fun `hasPartialFile should return true when partial exists`() {
        File(tempDir, "upload.bin.partial").createNewFile()

        assertTrue(StorageUtils.hasPartialFile(tempDir, "upload.bin"))
    }

    @Test
    fun `hasPartialFile should return false when partial does not exist`() {
        assertFalse(StorageUtils.hasPartialFile(tempDir, "upload.bin"))
    }

    // ==================== renameToFinal ====================

    @Test
    fun `renameToFinal should rename partial to final`() {
        val partial = File(tempDir, "result.csv.partial")
        partial.writeText("data")

        val success = StorageUtils.renameToFinal(tempDir, "result.csv")

        assertTrue(success.exists())
        assertFalse(partial.exists())
        assertTrue(File(tempDir, "result.csv").exists())
        assertEquals("data", File(tempDir, "result.csv").readText())
    }

    @Test
    fun `renameToFinal should return false if partial does not exist`() {
        val success = StorageUtils.renameToFinal(tempDir, "missing.txt")

        assertFalse(success.exists())
    }

    // ==================== 组合测试 ====================

    @Test
    fun `partial file workflow - create check delete`() {
        val fileName = "workflow.bin"

        assertFalse(StorageUtils.hasPartialFile(tempDir, fileName))

        val partial = StorageUtils.getPartialFile(tempDir, fileName)
        partial.createNewFile()

        assertTrue(StorageUtils.hasPartialFile(tempDir, fileName))

        StorageUtils.deletePartial(tempDir, fileName)

        assertFalse(StorageUtils.hasPartialFile(tempDir, fileName))
    }

    @Test
    fun `partial file workflow - create rename`() {
        val fileName = "rename.bin"
        val partial = StorageUtils.getPartialFile(tempDir, fileName)
        partial.writeBytes(byteArrayOf(1, 2, 3))

        val success = StorageUtils.renameToFinal(tempDir, fileName)

        assertTrue(success.exists())
        assertFalse(partial.exists())
        val final = File(tempDir, fileName)
        assertTrue(final.exists())
        assertArrayEquals(byteArrayOf(1, 2, 3), final.readBytes())
    }
}
