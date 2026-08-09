# Droid Mod Loader User Guide

Droid Mod Loader manages mod archives, installed mods, profiles, plugin state,
and physical deployment for supported Bethesda games on Android.

## Before you begin

DML requires Android 11 or newer for its shared storage workflow. Grant **Allow
access to manage all files** when Android prompts for DML's special storage
access.

Back up any game folder you plan to use for testing. DML is beta software and
physical deployment changes files in the selected game target.

## 1. Create or select a profile

Profiles keep mod, plugin, Archive Library, and deployment state separate. Select
the profile for the game setup you intend to manage before importing or deploying
mods.

Supported early game profiles include Skyrim Legendary Edition, Fallout: New
Vegas, Fallout 3, Oblivion, and Tale of Two Wastelands through Fallout: New
Vegas.

## 2. Select game folders

A physical setup uses a game `Data` folder and, where required, a Game Root.
DML stores direct filesystem paths for the active profile.

Physical deployment preflight distinguishes Data and Game Root, checks a bounded
set of game executable and master markers, rejects folders that are obviously too
broad or use the wrong target role, and checks that the selected Data and Game
Root belong to the same installation.

Tale of Two Wastelands uses a Fallout: New Vegas target. A clean Fallout: New
Vegas installation can be selected before TTW files are present.

Do not select unrelated storage roots as deployment targets.

## 3. Choose an Archive Library

Tap **Install Mod** to open the Archive Library. On first use, choose the folder
where you keep downloaded mod archives.

The selected archive folder is remembered per profile. DML scans files directly
from that folder and does not move or delete the original downloads when you
switch folders.

The Archive Library supports search, refresh, folder switching, and installed
archive history for the active profile.

Archive format is identified from file content rather than filename extension.
ZIP and supported 7z/RAR variants enter the normal installer flow. Unsupported or
unrecognized variants are rejected before they are registered as an installable
managed archive.

## 4. Install and manage mods

Installed mods are kept in profile storage managed by DML rather than copied
directly into the game during import.

Enabled mods participate in the resolved game view. Disabled mods remain
installed but do not contribute files to deployment.

Priority determines the winner when multiple enabled mods provide the same
normalized path.

## 5. Manage plugins

DML discovers Bethesda plugin files such as `.esm`, `.esp`, and `.esl` where the
selected game supports them.

Plugin activation and ordering are specific to each game:

- Skyrim Legendary Edition writes enabled plugins to `plugins.txt` and preserves the complete selected order in `loadorder.txt`.
- Oblivion, Fallout 3, Fallout: New Vegas, and TTW write enabled plugins to `plugins.txt` and apply the selected order through plugin modification timestamps.

Timestamp ordering requires a valid writable Data folder. DML preflights the
complete plugin set before changing plugin output or timestamps.

## 6. Deploy

Deployment physically writes the resolved managed mod view to the selected game
target.

DML builds a deployment plan before writing. The plan can contain additions,
updates, removals of previously managed files, backups, and blocked operations.

Physical deployment uses state for each target so deployment history for one
selected folder is not treated as the state of a different folder.

DML does not intentionally delete unrelated unmanaged files. Review warnings and
planned removals before deployment.

## 7. Recovery and diagnostics

DML records deployment journal state so interrupted work can be detected.
Recovery tools and diagnostics are safety features intended for users. They are
not tools reserved for developers.

Use diagnostics when the selected target, plugin state, archive support, or
deployment result is unclear.

If DML reports an unfinished deployment, review that warning before starting
another risky deployment operation.

## Current limitations

- DML remains beta software.
- RAR5 is detected but is not currently supported for installation.
- RAR archives protected by a password, encrypted RAR archives, and multipart RAR archives are not supported.
- Some uncommon 7z compression or encryption variants may be unsupported.
- Android can block access to another application's protected private storage.
- Guidance for game folder selection and the target marker matrix for each distribution are still limited.
- Returning to a previously used target does not automatically prove that every physical managed file is unchanged outside DML; use recovery tools when target state is uncertain.
