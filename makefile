JC = javac
JCFLAGS = -encoding utf8
JAR = jar
JARFLAGS = cvfm

default: jar

classes:
	if [ ! -d "out" ]; then \
		mkdir out ; \
	fi
	if [ -d "out/classes" ]; then \
		rm -r out/classes ; \
	fi
	mkdir out/classes

	$(JC) $(JCFLAGS) -d out/classes src/*.java

jar: classes
	$(JAR) $(JARFLAGS) out/STEM.jar src/META-INF/MANIFEST.MF -C out/classes/ . -C src/ checkmark.png

clean:
	rm -rf out/
