# Preference Settings Documentation

This document lists the settings defined in `preference_main.xml`.

## Display

### Display all menus in the main window

- **Type:** `CheckBoxPreference`
- **Key:** `pref_main_mode_menu_all`
- **Default Value Resource:** `true`

### Creation date

- **Type:** `CheckBoxPreference`
- **Key:** `pref_date`
- **Summary:** Display creation date
- **Default Value Resource:** `false`

### Modification Date

- **Type:** `CheckBoxPreference`
- **Key:** `pref_date_mod`
- **Summary:** Display modification date
- **Default Value Resource:** `false`

### Preview limit

- **Type:** `EditTextPreference`
- **Key:** `pref_preview_char_limit`
- **Summary:** Number of characters per note previewed

### Respect formatting in preview

- **Type:** `CheckBoxPreference`
- **Key:** `pref_preview_formatting`
- **Default Value Resource:** `False`

### Keep scrolling position

- **Type:** `CheckBoxPreference`
- **Key:** `pref_scroll`
- **Default Value Resource:** `False`

### Sort by

- **Type:** `ListPreference`
- **Key:** `pref_tri`
- **Entries Resource:** `@array/tri`
- **Entry Values Resource:** `@array/triValeur`

### Descending sort

- **Type:** `CheckBoxPreference`
- **Key:** `pref_ordretri`
- **Default Value Resource:** `false`

### Padding for notes in main screen

- **Type:** `EditTextPreference`
- **Key:** `pref_note_padding_main`

### Main screen buttons horizontal

- **Type:** `CheckBoxPreference`
- **Key:** `pref_forceButtonsH`
- **Default Value Resource:** `false`

## Search

### Search in content

- **Type:** `CheckBoxPreference`
- **Key:** `contentSearch`
- **Summary:** Search also in the notes\' content but not in note protected by password
- **Default Value Resource:** `false`

### Case sensitive research 

- **Type:** `CheckBoxPreference`
- **Key:** `sensitiveSearch`
- **Default Value Resource:** `true`

### Match exact word 

- **Type:** `CheckBoxPreference`
- **Key:** `wordSearch`
- **Default Value Resource:** `false`

### Display search options

- **Type:** `CheckBoxPreference`
- **Key:** `displaySearchOptions`
- **Default Value Resource:** `true`

### Vertical search options

- **Type:** `CheckBoxPreference`
- **Key:** `pref_searchCheckboxV`
- **Default Value Resource:** `false`

### Display number of found inside note search

- **Type:** `CheckBoxPreference`
- **Key:** `pref_search_note_count`
- **Default Value Resource:** `true`

## Color in main screen

### Menus color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_button_main`
- **Default Value Resource:** `0xff999999`
- **Reset Value Resource:** `0xff999999`

### Color of bottom buttons

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_button_bottom_main`
- **Default Value Resource:** `0xff000001`
- **Reset Value Resource:** `0xff000001`

### Global text color on main screen

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_text_color_main`
- **Default Value Resource:** `0xff999999`
- **Reset Value Resource:** `0xff999999`

### Background color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_color_main_bg`
- **Default Value Resource:** `0xff000001`
- **Reset Value Resource:** `0xff000001`

### Action bar color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_color_main_bar`
- **Default Value Resource:** `0xff000001`
- **Reset Value Resource:** `0xff000001`

### Use specific colors on main screen

- **Type:** `CheckBoxPreference`
- **Key:** `pref_note_text_color_main_bool`
- **Default Value Resource:** `false`

### Title\'s color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_text_color_title`
- **Default Value Resource:** `0xff999999`
- **Reset Value Resource:** `0xff999999`

### Note header\'s color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_text_color_note`
- **Default Value Resource:** `0xff999999`
- **Reset Value Resource:** `0xff999999`

### Details color

- **Type:** `app.varlorg.ambilwarna.widget.AmbilWarnaPreference`
- **Key:** `pref_note_text_color_details`
- **Default Value Resource:** `0xff999999`
- **Reset Value Resource:** `0xff999999`
