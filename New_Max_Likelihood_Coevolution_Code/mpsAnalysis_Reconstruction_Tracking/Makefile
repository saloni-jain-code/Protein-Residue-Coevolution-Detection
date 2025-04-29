# Makefile

SRC_DIR := src
BUILD_DIR := build
CLASS_DIR := $(BUILD_DIR)/classes
JAR_NAME := mpsAnalysis_Reconstruction_Tracking.jar
MANIFEST := $(BUILD_DIR)/manifest.mf

# All Java files
JAVA_FILES := $(shell find $(SRC_DIR) -name "*.java")

# Default target
all: $(JAR_NAME)

$(MANIFEST):
	@mkdir -p $(BUILD_DIR)
	echo "Main-Class: analysis_launchers.mpsCoevolutionAnalyser" > $(MANIFEST)

# Compile all .java files together
$(CLASS_DIR)/.compiled: $(JAVA_FILES) $(MANIFEST)
	@mkdir -p $(CLASS_DIR)
	javac -d $(CLASS_DIR) $(JAVA_FILES)
	@touch $@

# Create .jar from compiled classes
$(JAR_NAME): $(CLASS_DIR)/.compiled
	jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

# Clean
clean:
	rm -rf $(BUILD_DIR) $(JAR_NAME)

.PHONY: all clean
