# uNote
[![pipeline status](https://gitlab.com/Varlorg/uNote/badges/master/pipeline.svg)](https://gitlab.com/Varlorg/uNote/commits/master)
[![release](https://gitlab.com/Varlorg/uNote/-/badges/release.svg?order_by=release_at)](https://gitlab.com/Varlorg/uNote/-/releases)

uNote is an android application, it's a lightweight and minimalist notepad. uNote is for "micro Note".
It is available on f-droid repo:

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/app/app.varlorg.unote)


# Functionality

It focuses on simplicity and gives only basic features:

* add/delete note,
* secure a note with password (since version 1.1.0)
* sort notes by create date, modification date or title
* Search notes (only title or also in content except the ones password protected)
* Export/Import database to sdcard
* Preview mode (to detect url links and email addresses )

Warning: the database may be cleaned if you update from 1.0.X to 1.1.X

# Translations

* English
* French
* German thanks to Chris Orj and paulle69
* Spanish thanks to Andrés Hernándeaz (alias auroszx)
* Portuguese thanks to John (alias Maverick74)
* Russian thanks to The-First-King

## Translation contribution

*If you want to add a new language*

Fork the project and create a folder named "values-`<ISO_code>`" in app/src/main/res, then copy the strings.xml and array.xml files from app/src/main/res/values and translate them.

*If you want to improve an existing language*

Fork the project and modify files info the folder named "values-`<ISO_code>`" in app/src/main/res.

[A guide about Android ISO code](http://www.wilsonmar.com/android_localization.htm)

# Icon

The icon of the application has been taken for openclipart.org, made by enolynn [here the link] (https://openclipart.org/detail/190827/feuille-de-carnet-sheet-book)

# Donations

In case you want to support the project and its developers you can do that on [Liberapay](https://liberapay.com/Varlorg) or [Ko-Fi](https://ko-fi.com/varlorg).

<a href='https://ko-fi.com/varlorg' target='_blank'><img height='35' style='border:0px;height:46px;' src='https://az743702.vo.msecnd.net/cdn/kofi3.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' />
<a href="https://liberapay.com/Varlorg/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg"></a>

# License and Copyright

uNote is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

# Screenshots

The main screen

![Main screen](img/main.png?raw=true "Main activity empty")

The preferences screen

![Preference screen](img/pref1.jpg?raw=true "Preferences")
![Preference screen](img/pref2.jpg?raw=true "Preferences")

Creating a note

![Note creation](img/noteEdition.png?raw=true "Create note")

The main note with the note created

![Alt text](img/listNote.png?raw=true "Main activity with one note")

Search a note

![Search feature](img/search.jpg?raw=true "Search a note")
