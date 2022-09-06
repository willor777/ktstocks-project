
/* -------------------- Step One -------------------- */

//  !! Add this to your settings.gradle repositories block under dependencyResolutionManagement!!
maven { url 'https://jitpack.io' }


// Example
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()

            maven { url 'https://jitpack.io' }
    }
}

/* -------------------- Step Two -------------------- */

// Add the dependency
dependencies{
    implementation 'com.github.willor777:ktstocks-project:<...latest-version...>'
}
