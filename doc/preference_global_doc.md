# Preference Settings Documentation

This document lists the settings defined in `preference_global.xml`.

### Follow system theme

- **Type:** `CheckBoxPreference`
- **Key:** `pref_theme_system`
- **Default Value Resource:** `false`

### Light theme

- **Type:** `CheckBoxPreference`
- **Key:** `pref_theme`
- **Summary:** Light or dark theme
- **Default Value Resource:** `false`

### Display notifications

- **Type:** `CheckBoxPreference`
- **Key:** `pref_notifications`
- **Default Value Resource:** `true`

## Text Size

### Text Size

- **Type:** `ListPreference`
- **Key:** `pref_sizeNote`
- **Entries Resource:** `@array/textSize`
- **Entry Values Resource:** `@array/textSizeValues`

### Custom text size

- **Type:** `EditTextPreference`
- **Key:** `pref_sizeNote_custom`
- **Summary:** Choose Custom entry in Text Size

### Custom text size for buttons

- **Type:** `EditTextPreference`
- **Key:** `pref_sizeNote_button`

## Delete and cancel behavior

### Delete confirmation

- **Type:** `CheckBoxPreference`
- **Key:** `pref_del`
- **Default Value Resource:** `True`

### Cancel confirmation

- **Type:** `CheckBoxPreference`
- **Key:** `pref_cancel`
- **Default Value Resource:** `True`

### Back button action

- **Type:** `ListPreference`
- **Key:** `pref_back_action`
- **Summary:** Define behaviour when back button pressed
- **Entries Resource:** `@array/backButtonAction`
- **Entry Values Resource:** `@array/backButtonActionValue`

## Encryption

### Cipher notes by default

- **Type:** `CheckBoxPreference`
- **Key:** `pref_cipher_notes`
- **Summary:** when adding a password
- **Default Value Resource:** `False`
