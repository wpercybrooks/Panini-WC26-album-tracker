# Panini FIFA World Cup 2026 Sticker Tracker - User Manual
*(Colombian Release - Distributed by Continente)*

Welcome to the official user manual for the Panini FIFA WC 2026 Sticker Tracker. This application is designed to help you manage your sticker collection efficiently, track your progress toward completion, and identify duplicates for trading.

---

## 1. Introduction
The Panini FIFA WC 2026 Sticker Tracker is a native Android application tailored for the Colombian edition of the album. It features a complete database of all 994 stickers, including the FWC Intro, all 48 National Teams, and the Coca-Cola exclusive stickers.

### Key Features:
*   **Complete Catalog:** Pre-loaded with all sticker names and codes.
*   **Smart Navigation:** Nested accordion view for easy browsing.
*   **CV Scanner:** Use your camera to automatically identify and add stickers.
*   **Live Statistics:** Real-time tracking of completion and swaps.
*   **Data Portability:** Backup and restore your collection via JSON files.

---

## 2. The Master Tracker
The Master Tracker is your primary interface for managing your collection.

### 2.1 Navigating the List
The list is organized by the official album structure:
1.  **FWC (Intro & Museum):** Stickers 0-19.
2.  **Groups A - L:** All 48 participating nations, each with 20 stickers.
3.  **Coca-Cola Exclusive:** Stickers CC 1-14.

![Main Tracker - Nested Accordion](images/01.jpg)

**Accordion Grouping:**
*   Tap a **Group** header (e.g., Group K) to see the nations within it.
*   Tap a **Nation** header (e.g., Colombia) to expand the list of stickers for that team.
*   The app remembers your expanded sections for quick access.

### 2.2 Adding and Removing Stickers
We use a "Reveal-on-Touch" system for maximum efficiency:
1.  Locate the sticker in the list.
2.  **Tap the Circle** showing the number of copies (e.g., "0").
3.  **+/- Buttons** will appear inline.
4.  Tap **(+)** to add a copy or **(-)** to remove one.
5.  Changes are saved instantly.

![Adding/Removing Stickers - Reveal-on-Touch](images/02.jpg)

### 2.3 Visual Indicators
*   **Missing (Red Border):** Stickers you don't have yet are dimmed with a red border and a "Missing" badge.
*   **Owned (Green):** Once you have at least one copy, the sticker becomes high-contrast with a green badge.
*   **Duplicate (Orange):** If you have 2 or more copies, the count is highlighted in vibrant orange to indicate it's available for swapping.

### 2.4 Search and Filters
Use the top bar to find specific stickers:
*   **Search:** Type a name (e.g., "James"), a code ("COL 10"), or a country ("Brazil").
*   **Status Chips:** Filter the entire list to show only:
    *   **All:** The full collection.
    *   **Owned:** Only stickers you have collected.
    *   **Missing:** Only stickers you still need.
    *   **Swaps:** Only your duplicate stickers.

---

## 3. Computer Vision (CV) Scanner
The CV Scanner allows you to add stickers to your collection by simply pointing your camera at the back of the sticker.

### 3.1 How to Use the Scanner
1.  Tap the **Camera Icon** in the navigation bar.
2.  Position the **back of the sticker** within the camera frame.
3.  The scanner is optimized to find the **Sticker ID** usually located in the **top-right corner**.
4.  A green box will appear once a valid code is detected (e.g., "BRA 15").

![CV Scanner - Sticker ID Detection](images/03.jpg)

### 3.2 Confirmation Flow
To prevent accidental additions, the scanner uses a two-step process:
1.  **Detection:** The app identifies the code.
2.  **Confirmation:** A dialog appears showing the sticker name and your current count.
3.  Tap **"Add to Collection"** to increment the count, or "Cancel" to skip.

*Note: The scanner works 100% offline. No internet connection is required.*

---

## 4. Statistics and Progress
Tap the **Stats Icon** in the bottom navigation bar to view your collection's health.

### 4.1 Global Progress
*   **Completion Bar:** See your overall percentage and exact count (e.g., 752/994).
*   **Swap Count:** See exactly how many duplicates you have available for trade.
*   **Top Repeated:** View the stickers you have the most copies of.

### 4.2 Nation Breakdown
Scroll down to see a detailed list of every nation and section:

![Statistics - Progress and Swaps](images/04.jpg)

*   **Progress:** Number of stickers collected for that specific team.
*   **Swaps:** Number of duplicates for that team.
*   **Sorting:** Use the toggle to sort this list **Alphabetically** or by **Completion Percentage**.

---

## 5. Data Management (Backup & Restore)
Protect your progress by creating backups.

### 5.1 Exporting Data
1.  Open the **Options Menu** (three dots) in the top right.
2.  Select **"Export Backup"**.
3.  Choose a location on your phone or cloud storage (Google Drive, etc.) to save the `.json` file.

### 5.2 Importing Data
1.  Select **"Import Backup"** from the Options Menu.

![Data Management - Options Menu](images/05.jpg)

2.  Select your previously saved `.json` file.
3.  **Warning:** This will overwrite your current progress with the data from the file.
4.  Confirm the action to restore your collection.

---

## 6. Frequently Asked Questions (FAQ)

**Q: The scanner isn't recognizing my sticker. What should I do?**
A: Ensure you have good lighting and that the sticker ID is clearly visible. Avoid glare on the glossy surface. The scanner works best when the sticker is flat.

**Q: Can I use this for the International version of the album?**
A: This app is specifically calibrated for the Colombian release. While many stickers are the same, the section codes and total counts are optimized for the Continente distribution.

**Q: Where is my data stored?**
A: All data is stored locally on your device. We do not upload your collection to any servers. Use the Export feature to keep your data safe.

---
*Generated on May 26, 2026*
