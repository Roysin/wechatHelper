LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, app)
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_PACKAGE_NAME := WechatHelper
LOCAL_PROGUARD_ENABLED := disabled 
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_MULTILIB :=both
include $(BUILD_PACKAGE)
