.PHONY: all b r c

all: b r
b:	# Build
	mvn package

c:  # Clean Build
	mvn clean package

r:  # Run compiled JAR
	@/usr/lib/jvm/java-25-openjdk/bin/java \
		--enable-native-access=ALL-UNNAMED \
		-cp ./target/app-1.0-SNAPSHOT.jar \
		org.example.Main