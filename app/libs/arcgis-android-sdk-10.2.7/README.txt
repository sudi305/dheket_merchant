Welcome to the ArcGIS Runtime SDK for Android!

v10.2.7

----------------------------
Overview
----------------------------
The ArcGIS Runtime SDK for Android provides you with the API libraries required to build apps for ArcGIS Android.

----------------------------
SDK Contents
----------------------------
The ArcGIS Runtime SDK for Android contains everything you need to develop ArcGIS Android apps.
The contents of the SDK are provided below:

 + doc > API reference doc for arcgis android and arcgis android app toolkit api's.
 + legal > All the SDK license files.
 + lib-project > Local AAR bundle of the API
 + libs > API jar libraries for arcgis-android and arcgis-android-app-toolkit, arcgis-android core native libraries, and third party dependency jar libraries.
 + res > localization resources.
 + resources > advanced symbology resources.
 + samples > Sample projects archive (zip).  All samples are in Gradle project structure intended for use with Android Studio.

----------------------------
Getting Started
----------------------------

We recommend using [Gradle](http://www.gradle.org/).  This will automatically install all the necessary dependencies to work with ArcGIS Runtime SDK for Android.

```groovy
repositories {
    // Our internal artifactory repository
    maven {
        url 'http://esri.bintray.com/arcgis'
    }
}


dependencies {
	...
    compile 'com.esri.arcgis.android:arcgis-android:10.2.7'
}
```

----------------------------
Set up the ArcGIS Android SDK to work with local maven repository
----------------------------
1. Download the arcgis-android-sdk-10.2.7.zip file
2. Extract the contents of the archive to a location on disk
3. cd into the /lib-project/ directory
4. Copy the contents to your Maven local repository location (create the folder structure if not present).
	win: C:\Documents and Settings\[user-name]\.m2\com\esri\arcgis\android\arcgis-android\10.2.7\
    unix/mac: /Users/[user-name]/.m2/repository/com/esri/arcgis/android/arcgis-android/10.2.7/
6. Your full directory path should resemeble the following 2 files in your local maven repository: 
	win:
	C:\Documents and Settings\[user-name]\.m2\com\esri\arcgis\android\arcgis-android\10.2.7\arcgis-android-10.2.7.aar
	C:\Documents and Settings\[user-name]\.m2\com\esri\arcgis\android\arcgis-android\10.2.7\arcgis-android-10.2.7.pom
	unix/mac: 
	/Users/[user-name]/.m2/repository/com/esri/arcgis/android/arcgis-android/10.2.7/arcgis-android-10.2.7.aar
	/Users/[user-name]/.m2/repository/com/esri/arcgis/android/arcgis-android/10.2.7/arcgis-android-10.2.7.pom
7. Your local maven repo should be set, we will confirm in the next step.

----------------------------
Getting Started with Maven Local
----------------------------
1. Edit your build.gradle file to look at your local maven repository.
2. Add the following to your project root build.gradle file:


allprojects {
    repositories {
        mavenLocal()
    }
}

3. Add the following dependency to your app's build.gradle file:

dependencies {
	...
    compile 'com.esri.arcgis.android:arcgis-android:10.2.7'
}

4. From here forward you only need to add the depency from step 3 when you create new app modules by adding it to your new app modules build.gradle file. 
