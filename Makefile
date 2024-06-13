# Répertoire des sources
SRC_DIR := src
# Répertoire de destination pour les fichiers .class
BUILD_DIR := build
# Répertoire des bibliothèques
LIB_DIR := lib
# Fichier JAR de Gson
GSON_JAR := $(LIB_DIR)/gson-2.11.0.jar

# Commande de build
build:
	@echo Construction du projet...
	@if not exist $(BUILD_DIR) mkdir $(BUILD_DIR)
	@javac -cp "$(GSON_JAR)" -d "$(BUILD_DIR)" -sourcepath "$(SRC_DIR)" "$(SRC_DIR)/*.java"

# Commande pour nettoyer le projet (supprimer les fichiers .class)
clean:
	@echo Nettoyage...
	@if exist $(BUILD_DIR) rmdir /S /Q $(BUILD_DIR)

# Commande pour exécuter le programme
run: build
	@echo Exécution du programme...
	@java -cp "$(BUILD_DIR);$(GSON_JAR)" ContactManager

# Option 'phony' pour indiquer que 'clean', 'run', et 'build' ne sont pas des fichiers
.PHONY: build clean run
