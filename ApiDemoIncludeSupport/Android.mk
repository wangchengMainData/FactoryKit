LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

# 1.code in project
#LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/app/src/main/aidl
# 2.code not in project
LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/GosuncnZFYFWSupport/src/main/java

LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main/java)

# 1.code in project
#LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/aidl)
#LOCAL_SRC_FILES += $(call all-Iaidl-files-under, app/src/main/aidl)
# 2.code not in project
LOCAL_SRC_FILES += $(call all-java-files-under, GosuncnZFYFWSupport/src/main/java)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, GosuncnZFYFWSupport/src/main/java)

$(warning ###LIST SRC FILES### $(LOCAL_SRC_FILES))

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/app/src/main/res

#LOCAL_ASSET_DIR := $(LOCAL_PATH)/app/src/main/assets

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml

LOCAL_AAPT_FLAGS += --auto-add-overlay  \
#    --extra-packages appcompat  \
#    --extra-packages constraintlayout  \
#    --extra-packages material \
#    --extra-packages cardview

#LOCAL_SRC_FILES += .aidl

LOCAL_PACKAGE_NAME := GosuncnZFYFWApiDemo
#LOCAL_OVERRIDES_PACKAGES := XXX
#LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
LOCAL_PRIVATE_PLATFORM_APIS := true
#LOCAL_SDK_VERSION

LOCAL_DEX_PREOPT := false

#LOCAL_JAVA_LIBRARIES := xxx

#LOCAL_STATIC_JAVA_LIBRARIES := xxx

#LOCAL_STATIC_JAVA_AAR_LIBRARIES := appcompat \
#    constraintlayout \
#    material \
#    cardview

LOCAL_PROGUARD_ENABLED := disabled

#$(shell cp -r $(LOCAL_PATH)/app/src/main/assets/xxx.xml $(TARGET_OUT_ETC)/xxx.xml)

include $(BUILD_PACKAGE)

# ============================================================

#include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := appcompat:app/libs/appcompat-1.0.2.aar \
#    constraintlayout:app/libs/constraintlayout-1.1.3.aar \
#    material:app/libs/material-1.0.0.aar \
#    cardview:app/libs/cardview-1.0.0.aar

#include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
