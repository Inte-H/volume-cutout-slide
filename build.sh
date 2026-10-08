#!/bin/bash
# usage: build.sh <project dir> <output apk name> [stub dir]
set -e
P=$1; OUT=$2; STUBS=$3
BT=~/android-sdk/build-tools/36.1.0; AJ=~/android-sdk/platforms/android-36/android.jar; J8=~/jdk8u492-b09/bin
cd $P; rm -rf build; mkdir -p build/stubs build/classes build/dex build/res
CP=$AJ
if [ -n "$STUBS" ]; then
  $J8/javac -nowarn -encoding UTF-8 -source 8 -target 8 -bootclasspath $AJ -d build/stubs $(find $STUBS -name '*.java')
  CP=build/stubs
fi
$J8/javac -encoding UTF-8 -source 8 -target 8 -bootclasspath $AJ -classpath $CP -d build/classes $(find src -name '*.java')
D8CP=""; [ -n "$STUBS" ] && D8CP="--classpath build/stubs"
$BT/d8 --min-api 30 --lib $AJ $D8CP --output build/dex $(find build/classes -name '*.class')
ASSETS=""; [ -d assets ] && ASSETS="-A assets"
if [ -d res ]; then $BT/aapt2 compile --dir res -o build/res/res.zip; RES=build/res/res.zip; fi
$BT/aapt2 link -I $AJ --manifest AndroidManifest.xml $ASSETS -o build/unsigned.apk $RES
(cd build/dex && python3 -I -c "import zipfile;z=zipfile.ZipFile('../unsigned.apk','a');z.write('classes.dex');z.close()")
$BT/zipalign -f -p 4 build/unsigned.apk build/aligned.apk
JAVA_HOME=~/jdk-25 PATH=~/jdk-25/bin:$PATH $BT/apksigner sign --ks ~/vol-cutout/module/key.jks --ks-pass pass:volcutout --key-pass pass:volcutout --out build/$OUT build/aligned.apk 2>/dev/null
$BT/dexdump build/$OUT 2>/dev/null | grep "Class descriptor"
ls -la build/$OUT
