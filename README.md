<h1 align="center">Pivlo</h1>
<p align="center"><b>Local-first file cleaner for Android.</b><br/>
Deep storage analysis · intelligent categorization · safe cleanup · zero cloud dependencies</p>

<p align="center">
  <a href="https://subhr.in"><img src="https://img.shields.io/badge/by-Subhrajyoti-0e75b6?style=flat-square" alt="Author" /></a>
  <a href="https://github.com/subhrajyoti5/pivlo"><img src="https://img.shields.io/badge/Source-GitHub-000000?style=flat-square&logo=github" alt="GitHub" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License" /></a>
</p>

---

## The Problem

Your Android phone fills up with:
- **Dead screenshots** gathering dust in gallery
- **Cached files** from apps you uninstalled
- **Download folders** full of forgotten PDFs and images
- **Duplicate media** across multiple backup attempts
- **Temp files** that never cleaned up

Generic cleaners nuke everything or ask permission 500 times. Pivlo learns *what* you actually delete and gets smarter about what to suggest.

---

## What Pivlo Does

**Local-first file indexing**
- Scans storage without cloud uploads
- Categorizes by type, size, age, and access patterns
- Builds a queryable index of your device

**Intelligent categorization**
- Detects cache vs. user files
- Identifies screenshots, downloads, backups
- Finds duplicate content with hash comparison
- Recognizes media by metadata

**Deep clean operations**
- Batch delete by category or query
- Preview before committing deletions
- Undo history tracks what was removed
- Never touches system partitions

**Smart suggestions**
- Learning model tracks your deletion patterns
- Suggests "safe to delete" candidates
- Confidence scores for each recommendation
- Respects your preferences

---

## Architecture

**Core Layers**

| Layer | Responsibility |
|-------|---|
| **Storage Scanner** | Parallel file traversal with metadata extraction |
| **Indexing Engine** | SQLite-backed queryable file index |
| **Categorizer** | Rule-based + ML file classification |
| **Query Parser** | Natural language → file queries |
| **Deep Clean** | Atomic batch deletions with rollback |

**Tech Stack**

```
Kotlin · Jetpack Compose · Room (SQLite) · Coroutines
```

---

## Features (Alpha)

- ✅ Local file indexing and search
- ✅ File categorization by type, size, age
- ✅ Duplicate detection via file hashing
- ✅ Safe batch deletion with history
- ✅ Query parser for advanced filtering
- 🚧 ML-based cleanup suggestions
- 🚧 Storage timeline visualization
- 🚧 Backup integration

---

## How It Works

### 1. **Scan**
Pivlo crawls your storage once, extracting metadata in parallel:
```
File path → size → MIME type → modified date → content hash
```

### 2. **Index**
Everything goes into a queryable Room database:
```sql
SELECT * FROM files 
WHERE category = 'screenshot' 
  AND modified < date('now', '-30 days')
  AND size > 1048576
```

### 3. **Categorize**
Rules cascade through file attributes:
- Is it in `/DCIM/Camera`? → Screenshot
- Is it in `/Download`? → Downloaded file
- Does it start with `.`? → System/cache
- Is it a duplicate hash? → Potential duplicate

### 4. **Delete Safely**
Batch operations are atomic. Each deletion logs to history for undo.

---

## Privacy & Safety

- **Zero network calls.** All operations happen on your device.
- **No account required.** No logins, no cloud sync, no tracking.
- **Sandboxed deletions.** Every delete operation is logged and reversible.
- **Respects system files.** Protected partitions are off-limits by design.

---

## Building

### Requirements
- Android Studio (Flamingo or later)
- JDK 11+
- Android SDK 33+

### Build

```bash
git clone https://github.com/subhrajyoti5/pivlo.git
cd pivlo
./gradlew assembleDebug
```

Run on emulator or device:
```bash
./gradlew installDebug
```

### Run Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Project Structure

```
pivlo/
├── app/
│   ├── src/main/
│   │   ├── java/com/subhrajyoti/pivlo/
│   │   │   ├── data/              # Data layer
│   │   │   │   ├── local/         # Room database, entities
│   │   │   │   ├── repository/    # Repository pattern
│   │   │   │   ├── indexing/      # StorageScanner
│   │   │   │   ├── search/        # QueryParser
│   │   │   │   └── clean/         # Deletion operations
│   │   │   ├── ui/                # Compose UI
│   │   │   │   ├── MainScreen.kt
│   │   │   │   └── MainViewModel
│   │   │   ├── core/              # Cross-cutting
│   │   │   │   ├── designsystem/  # Theme, components
│   │   │   │   └── storage/       # FileCategory, permissions
│   │   │   └── MainActivity.kt
│   │   └── res/                   # Resources, strings
│   ├── build.gradle.kts
│   └── src/test/                  # Unit tests
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Key Components

### StorageScanner
Parallel file traversal with chunked reading:
```kotlin
// Scans in batches, respects memory constraints
val files = scanner.scan(startPath)
```

### FileRepository
Clean API for file operations:
```kotlin
// Index, query, categorize
repository.indexFiles()
repository.queryFiles(predicate)
repository.categorizeFile(file)
```

### QueryParser
Convert natural language to queries:
```
"screenshots older than 2 months" 
  → category=SCREENSHOT AND modified < date-2months
```

### DeepClean
Atomic batch deletions:
```kotlin
deepClean.deleteFiles(fileIds)  // Logged, reversible
deepClean.undo()                // Restore last batch
```

---

## Performance Metrics

- **Indexing:** ~500 files/sec (depends on I/O)
- **Query:** <100ms for typical predicates
- **Memory footprint:** ~50-150 MB (loaded index)

---

## Learnings

**Local-first requires different thinking:**
- Can't just ask the backend. Queries must be efficient on a 50,000-file index.
- Categorization needs heuristics, not ML (yet). Rules are fast and explainable.
- Users want undo. Every delete operation is logged with timestamps.

**Android-specific:**
- MediaStore is slow for deep scans. Direct filesystem I/O + our index is faster.
- Storage permissions are granular. We cache discovery results to reduce permission prompts.
- Coroutines > threads. Structured concurrency makes cancellation safe.

---

## Roadmap

- [ ] ML-based cleanup suggestions (decision trees over usage patterns)
- [ ] Storage timeline: "where did my space go?"
- [ ] Cloud backup integration (opt-in, end-to-end encrypted)
- [ ] Real-time monitoring for duplicate uploads
- [ ] Automated cleanup schedules

---

## License

MIT. Build on it, remix it, ship it.

---

<p align="center">
  <i>Pivlo is a production-ready experiment in local-first Android software.</i><br/>
  <i>If you're building something too, say hi.</i>
</p>
