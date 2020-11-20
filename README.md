
# HelloGluon CI

This sample shows how to automatically build a Gluon Application using Github Actions.

It uses a version of [HelloGluon](https://github.com/gluonhq/gluon-samples/tree/master/HelloGluon), a Hello World application with Java 11+, JavaFX 15+, Gluon Mobile and GraalVM.
For more details about Gluon Applications in general, please have a look at the [Gluon docs](https://docs.gluonhq.com) or the [other samples](https://gluonhq.com/developers/samples/). 

This sample focusses on the continuous integration using Github Actions on these platforms:
* Windows
* MacOS
* Linux
* iOS
* Android

For iOS and Android, the build actions includes steps to properly sign and upload the binary to the Play Store and App Store.

## Build setup

Building on Github Actions is not very different from building locally. However, there are some things to note, 
particularly to make the app ready to be published in the Play Store and App Store.

We added this releaseConfiguration to the maven-client-plugin configuration:

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

For iOS:
* bundleVersion is set to the GITHUB_RUN_NUMBER, so each build will have unique CFBundleVersion. [See this doc](https://docs.gluonhq.com/#_ios) for more information.

For Android:
* versionCode is set to the GITHUB_RUN_NUMBER, so each build will have unique CFBundleVersion. [See this doc](https://docs.gluonhq.com/#_android) for more information.
* keystore configuration are taken from env variables, that will be set by the workflow action.

## Platforms

The Github action workflows are specified in [.github/workflows](tree/main/.github/workflows). 
All platform workflows use these steps to checkout the code and setup GraalVM:

      - uses: actions/checkout@v2

      - name: Setup GraalVM environment
        uses: DeLaGuardo/setup-graalvm@master
        with:
          graalvm-version: 20.3.0.java11

Next to that, all workflows share the step of setting the (optional) Gluon license:

      - name: Gluon License
        uses: gluonhq/gluon-build-license@v1
        with:
          gluon-license: ${{ secrets.GLUON_LICENSE }}

Using this step, requires you to set a GLUON_LICENSE secret in the secret configuration of your repo. 
Have a look at the [Gluon website](https://gluonhq.com/products/mobile/buy/) for more information about licences.

All workflows also have a build step like this, but customized for the specific platform:

      - name: Gluon Build
        run: mvn client:build client:package
        env:
          GRAALVM_HOME: ${{ env.JAVA_HOME }}

The next sections will focus on the differences between the different platforms. 

### Desktop Windows

    runs-on: windows-latest

The Windows workflow contains extra setup steps for the build environment:

      - name: Add msbuild to PATH
        uses: microsoft/setup-msbuild@v1.0.2

      - name: Visual Studio shell
        uses: egor-tensin/vs-shell@v1

The build step is customised to use `-Pdesktop` which means the Gluon Build will use the host as the client target. 

      - name: Gluon Build
        run: mvn -Pdesktop client:build client:package
        env:
          GRAALVM_HOME: ${{ env.JAVA_HOME }}

### Desktop MacOS

    runs-on: macOS-latest

The build step is customised to use `-Pdesktop` which means the Gluon Build will use the host as the client target. 

      - name: Gluon Build
        run: mvn -Pdesktop client:build client:package
        env:
          GRAALVM_HOME: ${{ env.JAVA_HOME }}
          

### Desktop Linux

    runs-on: ubuntu-latest

The build step is customised to use `-Pdesktop` which means the Gluon Build will use the host as the client target. 

      - name: Gluon Build
        run: mvn -Pdesktop client:build client:package
        env:
          GRAALVM_HOME: ${{ env.JAVA_HOME }}
          
          
### iOS

The iOS build has to run on macOS:

    runs-on: macOS-latest

It also has these extra steps to configure the signing idenity and provisioning profile:

      - uses: Apple-Actions/import-codesign-certs@v1
        with:
          p12-file-base64: ${{ secrets.GLUON_IOS_CERTIFICATES_FILE_BASE64 }}
          p12-password: ${{ secrets.GLUON_IOS_CERTIFICATES_PASSWORD }}

      - uses: Apple-Actions/download-provisioning-profiles@v1
        with:
          bundle-id: com.gluonhq.hello.HelloGluonApp
          issuer-id: ${{ secrets.GLUON_IOS_APPSTORE_ISSUER_ID }}
          api-key-id: ${{ secrets.GLUON_IOS_APPSTORE_KEY_ID }}
          api-private-key: ${{ secrets.GLUON_IOS_APPSTORE_PRIVATE_KEY }}

These steps use parameters from the secrets config:
* `GLUON_IOS_CERTIFICATES_FILE_BASE64` and `GLUON_IOS_CERTIFICATES_PASSWORD`: key, certificate and password used for signing the app. see below for more information.
* : the password used to export the key and certificate
* `GLUON_IOS_APPSTORE_ISSUER_ID`, `GLUON_IOS_APPSTORE_KEY_ID` and `GLUON_IOS_APPSTORE_PRIVATE_KEY`: settings for the action to call into the App Store connect API. See the [Apple docs](https://developer.apple.com/documentation/appstoreconnectapi/creating_api_keys_for_app_store_connect_api) for more information on how to configure them.

As a final step, the iOS workflow uploads the .ipa to TestFlight from where it can be tested and released.

      - uses: Apple-Actions/upload-testflight-build@master
        with:
          app-path: target/client/arm64-ios/HelloGluon.ipa
          issuer-id: ${{ secrets.GLUON_IOS_APPSTORE_ISSUER_ID }}
          api-key-id: ${{ secrets.GLUON_IOS_APPSTORE_KEY_ID }}
          api-private-key: ${{ secrets.GLUON_IOS_APPSTORE_PRIVATE_KEY }}
          
It uses secrets that were already set in previous steps. (see above)          

#### Configuring the signing identity

To configure the secrets `GLUON_IOS_CERTIFICATES_FILE_BASE64` and `GLUON_IOS_CERTIFICATES_PASSWORD` you need to:

You'll need a Mac for this one time configuration. 

* Get an iOS Distribution certificate if you don't have one already. See [this online guide](https://learn.pandasuite.com/article/646-step-2-generate-ios-distribution-certificate) for more information.
* Use `Keychain Access` on Mac, search for the newly imported certificate
* Make sure to expand the certificate so you can also see the private key that was used to sign it. 
* Select both the certificate and private key and choose File->Export items
* Export it to a .p12 file and choose a password
* The `GLUON_IOS_CERTIFICATES_FILE_BASE64` secret is the base64 encoded value of the .p12 file
* The `GLUON_IOS_CERTIFICATES_PASSWORD` is the password you used to export the key and certificate.


### Android

The Android build has to run on Linux:

    runs-on: ubuntu-latest

It also has these extra steps to configure the keystore:

      - name: Setup Android Keystore
        id: android_keystore_file
        uses: timheuer/base64-to-file@v1
        with:
          fileName: 'my.keystore'
          encodedString: ${{ secrets.GLUON_ANDROID_KEYSTORE_BASE64 }}
          
The secret `GLUON_ANDROID_KEYSTORE_BASE64` contains the base64 value of the keystore that contains the key to sign the apk.  
See the [Android docs](https://developer.android.com/studio/publish/app-signing) for more information on how to generate a keystore.

The build step includes some more variables for signing the apk:

      - name: Gluon Build
        run: mvn -Pandroid client:build client:package
        env:
          GRAALVM_HOME: ${{ env.JAVA_HOME }}
          GLUON_ANDROID_KEYSTOREPATH: ${{ steps.android_keystore_file.outputs.filePath }}
          GLUON_ANDROID_KEYSTORE_PASSWORD: ${{ secrets.GLUON_ANDROID_KEYSTORE_PASSWORD }}
          GLUON_ANDROID_KEYALIAS: ${{ secrets.GLUON_ANDROID_KEYALIAS }}
          GLUON_ANDROID_KEYALIAS_PASSWORD: ${{ secrets.GLUON_ANDROID_KEYALIAS_PASSWORD }}
          
* `GLUON_ANDROID_KEYSTORE_PASSWORD`: the generated keystore's password
* `GLUON_ANDROID_KEYALIAS`: the alias of the key that will be used for signing
* `GLUON_ANDROID_KEYALIAS_PASSWORD`: the password of the generated key

As a final step, the Android workflow uploads the .apk to the Play Store from where it can be tested and released.
   
      - name: Upload to Google Play
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.GLUON_ANDROID_SERVICE_ACCOUNT_JSON }}
          packageName: com.gluonhq.hello.hellogluonapp
          releaseFiles: target/client/aarch64-android/gvm/HelloGluon.apk
          track: beta

The secret `GLUON_ANDROID_SERVICE_ACCOUNT_JSON` is the JSON formatted key of the Google service account that has permisson to upload APKs.
See the [Android docs](https://developers.google.com/android/management/service-account) for more information how to create a service account.
