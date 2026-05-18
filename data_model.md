# Panini FIFA WC 2026 App - Data Model Architecture

## Overview
This document outlines the data model for the Panini FIFA World Cup 2026 sticker tracking application. The app will be built natively for Android using **Kotlin** and **Room Database** for local persistence.

The core architectural principle is the separation of **Static Data** (the official catalog of 994+ stickers) and **Dynamic Data** (the user's collection progress). This separation allows the catalog to be updated via app updates or API syncs without risking the deletion of the user's localized collection data.

## 1. Room Entities

### 1.1 Section Entity (Static)
Represents a grouping within the album, such as a specific National Team (e.g., Colombia), the Introductory section (FWC), or Coca-Cola exclusives (CC).

```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sections")
data class Section(
    @PrimaryKey val id: String,          // e.g., "COL", "FWC", "CC"
    val name: String,                    // e.g., "Colombia", "Introductory", "Coca-Cola"
    val category: String,                // e.g., "Group K", "Specials", "Host Nations"
    val orderIndex: Int                  // For UI sorting to match physical album pages
)
```

### 1.2 Sticker Entity (Static)
Represents an individual sticker in the catalog. This table should be pre-populated when the database is created.

```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stickers",
    foreignKeys = [
        ForeignKey(
            entity = Section::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sectionId"])]
)
data class Sticker(
    @PrimaryKey val id: String,          // e.g., "COL_10"
    val code: String,                    // Display value: e.g., "COL 10"
    val sectionId: String,               // Foreign Key -> Section.id
    val name: String,                    // e.g., "Jefferson Lerma"
    val type: StickerType,               // Enum: BADGE, TEAM_PHOTO, PLAYER, MUSEUM, EMBLEM
    val isFoil: Boolean,                 // true for shiny stickers
    val numberInSection: Int             // e.g., 10 (Used for chronological sorting within a section)
)

enum class StickerType {
    BADGE,
    TEAM_PHOTO,
    PLAYER,
    MUSEUM,
    EMBLEM
}
```

### 1.3 UserSticker Entity (Dynamic)
Stores the user's actual progress. To optimize database size, rows can be inserted here *only* when a user interacts with a sticker (e.g., marks it as collected or adds a duplicate).

```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_stickers",
    foreignKeys = [
        ForeignKey(
            entity = Sticker::class,
            parentColumns = ["id"],
            childColumns = ["stickerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["stickerId"])]
)
data class UserSticker(
    @PrimaryKey val stickerId: String,   // Foreign Key -> Sticker.id
    val isCollected: Boolean = false,    // true if glued in the album
    val duplicateCount: Int = 0          // Count of repeats/swaps available
)
```

## 2. Relational POJOs (For UI Consumption)
Room allows modeling one-to-many and one-to-one relationships to easily query a sticker along with the user's progress.

```kotlin
import androidx.room.Embedded
import androidx.room.Relation

// Represents a Sticker joined with the User's progress. 
// Ideal for populating UI grids.
data class StickerWithUserProgress(
    @Embedded val sticker: Sticker,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "stickerId"
    )
    val userSticker: UserSticker? // Nullable because a user might not have interacted with it yet
)
```

## 3. Data Access Object (DAO) Strategy
The implementation agent should define `StickerDao` to handle the following core queries:

1. **Get Section Progress:** Retrieve a list of `StickerWithUserProgress` for a specific `sectionId` to display the grid view for a team.
2. **Toggle Collected:** Upsert (Insert or Update) a `UserSticker` setting `isCollected` to true/false.
3. **Increment/Decrement Duplicates:** Update the `duplicateCount` for a specific `stickerId`.
4. **Get Missing List:** Query `Sticker` where a matching `UserSticker` either doesn't exist or `isCollected == false`.
5. **Get Swap List:** Query `StickerWithUserProgress` where `duplicateCount > 0`.
6. **Overall Statistics:** Aggregate queries to calculate the total percentage of the album completed.
7. **Nation Statistics:** Logic to group all stickers by nation/category to compute owned, total, and duplicate counts per group.
8. **Sorting:** Dynamically sort nation statistics by completion percentage (descending) or alphabetically.

## 4. Initialization Strategy
The implementation agent should use **Room's `createFromAsset()`** feature. 
The static catalog (`sections` and `stickers` tables) should be provided as a pre-packaged SQLite database file (e.g., `catalog.db`) stored in the Android app's `assets/` folder. This avoids the need to parse massive JSON files on the main thread during the app's first launch.
