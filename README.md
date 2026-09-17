# 🅿️ Parking Management System — Software Re-Engineering Project

A case study in **software re-engineering**: taking an existing open-source
Java Swing application, identifying real code smells and defects through
manual analysis, refactoring the codebase to fix them, and extending it with
new functionality.

Original project cloned from GitHub → analyzed → refactored → forward-engineered.

---

## 🎯 Project Goal

This project follows the classic **re-engineering workflow** taught in
Software Re-Engineering (SRE):

```
Original Code  →  Reverse Engineering  →  Refactoring  →  Forward Engineering
 (as-is system)    (understand & find       (fix without      (extend with
                     smells/bugs)             changing           new features)
                                               behavior)
```

1. **Reverse Engineering** — Read and understood all 13 `.java` files
   (~2,750 lines) of the original Parking Management System to build a full
   picture of its structure, responsibilities, and problems.
2. **Refactoring** — Fixed 17 identified code smells (duplication, security
   issues, poor naming, God classes, etc.) **without changing external
   behavior**.
3. **Forward Engineering** — Added new features (Staff Management module)
   on top of the cleaned-up codebase.

---

## 🛠️ Tech Stack

- **Java** (Swing GUI, NetBeans-generated forms)
- **MS Access** database via **JDBC** (UCanAccess driver)
- **NetBeans IDE**

---

## 🐞 Code Smells Identified (17 total)

| # | Smell | Category |
|---|-------|----------|
| 1 | Duplicate Code — `Detail.java` vs `BikeDetail.java` (~95% identical) | Bloater |
| 2 | Duplicate Code — `login.java` vs `Adlog.java` | Duplication |
| 3 | Duplicate Code — `getRevenue()` repeated in 2 classes | Duplication |
| 4 | Duplicate Code — `SystemExit()` copy-pasted in 5 files | Duplication |
| 5 | Hardcoded Credentials in login screens | Security |
| 6 | SQL Injection — raw string concatenation in all DB queries | Security |
| 7 | Hardcoded absolute file paths (`D:\...`) | Portability |
| 8 | Magic Numbers — parking rates & combo-box indices | Readability |
| 9 | **Functional bug**: Bike revenue displayed "20" but calculated with rate "30" | Bug |
| 10 | Dead/unused imports | Clutter |
| 11 | No input validation — app crashes on invalid ID input | Robustness |
| 12 | Database connections never closed on error paths | Reliability |
| 13 | Generic `catch(Exception e)` swallowing errors silently | Maintainability |
| 14 | Non-descriptive naming (`jButton1`, `k`, `op`, `m1`...) | Readability |
| 15 | God-Class tendency — one class handling all DB operations | Design |
| 16 | Long methods mixing UI + business + DB logic | Design |
| 17 | Type-code strings (`"Car"`/`"Bike"`) instead of constants | Design |

*(Full details with before/after code examples in [`REFACTORING_REPORT.txt`](REFACTORING_REPORT.txt))*

---

## 🔧 Key Refactoring Techniques Applied

- **Extract Class** — created `VehicleCheckInBase` to eliminate ~120 lines
  duplicated between `Detail.java` and `BikeDetail.java`
- **Extract Method** — centralized repeated window-close logic into `WindowUtils`
- **Replace Magic Number with Constant** — all rates, table names, and
  credentials now live in one place: `AppConstants.java`
- **Introduce Parameter Object / PreparedStatement** — every SQL query
  rewritten to use bound parameters, removing SQL injection risk
- **Replace Hardcoded Path with Configuration** — `DbConfig.java` and
  `ImagePaths.java` resolve paths relative to the project, not a specific
  developer's machine
- **Remove Dead Code** — deleted an entire redundant class
  (`getRevenueFromDataBase.java`)

### Example: Fixing a real functional bug

```java
// BEFORE — label says 20, but math uses 30 (car's rate) for a bike report
Area.setText("...Parking Charges for a Bike: 20\n Total Revenue: " + (count*30));

// AFTER — single source of truth, label and math always match
int total = count * AppConstants.BIKE_PARKING_CHARGE;
return "...Parking Charges for a Bike: " + AppConstants.BIKE_PARKING_CHARGE
       + "\nTotal Revenue Collected: " + total;
```

---

## ✨ Forward Engineering — New Features Added

On top of the refactored codebase, new functionality was added:

- **Staff Management module** (`StaffManagement.java`, `StaffRegister.java`,
  `StaffDataBase.java`) — allows registering and managing staff accounts,
  extending the system beyond its original fixed-credential login.

This demonstrates that a properly refactored codebase (centralized constants,
reusable base classes, clean DB layer) is significantly easier to extend
than the original tightly-coupled version.

---

## 📁 Project Structure

```
Parking-Management-System-master/
├── src/
│   ├── login.java
│   ├── AdminPanel/          # Admin login, dashboard, revenue, search, staff mgmt
│   ├── parking/             # Core check-in/check-out screens + shared base class
│   ├── DataBase/            # DB access layer
│   ├── util/                # AppConstants, DbConfig, ImagePaths, WindowUtils (new)
│   └── lib/                 # JDBC drivers
├── Pictures/                # UI screenshots used by the app
├── REFACTORING_REPORT.txt   # Full technical report (smells, fixes, rationale)
├── HOW_TO_RUN.txt           # Setup instructions (JDBC drivers, NetBeans steps)
└── README.md
```

---

## ▶️ How to Run

This is a NetBeans project requiring the UCanAccess JDBC driver (not bundled
due to size). Full setup steps — including where to download the driver and
default login credentials — are in [`HOW_TO_RUN.txt`](HOW_TO_RUN.txt).

Quick summary:
1. Download UCanAccess 5.0.1 and copy its jars into `src/lib/`
2. Open the project in NetBeans, fix the library classpath
3. Clean & Build, then Run

---

## ⚠️ Honest Notes on This Refactor

In the interest of transparency (and because this matters for how re-engineering
work should be evaluated):

- This refactor was done through careful manual code reading and
  cross-referencing, not a live compiler, since a JDK wasn't available in the
  environment it was performed in. **It must be verified with a real
  Clean & Build in NetBeans** before being considered final.
- One real bug was found *during* this process (a database path resolution
  issue) and fixed — documented in full in `REFACTORING_REPORT.txt` for
  transparency about what changed and why.
- A few deliberate non-changes are documented too (e.g. NetBeans-generated
  `jButton1`-style names were left alone, since safely renaming them requires
  the NetBeans GUI form editor, not manual text edits).

---

## 📌 Key Takeaways

- Practiced structured **reverse engineering** — reading and mapping an
  unfamiliar ~2,750-line codebase before touching it
- Identified and categorized **17 distinct code smells** across
  duplication, security, design, and robustness dimensions
- Applied **behavior-preserving refactoring techniques** with before/after
  justification for each change
- Found and fixed a **real functional bug** hidden by code duplication
- Extended the refactored system with a **new feature module**, showing the
  practical payoff of clean code for future development

---

## 🔗 Contact

**[Zainab Akram]**
[LinkedIn](www.linkedin.com/in/zainab-akram77) 
