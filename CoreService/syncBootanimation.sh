
prjPath=`pwd`
pkgPath="$1"
outfile="$prjPath/$pkgPath/app/src/main/res/drawable/power.xml"
#app/src/main/res/drawable/power.xml

echo "START...START..." > "$prjPath/out/bootanimation.log"

echo "prjPath:$prjPath" >> "$prjPath/out/bootanimation.log"
echo "pkgPath:$pkgPath" >> "$prjPath/out/bootanimation.log"

/bin/cp -rf "$prjPath/device/qcom/msm8953_64/prebuilt/BootAnimation/bootanimation.zip" "$prjPath/$pkgPath/"
echo "`ls $prjPath/$pkgPath/`" >>  "$prjPath/out/bootanimation.log"

unzip -o "$prjPath/$pkgPath/bootanimation.zip" -d "$prjPath/$pkgPath/bootanimation"

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>
<animation-list xmlns:android=\"http://schemas.android.com/apk/res/android\"
    android:oneshot=\"true\">" > $outfile
for file in $prjPath/$pkgPath/bootanimation/part0/*; do
    filename=${file##*/}
    filename=${filename%.*}
    extension=${file##*.}

    if [ "wav" != "$extension" ]; then
    echo "    <item android:drawable=\"@drawable/p$filename\" android:duration=\"200\" />" >> "$outfile"
    fi
done

for file in $prjPath/$pkgPath/bootanimation/part1/*; do
    filename=${file##*/}
    filename=${filename%.*}
    extension=${file##*.}

    if [ "wav" != "$extension" ]; then
    echo "    <item android:drawable=\"@drawable/p$filename\" android:duration=\"200\" />" >> "$outfile"
    fi
done
echo "</animation-list>" >> "$outfile"

for file in $prjPath/$pkgPath/bootanimation/part0/*; do
    filename=${file##*/}
    filename=${filename%.*}
    extension=${file##*.}

    if [ "wav" != "$extension" ]; then
    cp $file "$prjPath/$pkgPath/app/src/main/res/drawable/p$filename.$extension"
    fi
done

for file in $prjPath/$pkgPath/bootanimation/part1/*; do
    filename=${file##*/}
    filename=${filename%.*}
    extension=${file##*.}

    if [ "wav" != "$extension" ]; then
    cp $file "$prjPath/$pkgPath/app/src/main/res/drawable/p$filename.$extension"
    fi
done

rm -rf $prjPath/$pkgPath/bootanimation
rm -rf $prjPath/$pkgPath/bootanimation.zip

echo "END...END..." >> "$prjPath/out/bootanimation.log"
