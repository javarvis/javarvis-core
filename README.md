## What?
A voice command home automation assistant written in Java.

Lightweight, it can run on a raspberry pi.

You need a microphone and speakers (if you want text-to-speech).

Depends on Snowboy that uses JNI and must be compiled on your system (see https://snowboy.kitt.ai/ ).
Depends on some Java libraries which detail can be found in pom.xml file.

## How?
Once the project compiled, use configuration examples to configure your home automation. 

Create your own AutomationProvider that matches the tech you used on your home automation.

### Simplest startup

Here is the tree of how I deploy javarvis.
```
¦¦¦ bin-conf
¦   ¦¦¦ snowboy
¦       ¦¦¦ common.res
¦       ¦¦¦ jarvis.pmdl
¦¦¦ conf
¦   ¦¦¦ javarvis.properties
¦   ¦¦¦ p2c-text-automation.txt
¦¦¦ itineric-bootstrapper-0.1.0-beta.jar
¦¦¦ jni
¦   ¦¦¦ libsnowboy-detect-java.so
¦¦¦ lib
¦   ¦¦¦ ... dependencies ....
¦   ¦¦¦ javarvis-core-0.1.0-beta.jar
¦   ¦¦¦ ... dependencies ....
¦¦¦ resources
    ¦¦¦ log4j2.xml
```

And the command to launch it:
`java -Djava.library.path=jni -jar itineric-bootstrapper-0.1.0-beta.jar -c resources -l lib -r com.itineric.javarvis.core.Javarvis -- conf/javarvis.properties`
