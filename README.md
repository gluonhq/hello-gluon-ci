
# HelloGluon CI

This sample shows how to automatically build a Gluon Application using Github Actions.

It uses a version of [HelloGluon](https://github.com/gluonhq/gluon-samples/tree/master/HelloGluon), a Hello World application with Java 11+, JavaFX 15+, Gluon Mobile and GraalVM.
For more details about Gluon Applications in general, please have a look at the [Gluon docs](https://docs.gluonhq.com) or the [other samples](https://gluonhq.com/developers/samples/). 

This sample focuses on the continuous integration using Github Actions on these platforms:

* Windows
* MacOS
* Linux
* iOS
* Android
* Embedded - AArch64 Linux

All these platform specific workflows share these common steps:

* Checkout your code
* Setup the build environment, specific to the platform
* Build the application
* Upload the application

Next to the above steps, for iOS and Android, the workflow includes steps to properly sign and upload the binary to the Play Store and App Store.


## Build setup

Building using Github Actions is not very different from building locally.

On top of a [default Gluon application](https://docs.gluonhq.com/#_getting_started), the following `releaseConfiguration` was added to the maven-gluonfx-plugin configuration:

```
<configuration>
    <releaseConfiguration>
        <!-- for iOS -->
        <bundleVersion>${env.GITHUB_RUN_NUMBER}</bundleVersion>
        
        <!-- for Android -->
        <versionCode>${env.GITHUB_RUN_NUMBER}</versionCode>
        <providedKeyStorePath>${env.GLUON_ANDROID_KEYSTOREPATH}</providedKeyStorePath>
        <providedKeyStorePassword>${env.GLUON_ANDROID_KEYSTORE_PASSWORD}</providedKeyStorePassword>
        <providedKeyAlias>${env.GLUON_ANDROID_KEYALIAS}</providedKeyAlias>
        <providedKeyAliasPassword>${env.GLUON_ANDROID_KEYALIAS_PASSWORD}</providedKeyAliasPassword>
    </releaseConfiguration>
</configuration>
```

For iOS:
* bundleVersion is set to the GITHUB_RUN_NUMBER, so each build will have unique CFBundleVersion. [See this doc](https://docs.gluonhq.com/#platforms_ios_distribution_build) for more information.

For Android:
* versionCode is set to the GITHUB_RUN_NUMBER, so each build will have a unique `android:versionCode`. [See this doc](https://docs.gluonhq.com/#platforms_ios_distribution_build) for more information.
* keystore configuration are taken from env variables, that will be set by the workflow action.

## Gluon license

All workflows use this action this Gluon license action:

      - name: Gluon License
        uses: gluonhq/gluon-build-license@v1
        with:
          gluon-license: ${{ secrets.GLUON_LICENSE }}

Using a Gluon license is optional and depends on your situation.
Have a look at the [Gluon website](https://gluonhq.com/products/mobile/buy/) for more information about licences or [contact us](https://gluonhq.com/about-us/contact-us/).


## Platforms

Github Action workflows are specified in [.github/workflows](https://github.com/gluonhq/hello-gluon-ci/tree/master/.github/workflows) and configured to be triggered on `push`.
Depending on your own preference and requirements, this can of course be changed. Please refer to the [GitHub Actions documentation](https://docs.github.com/en/free-pro-team@latest/actions) for more information.

### Windows

[![Windows](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/windows.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/windows.yml)

* Workflow file: [.github/workflows/windows.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/windows.yml)
* Detailed documentation: [Gluon documentation for Windows](https://docs.gluonhq.com/#platforms_windows) for more detailed information.

### MacOS

[![MacOS](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/macos.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/macos.yml)

* Workflow file: [.github/workflows/macos.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/macos.yml)
* Detailed documentation: [Gluon documentation for Mac OS](https://docs.gluonhq.com/#platforms_macos) for more detailed information.

### Linux

[![Linux](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/linux.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/linux.yml)

* Workflow file: [.github/workflows/linux.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/linux.yml)
* Detailed documentation: [Gluon documentation for Linux](https://docs.gluonhq.com/#platforms_linux) for more detailed information.

### iOS

[![iOS](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/ios.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/ios.yml)

* Workflow file: [.github/workflows/ios.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/ios.yml)
* Detailed documentation: [Gluon documentation for iOS](https://docs.gluonhq.com/#platforms_ios) for more detailed information.

### Android

[![Android](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/android.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/android.yml)

* Workflow file: [.github/workflows/android.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/android.yml)
* Detailed documentation: [Gluon documentation for Android](https://docs.gluonhq.com/#platforms_android) for more detailed information.

### Embedded - AArch64 Linux

[![AArch64 Linux](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/aarch64-linux.yml/badge.svg)](https://github.com/gluonhq/hello-gluon-ci/actions/workflows/aarch64-linux.yml)

* Workflow file: [.github/workflows/aarch64-linux.yml](https://github.com/gluonhq/hello-gluon-ci/blob/master/.github/workflows/aarch64-linux.yml)
* Detailed documentation: [Gluon documentation for Embedded - AArch64 Linux](https://docs.gluonhq.com/#platforms_embedded) for more detailed information.
