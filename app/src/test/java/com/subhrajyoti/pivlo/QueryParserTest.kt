package com.subhrajyoti.pivlo

import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.data.search.QueryParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QueryParserTest {

    private val parser = QueryParser()

    @Test
    fun parseExtensionFilter() {
        val filter = parser.parse("ext:pdf thesis notes")
        assertEquals("pdf", filter.extension)
        assertEquals("thesis notes", filter.query)
    }

    @Test
    fun parseCategoryFilter() {
        val filter = parser.parse("type:audio podcast")
        assertEquals(FileCategory.AUDIO, filter.category)
        assertEquals("podcast", filter.query)
    }

    @Test
    fun parseSizeFilters() {
        val filterLarger = parser.parse("size:>100MB movie")
        assertEquals("movie", filterLarger.query)
        assertEquals(100L * 1024 * 1024, filterLarger.minSize)

        val filterSmaller = parser.parse("size:<10MB document")
        assertEquals("document", filterSmaller.query)
        assertEquals(10L * 1024 * 1024, filterSmaller.maxSize)
    }

    @Test
    fun parseModifiedDaysFilter() {
        val filter = parser.parse("modified:7d report")
        assertEquals("report", filter.query)
        assertNotNull(filter.modifiedAfter)
    }

    @Test
    fun parseCombinedQuery() {
        val filter = parser.parse("ext:zip size:>50MB archives")
        assertEquals("zip", filter.extension)
        assertEquals(50L * 1024 * 1024, filter.minSize)
        assertEquals("archives", filter.query)
    }
}
