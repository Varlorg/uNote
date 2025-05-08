import xml.etree.ElementTree as ET
import os
import re
def get_string_resource_value(resource_file_path, resource_name):
    """
    Retrieves the value of a string resource from an Android strings.xml file.

    Args:
        resource_file_path (str): The path to the strings.xml file.
        resource_name (str): The name of the string resource (e.g., "app_name").

    Returns:
        str or None: The value of the string resource, or None if not found.
    """
    try:
        tree = ET.parse(resource_file_path)
        root = tree.getroot()

        # Find the string element with the matching name attribute
        for string_element in root.findall('.//string'):
            if string_element.get('name') == resource_name:
                return string_element.text

    except FileNotFoundError:
        print(f"Error: String resource file not found at {resource_file_path}")
    except ET.ParseError:
        print(f"Error: Could not parse string resource file at {resource_file_path}. Make sure it's valid XML.")
    except Exception as e:
        print(f"An unexpected error occurred while reading string resource: {e}")

    return None

def generate_preference_documentation(xml_file_path, strings_file_path, output_file_path):
    """
    Generates basic documentation for Android XML preference files,
    retrieving English string values for titles.

    Args:
        xml_file_path (str): The path to the Android XML preference file.
        strings_file_path (str): The path to the English strings.xml file.
        output_file_path (str): The path where the documentation will be saved.
    """
    try:
        tree = ET.parse(xml_file_path)
        root = tree.getroot()
        # Define known preference types (you can add more as needed)
        preference_types = [
            'Preference',
            'PreferenceCategory',
            'CheckBoxPreference',
            'EditTextPreference',
            'ListPreference',
            'MultiSelectListPreference',
            'SwitchPreference',
            'SeekBarPreference',
            # Add custom preference types here if they inherit from Preference
            'AmbilWarnaPreference', # Added based on your XML
            'app.varlorg.ambilwarna.widget.AmbilWarnaPreference' # Added based on your XML
        ]
        android_namespace = '{http://schemas.android.com/apk/res/android}'
        app_namespace = '{http://schemas.android.com/apk/res-auto}' # Added for custom attributes

        with open(output_file_path, 'w', encoding='utf-8') as f:
            f.write(f"# Preference Settings Documentation\n\n")
            f.write(f"This document lists the settings defined in `{os.path.basename(xml_file_path)}`.\n\n")

            # Iterate through all elements in the XML
            for element in root.iter():
                # Check if the element is a known preference type
                element_tag = element.tag.replace(android_namespace, '').replace(app_namespace, '')
                #print(element_tag)

                if element_tag in preference_types:
                    title_ref = element.get(android_namespace + 'title')
                    key = element.get(android_namespace + 'key')
                    summary_ref = element.get(android_namespace + 'summary')
                    preference_type = element_tag
                    # Extract string resource name from the reference (e.g., "@string/pref_title")
                    title_resource_name = None
                    if title_ref and title_ref.startswith('@string/'):
                        title_resource_name = title_ref.replace('@string/', '')

                    summary_resource_name = None
                    if summary_ref and summary_ref.startswith('@string/'):
                        summary_resource_name = summary_ref.replace('@string/', '')

                    # Retrieve the actual string values
                    title_value = get_string_resource_value(strings_file_path, title_resource_name) if title_resource_name else title_ref
                    summary_value = get_string_resource_value(strings_file_path, summary_resource_name) if summary_resource_name else summary_ref

                    if preference_type == 'PreferenceCategory':
                        f.write(f"## {title_value if title_value else 'Untitled Preference'}\n\n")
                    else:
                        f.write(f"### {title_value if title_value else 'Untitled Preference'}\n\n")
                        f.write(f"- **Type:** `{preference_type}`\n")
                    if key:
                        f.write(f"- **Key:** `{key}`\n")
                    if summary_value:
                        f.write(f"- **Summary:** {summary_value}\n")

                    # Handle specific preference types with additional details
                    if preference_type == 'ListPreference':
                        entries_ref = element.get(android_namespace + 'entries')
                        entry_values = element.get(android_namespace + 'entryValues')

                        if entries_ref:
                            f.write(f"- **Entries Resource:** `{entries_ref}`\n")
                        if entry_values:
                            f.write(f"- **Entry Values Resource:** `{entry_values}`\n")
                    elif preference_type == 'CheckBoxPreference':
                        default_value = element.get(android_namespace + 'defaultValue')

                        if default_value:
                            f.write(f"- **Default Value Resource:** `{default_value}`\n")
                    elif preference_type == 'app.varlorg.ambilwarna.widget.AmbilWarnaPreference':
                        default_value = element.get(android_namespace + 'defaultValue')
                        reset_value = element.get(app_namespace +  'resetValue')
                        #print(ET.tostring(element, 'utf-8'))

                        if default_value:
                            f.write(f"- **Default Value Resource:** `{default_value}`\n")
                        if reset_value:
                            f.write(f"- **Reset Value Resource:** `{reset_value}`\n")
                    elif preference_type == 'EditTextPreference':
                        dialog_title_ref = element.get(android_namespace + 'dialogTitle')
                        dialog_message_ref = element.get(android_namespace + 'dialogMessage')

                        dialog_title_resource_name = None
                        if dialog_title_ref and dialog_title_ref.startswith('@string/'):
                            dialog_title_resource_name = dialog_title_ref.replace('@string/', '')

                        dialog_message_resource_name = None
                        if dialog_message_ref and dialog_message_ref.startswith('@string/'):
                            dialog_message_resource_name = dialog_message_ref.replace('@string/', '')

                        dialog_title_value = get_string_resource_value(strings_file_path,
                                                                       dialog_title_resource_name) if dialog_title_resource_name else dialog_title_ref
                        dialog_message_value = get_string_resource_value(strings_file_path,
                                                                         dialog_message_resource_name) if dialog_message_resource_name else dialog_message_ref

                        if dialog_title_value:
                            f.write(f"- **Dialog Title:** {dialog_title_value}\n")
                        if dialog_message_value:
                            f.write(f"- **Dialog Message:** {dialog_message_value}\n")

                    #f.write("\n---\n\n") # Separator

        print(f"Documentation generated successfully at: {output_file_path}")

    except FileNotFoundError:
        print(f"Error: XML file not found at {xml_file_path}")
    except ET.ParseError:
        print(f"Error: Could not parse XML file at {xml_file_path}. Make sure it's a valid XML.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

def list_files_in_folder(folder_path):
    """
    Lists all files (not directories) in a given folder.

    Args:
        folder_path (str): The path to the folder.

    Returns:
        list: A list of file names in the folder, or an empty list if the folder
              doesn't exist or is empty.
    """
    files_list = []
    try:
        # Check if the provided path is a directory
        if os.path.isdir(folder_path):
            # List all entries in the directory
            for entry in os.listdir(folder_path):
                # Construct the full path to the entry
                full_path = os.path.join(folder_path, entry)
                # Check if the entry is a file
                if os.path.isfile(full_path):
                    files_list.append(entry)
        else:
            print(f"Error: '{folder_path}' is not a valid directory.")
    except FileNotFoundError:
        print(f"Error: Folder not found at '{folder_path}'")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

    return files_list

if __name__ == "__main__":
    xml_folder = 'app/src/main/res/xml/'
    strings_file = 'app/src/main/res/values/strings.xml'  # Path to your English strings.xml

    files = list_files_in_folder(xml_folder)

    if files:
        print(f"Files in folder '{xml_folder}':")
        for file_name in files:
            print("Processing " + file_name)

            output_file = os.path.join('doc', os.path.splitext(file_name)[0] + '_doc.md')
            print(output_file)
            generate_preference_documentation(os.path.join(xml_folder, file_name), strings_file, output_file)
    else:
        print(f"No files found in folder '{folder_to_list}' or the folder does not exist.")



