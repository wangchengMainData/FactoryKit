LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, Log/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ThirdLib/src)
# REQ116,lights,wmd,2020.0407
LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/../GosuncnZFYFW/GosuncnZFYSDK/GosuncnZFYSDKModule/src/main/java
LOCAL_SRC_FILES += $(call all-java-files-under, ../GosuncnZFYFW/GosuncnZFYFWApiDemo/GosuncnZFYFWSupport/src/main/java)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, ../GosuncnZFYFW/GosuncnZFYSDK/GosuncnZFYSDKModule/src/main/java)
LOCAL_SRC_FILES := $(filter-out ../GosuncnZFYFW/GosuncnZFYFWApiDemo/GosuncnZFYFWSupport/src/main/java/ga/mdm/PolicyManagerUtils.java, $(LOCAL_SRC_FILES))

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/Log/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/ThirdLib/res
LOCAL_STATIC_JAVA_LIBRARIES += zxing
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_PACKAGE_NAME := GosuncnZFYFactoryTest
LOCAL_CERTIFICATE := platform

LOCAL_USE_AAPT2 := true

LOCAL_DEX_PREOPT=false
#LOCAL_SDK_VERSION := current
LOCAL_PRIVATE_PLATFORM_APIS := true
#LOCAL_JNI_SHARED_LIBRARIES := libserial_port
#libqcomfm_jni
LOCAL_JAVA_LIBRARIES := telephony-common

LOCAL_STATIC_ANDROID_LIBRARIES += \
androidx.annotation_annotation \
androidx.appcompat_appcompat
#qcom.fmradio

major := 1
#minor := 0
#point := 0
version_name := $(shell date +%y%m%d)
$(warning @@@VERSION_NAME: $(version_name))
 $(shell $(LOCAL_PATH)/version.sh $(LOCAL_PATH) $(major) $(version_name))
LOCAL_AAPT_FLAGS += --version-code $(major) \
    --version-name $(version_name)

#    --extra-packages com.google.zxing.client.android \
#    --extra-packages com.journeyapps.barcodescanner

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += zxing:libs/core-3.4.0.jar
include $(BUILD_MULTI_PREBUILT)