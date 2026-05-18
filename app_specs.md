# Panini FIFA WC 2026 App - Application Specifications

## 1. Primary Screen: Master Sticker Tracker
The main screen of the application provides a comprehensive list of all 994+ stickers.

### 1.1 Data Columns
Each row in the master list must display:
*   **Section:** Category name (e.g., "FWC", "Colombia", "Coca-Cola").
*   **Sticker Code:** The alphanumeric identifier (e.g., "FWC 0", "COL 10").
*   **Sticker Name:** The person or object name (e.g., "James Rodríguez", "Official Ball").
*   **ncopies:** Number of copies owned. 
    *   Default: 0.
    *   Input: A simple increment/decrement (+/-) or numeric entry.

### 1.2 Default Sort Order
The list follows the physical album structure:
1.  **FIFA Intro + Museum:** All `FWC` stickers (0-19).
2.  **National Teams:** Groups A through L, in order.
    *   Within each team: Numerical order (1-20).
3.  **Coca-Cola:** All `CC` stickers (1-14).

### 1.3 Grouping Functionality (Nested Accordion)
*   **Hierarchical Behavior:** The list supports two levels of nesting:
    *   **Level 1: Category/Group** (e.g., "FWC", "Group K", "Coca-Cola").
    *   **Level 2: Nation** (e.g., "Colombia" within "Group K").
*   Users can expand/collapse at either level to manage the long list of stickers.
*   **Visual Cue:** Use standard chevron icons for expand/collapse states.

### 1.4 Filtering & Search
A persistent filter bar at the top allows users to narrow the view:
*   **By Group:** (e.g., Filter only Group K).
*   **By Nation:** (e.g., Filter only "Brazil").
*   **By Status:**
    *   **Owned:** `ncopies >= 1`
    *   **Missing:** `ncopies == 0`
    *   **Duplicated:** `ncopies >= 2`

---

## 1.5 Statistics View
A secondary screen accessible via a **Bottom Navigation Bar** that provides a deep dive into the collection's status.

### 1.5.1 Collection Overview
*   **Global Progress:** Displays the total number of stickers collected out of the album's total (e.g., "752/994") and the overall completion percentage.
*   **Swap Availability:** Displays the total count of duplicate stickers available for trade.
*   **Top Repeated Stickers:** Lists the top 3 stickers with the highest number of copies.

### 1.5.2 Progress by Nation
A sortable list displaying every nation/group (FWC, COL, BRA, Coca-Cola, etc.) with:
*   **Progress:** Number of stickers owned in that section and percentage.
*   **Duplicates:** Total number of swaps available for that specific nation.
*   **Sorting Options:**
    *   **Percentage (Default):** Descending order (highest completion first).
    *   **Alphabetical:** A-Z order by nation/group name.

---

## 2. Technical UI Requirements (Android/Kotlin)
*   **View Component:** `RecyclerView` with a `ListAdapter` and a custom tree-based data structure to handle nested groups.
*   **Progress Marker:** A permanent, sticky header or status bar element showing real-time completion (e.g., "752/994 Stickers Collected - 75%").
*   **Search/Filter:** Use `SearchView` and `ChipGroup` for Status filters.
*   **Navigation:** **BottomNavigationView** for switching between the Master Tracker and the Statistics View.

## 3. Interaction Design
*   **ncopies Adjustment (Reveal-on-Touch):** Tapping the `ncopies` circle reveals inline **+/- buttons**. 
    *   **+ Button:** Increments the count by 1.
    *   **- Button:** Decrements the count by 1 (minimum 0).
    *   This provides a fast, one-tap adjustment method without the need for a system keyboard or modal dialog.
*   **Visual Branding:** Adhere to the official FIFA WC 2026 color palette (Blue, Purple, and Green accents) for a modern, global look.
*   **Sticker Highlighting:** 
    *   **Missing:** Slightly dimmed background with a **subtle red border**.
    *   **Owned:** High contrast text with white/clean background.
---

## 4. Data Portability
The app provides tools for users to back up and restore their collection progress.

### 4.1 Export Backup
*   **Trigger:** "Export Backup" in the Options Menu.
*   **Format:** JSON file containing an array of sticker IDs and their respective `ncopies`.
*   **Mechanism:** Uses Storage Access Framework (SAF) to let users choose a save location.
*   **Inclusion:** Only stickers with `ncopies > 0` are exported to keep the file lightweight.

### 4.2 Import Backup
*   **Trigger:** "Import Backup" in the Options Menu.
*   **Mechanism:** Uses Storage Access Framework (SAF) to let users select a compatible JSON file.
*   **Safety:** Displays a confirmation warning that current progress will be overwritten.
*   **Behavior:** (Overwrite) Replaces the entire local collection status with the file contents. Stickers not present in the JSON file are reset to 0 copies.
