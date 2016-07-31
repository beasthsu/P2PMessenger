JAVAC = $(JAVA_HOME)/bin/javac
JMF_CLASSPATH = $(JMF_HOME)/lib/jmf.jar

ifndef CLASSPATH
  CLASSPATH = $(JAVA_HOME)/jre/lib
endif

SOURCEPATH = .

#some big5 comment are mixed with the codes
JAVAC_FLAGS = -encoding big5

.PHONY: all clean PeerUI MultiChatServer

all: PeerUI MultiChatServer

PeerUI:
	"${JAVAC}" ${JAVAC_FLAGS} -sourcepath "${SOURCEPATH}" -cp "${CLASSPATH}":"${JMF_CLASSPATH}" userInterface/PeerUI.java

MultiChatServer:
	"${JAVAC}" ${JAVAC_FLAGS} -sourcepath "${SOURCEPATH}" -cp "${CLASSPATH}":"${JMF_CLASSPATH}" userInterface/MultiChatServerInterface.java

ifeq ($(OS), Windows_NT)
clean:
	del *.class /s
else 
clean:
	find -name *.class -delete
endif
