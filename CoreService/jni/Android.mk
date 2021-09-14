LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS    := -lm -llog
LOCAL_MODULE_TAGS := optional
LOCAL_CPP_EXTENSION := .cpp
LOCAL_MODULE    := libnvgxx_jni
LOCAL_SRC_FILES := com_gosuncn_zfyfw_nv_NvUtilExt.cpp

LOCAL_CFLAGS := -Wall
LOCAL_CFLAGS += -g

LOCAL_C_INCLUDES += \
    vendor/qcom/proprietary/commonsys/fastmmi/libmmi \
    external/libcxx/include \
	external/libxml2/include \
    external/skia/include/core \
    external/libxml2/include \
    external/icu/icu4c/source/common \
    vendor/qcom/proprietary/commonsys-intf/diag/include \
    vendor/qcom/proprietary/commonsys-intf/diag/src/ \
    $(TARGET_OUT_HEADERS)/common/inc \
	$(JNI_H_INCLUDE) \
	$(TOP)/libnativehelper/include/nativehelper \
	frameworks/native/libs/nativewindow/include \
	frameworks/base/libs/androidfw/include

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libmmi libcutils liblog libdiag_system libft2 libutils libxml2 libicuuc libc libui libbinder libgui libhwui libc++ libc_malloc_debug

LOCAL_HEADER_LIBRARIES := libdiag_headers

LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include
ifeq ($(TARGET_COMPILE_WITH_MSM_KERNEL),true)
LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr
endif

LOCAL_MODULE_SUFFIX := .so
#LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_CLASS := SHARED_LIBRARIES

include $(BUILD_SHARED_LIBRARY)
