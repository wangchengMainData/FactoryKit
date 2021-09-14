CURRENTDIR="$1"
echo "DIR:$CURRENTDIR"
#version_file="$CURRENTDIR/test.xml"
version_file="$CURRENTDIR/AndroidManifest.xml"
cat $version_file | grep version

versionCode=`grep "versionCode=" $version_file | awk -F'"' '{print$2}'`
echo versionCode:$versionCode
versionName=`grep "versionName=" $version_file | awk -F'"' '{print$2}'`
echo versionName:$versionName

echo sed -i 's/versionCode="$versionCode"/versionCode="'$2'"/g' $version_file
sed -i 's/versionCode="'$versionCode'"/versionCode="'$2'"/g' $version_file
sed -i 's/versionName="'$versionName'"/versionName="'$3'"/g' $version_file

cat $version_file | grep version

