cd /home/runner/work/eclipse.platform.swt/eclipse.platform.swt/bundles/org.eclipse.swt && java -Dws=gtk -Darch=x86_64 build-scripts/CollectSources.java -nativeSources '/home/runner/build/gtk'
cd /home/runner/build/gtk && SWT_JAVA_HOME=/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/21.0.9-10/x64 MODEL=x86_64 OUTPUT_DIR=/home/runner/build/tmp ./build.sh install clean
cd /home/runner/work/eclipse.platform.swt/eclipse.platform.swt
