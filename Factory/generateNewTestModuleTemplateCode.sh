
newModuleName="$1"
newModuleName_zh_CN="$2"

if [ ! -d src/com/gosuncn/factorykit/$1 ]; then
	echo "0. mkdir src/com/gosuncn/factorykit/$1"
	mkdir src/com/gosuncn/factorykit/$1
else
	echo "0. no need to mkdir src/com/gosuncn/factorykit/$1"
fi

echo "1. src/com/gosuncn/factorykit/$1/$1.java"
echo "package com.gosuncn.zfyfactorytest.$1;

import android.os.Bundle;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.R;

public class $1 extends BaseActivity {

    private static final String TAG = $1.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(loadDefaultConfirmText(getResources().getString(R.string.$1_confirm)));
    }

    @Override
    protected void onPositiveCallback() {
        setResult(RESULT_OK);
        Utils.writeCurMessage(this, TAG, \"Pass\");
        finish();
    }

    @Override
    protected void onNegativeCallback() {
        setResult(RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, \"Failed\");
        finish();
    }
}
" > src/com/gosuncn/factorykit/$1/$1.java

#cat src/com/gosuncn/factorykit/$1/$1.java

echo "2. write to AndroidManifest.xml"

echo "        <activity
            android:name=\".$newModuleName.$newModuleName\"
            android:exported=\"false\"
            android:configChanges=\"keyboardHidden|orientation\"
            android:label=\"@string/"$newModuleName"_name\"
            android:screenOrientation=\"portrait\" >
            <intent-filter>
                <action android:name=\"android.intent.action.MAIN\" />
                <category android:name=\"android.category.factory.kit\" />
            </intent-filter>
        </activity>
" 
#>> AndroidManifest.xml

echo "3. res/xml/item_config_default.xml"
echo "
    <FunctionItem
        name=\"$newModuleName_zh_CN\"
        auto=\"true\"
        enable=\"true\"
        packageName=\"com.gosuncn.zfyfactorytest.$newModuleName\" >
    </FunctionItem>
"
echo "4. res/values/strings.xml res/values-zh/strings.xml"
echo "
    <!-- $newModuleName_zh_CN -->
    <string name=\""$newModuleName"_name\">$newModuleName</string>
    <string name=\""$newModuleName"_confirm\">Is $newModuleName normally?</string>

    <!-- $newModuleName_zh_CN -->
    <string name=\""$newModuleName"_name\">$newModuleName_zh_CN</string>
    <string name=\""$newModuleName"_confirm\">$newModuleName_zh_CN正常吗?</string>
"


