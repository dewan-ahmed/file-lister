# File Lister Change Log

## 1.0.2 - September 2019

* Removes `/gradle`, `/build`, `/node_modules` from default excludes to allow more control to the user of the Plugin.
  * If these folder are in the `.gitgnore`, then they will be ignored (Having it hardcoded in defaults seems redundant).
* Adds Gitlab CI.
* Updates configurations.
* Updates tools version.
* Updates README file.

## 1.0.1 - October 2018

* Adds two new static fields to the `FileListerPlugin`.
* Renames `FileLister` to `FileListerExtension`.
* Improves `build.gradle`.
* Updates README file.
