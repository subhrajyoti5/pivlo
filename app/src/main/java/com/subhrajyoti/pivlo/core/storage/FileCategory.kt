package com.subhrajyoti.pivlo.core.storage

import androidx.compose.ui.graphics.Color
import com.subhrajyoti.pivlo.ui.theme.CategoryAppColor
import com.subhrajyoti.pivlo.ui.theme.CategoryArchiveColor
import com.subhrajyoti.pivlo.ui.theme.CategoryAudioColor
import com.subhrajyoti.pivlo.ui.theme.CategoryDevColor
import com.subhrajyoti.pivlo.ui.theme.CategoryDocumentColor
import com.subhrajyoti.pivlo.ui.theme.CategoryImageColor
import com.subhrajyoti.pivlo.ui.theme.CategoryOtherColor
import com.subhrajyoti.pivlo.ui.theme.CategoryVideoColor

enum class FileCategory(val displayName: String, val color: Color) {
    ALL("All", CategoryOtherColor),
    DOCUMENTS("Documents", CategoryDocumentColor),
    IMAGES("Images", CategoryImageColor),
    AUDIO("Audio", CategoryAudioColor),
    VIDEO("Video", CategoryVideoColor),
    ARCHIVES("Archives", CategoryArchiveColor),
    APPLICATIONS("Apps", CategoryAppColor),
    DEVELOPMENT("Code", CategoryDevColor),
    OTHER("Other", CategoryOtherColor);

    companion object {
        fun fromExtension(ext: String): FileCategory {
            val cleanExt = ext.lowercase().removePrefix(".")
            return when (cleanExt) {
                "pdf", "doc", "docx", "txt", "rtf", "odt", "pages", "xls", "xlsx", "ppt", "pptx", "epub" -> DOCUMENTS
                "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "raw", "heic", "ico" -> IMAGES
                "mp3", "wav", "aac", "flac", "ogg", "m4a", "wma", "opus" -> AUDIO
                "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "3gp", "m4v" -> VIDEO
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso" -> ARCHIVES
                "apk", "apks", "xapk", "aab" -> APPLICATIONS
                "kt", "java", "py", "c", "cpp", "h", "hpp", "js", "ts", "html", "css", "json", "xml", "rs", "go", "rb", "php", "sh", "sql" -> DEVELOPMENT
                else -> OTHER
            }
        }
    }
}
