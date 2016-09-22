### DHCPCD Config

A Java GUI Program to Edit Static IP Parameters. dhcpcd as the DHCP client to obtain IP addresses. dhcpcd uses /etc/dhcpcd.conf.

A little java application for RaspberryPi3 Jessie. Needs Java Linux ARM 32 Hard Float ABI (oracle.com/java):
```
jdk-8u101-linux-arm32-vfp-hflt.tar.gz.
```

It could work on another Debian distribution, but this repo is only tested on RPi3 with Debian Jessie.

```
#!/bin/bash
if /usr/lib/jvm/jdk8/bin/javac IPConfig.java; then
	/usr/lib/jvm/jdk8/bin/jar cvfe IPConfig.jar IPConfig *.class
fi
rm *.class
```

```
#!/bin/sh
/usr/lib/jvm/jdk8/bin/java -jar IPConfig.jar
```

#### Credits
Thanks to `http://knutejohnson.com/ipconfig/`
