# Project: Panini FIFA WC 2026 Sticker Tracker (Colombian Release)

This document serves as the foundational mandate for the development of the Panini FIFA World Cup 2026 sticker tracking application. All future implementation agents must adhere strictly to these guidelines.

## 1. Project Context
*   **Target Release:** Colombian Panini Album (Distributed by Continente).
*   **Platform:** Native Android (Kotlin).
*   **Storage:** Local-first with Room Persistence Library.
*   **UI Style:** Modern, official FIFA WC 2026 branding (Blue, Purple, Green).

## 2. Core Data Reference
The source of truth for all sticker data is `master_list.md`.
*   **Total Main Stickers:** 980 (WFC 0-19 + 48 Nations x 20 stickers).
*   **Total Bonus Stickers:** 14 Coca-Cola Exclusive (CC 1-14).
*   **Specific Identifiers:** WFC (Intro/Museum), CC (Coca-Cola), and 3-letter country codes (COL, BRA, MEX, etc.).

## 3. Implementation Mandates

### 3.1 Data Architecture
Refer to `data_model.md` for the schema.
*   **Separation of Concerns:** Keep the static `Sticker` catalog separate from the `UserSticker` progress table.
*   **Initialization:** Use `createFromAsset()` to pre-populate the database with the static catalog from a prepared SQLite file.
*   **Relationship Modeling:** Use Room `@Relation` to join stickers with user progress for efficient UI rendering.

### 3.2 UI/UX Requirements
Refer to `app_specs.md` for visual and interaction details.
*   **Primary View:** A `RecyclerView` with **Nested Accordion Grouping** (Level 1: Group/Category -> Level 2: Nation).
*   **Real-time Progress:** A permanent, sticky marker in the header showing total collection percentage (e.g., "752/994 - 75%").
*   **Input Method:** Tapping `ncopies` must trigger a **Pop-up Selector** (Modal Bottom Sheet), NOT a system keyboard.
*   **Filtering:** Multi-mode filtering by Group, Nation, and Status (Owned, Missing, Duplicated).

### 3.3 Visual Identity & States
*   **Palette:** FIFA WC 2026 Official Branding.
*   **Sticker Highlighting:**
    *   **Missing:** Dimmed background + red border.
    *   **Owned:** High contrast.
    *   **Duplicated:** Count highlighted in vibrant Orange.

## 4. Development Workflow
1.  **Phase 1: Database Setup:** [COMPLETED] Implemented Room entities, DAOs, and pre-packaged database support.
2.  **Phase 2: Data Ingestion:** [COMPLETED] Python script `ingest_data.py` converts `master_list.md` to `catalog.db`.
3.  **Phase 3: Core UI:** [COMPLETED] Implemented Nested RecyclerView with ListAdapter and sticky progress header.
4.  **Phase 4: Filtering & Logic:** [COMPLETED] Implemented Status-based filtering, Text Search, and Modal Bottom Sheet.
5.  **Phase 4.5: Data Portability:** [COMPLETED] Implemented JSON-based Export/Import via Storage Access Framework (SAF).
6.  **Phase 5: Performance & Verification:** Ensure 60fps scrolling and verify collection logic.

## 5. Verification Standards
*   Every DAO query must have a corresponding unit test.
*   The UI must handle the 994+ sticker list without frame drops (60fps) during scrolling and expansion.
*   Progress calculation must be verified against manual edge cases (0% and 100% completion).
