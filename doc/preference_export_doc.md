# Preference Settings Documentation

This document lists the settings defined in `preference_export.xml`.

## Note export

### Add date to note filename

- **Type:** `CheckBoxPreference`
- **Key:** `pref_export_note_date`
- **Default Value Resource:** `True`

### Use title instead of id in note filename

- **Type:** `CheckBoxPreference`
- **Key:** `pref_export_note_title`
- **Summary:** Use note\'s title for the export filename instead of id
- **Default Value Resource:** `False`

## Database

### Export database

- **Type:** `Preference`
- **Key:** `buttonExport`

### Export database in a zip archive

- **Type:** `Preference`
- **Key:** `buttonExportPwd`
- **Summary:** Zip file can be protected with password

### Import database

- **Type:** `Preference`
- **Key:** `buttonImport`

### Import database from a zip archive

- **Type:** `Preference`
- **Key:** `buttonImportPwd`

### Export csv

- **Type:** `Preference`
- **Key:** `buttonExportCSV`
- **Summary:** Export all non-password protected notes into csv format file

### Export csv

- **Type:** `Preference`
- **Key:** `buttonExportCSVAll`
- **Summary:** Export all notes into csv format file

### Import from csv

- **Type:** `Preference`
- **Key:** `importFromCSV`
- **Summary:** Import notes from csv file (add to existing ones)

### Export all notes in plain text

- **Type:** `Preference`
- **Key:** `buttonExportAllNotes`
- **Summary:** Export all non-password protected notes in plain text in a folder

### Timestamped folder

- **Type:** `CheckBoxPreference`
- **Key:** `pref_export_note_folder_timestamped`
- **Summary:** Export all notes in a timestamped folder
- **Default Value Resource:** `true`

## Export path 
- **Key:** `exportInfo`

### Change export path 

- **Type:** `Preference`
- **Key:** `exportDirSelect`
- **Summary:** Current path 
