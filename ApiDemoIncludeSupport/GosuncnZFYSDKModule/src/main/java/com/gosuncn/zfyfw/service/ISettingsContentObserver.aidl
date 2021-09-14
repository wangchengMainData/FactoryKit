package com.gosuncn.zfyfw.service;

interface ISettingsContentObserver {
    void onchanged(int type, int value, in List<String> valueList);
}
