FROM openjdk:8-jdk

ENV ANDROID_COMPILE_SDK="26"
ENV ANDROID_BUILD_TOOLS="26.0.2"
ENV ANDROID_SDK_TOOLS="24.4.1"

RUN \
   apt-get --quiet update --yes; \
   apt-get --quiet install --yes wget tar unzip lib32stdc++6 lib32z1;

RUN \
   wget --quiet --output-document=android-sdk.tgz https://dl.google.com/android/android-sdk_r${ANDROID_SDK_TOOLS}-linux.tgz && \
   tar --extract --gzip --file=android-sdk.tgz; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter android-${ANDROID_COMPILE_SDK}; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter platform-tools; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter build-tools-${ANDROID_BUILD_TOOLS}; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter extra-android-m2repository; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter extra-google-google_play_services; \
   echo y | android-sdk-linux/tools/android --silent update sdk --no-ui --all --filter extra-google-m2repository
