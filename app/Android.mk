LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, app)

#LOCAL_STATIC_JAVA_LIBRARIES += libVivoAnalysis
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_PACKAGE_NAME := WechatHelper
LOCAL_PROGUARD_ENABLED := disabled 
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_MULTILIB :=both
include $(BUILD_PACKAGE)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES :=libVivoAnalysis:app/libs/VivoAnalysis-v1.2.jar
#include $(BUILD_MULTI_PREBUILT)
# additionally, build unit tests in a separate .apk
# include $(call all-makefiles-under,$(LOCAL_PATH))
