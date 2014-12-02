mvn clean -Dmaven.test.skip=true install
cp ~/.m2/repository/io/janusproject/io.janusproject.kernel/2.0.2.0-SNAPSHOT/io.janusproject.kernel-2.0.2.0-SNAPSHOT-with-dependencies.jar ../JanusAndroid/app/libs/JanusKernel.jar
echo "JanusKernel inserted in JanusAndroid libs."
