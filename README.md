# Volume Cutout Slide

[한국어](README.ko.md)

An LSPosed module for Samsung Galaxy phones. In landscape, the Good Lock
**Sound Assistant** volume panel on the camera side pops in and out at the edge
of the camera cutout area. With this module it slides in from, and out to,
beyond the screen edge. Where the panel stops is unchanged.

| Without the module | With the module |
| :---: | :---: |
| ![Panel appearing next to the camera](docs/before.gif) | ![Panel sliding in from the screen edge](docs/after.gif) |

## Requirements

- LSPosed with API 102 or later
- Good Lock Sound Assistant with its custom volume panel. The module changes only
  that panel, so without Sound Assistant it does nothing.

Tested on SM-S948N, One UI 8.5 (Android 16) with LSPosed v2.1.1 and Sound
Assistant 8.5.00.2.

## Usage

1. Download the APK from [Releases](https://github.com/Inte-H/volume-cutout-slide/releases) and install it.
2. In LSPosed, enable **Volume Cutout Slide** and check in its scope:
   - **System UI** (the module runs there)
   - the apps that fill the camera cutout area, such as YouTube
3. Restart System UI or reboot.

Apps set to fill the camera cutout area in Samsung's per-app camera cutout
setting are handled without checking them. The module applies only in landscape
when both the panel and the camera are on the right side of the screen.

LSPosed does not tell modules when the scope changes, so a newly checked app
takes effect after an app switch, up to about 30 seconds later.

## Building

```sh
./gradlew :app:assembleDebug
```

Release builds are signed with the key described in `keystore.properties` in the
project root (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).
