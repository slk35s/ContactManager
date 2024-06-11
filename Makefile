# Nom du compilateur Java
JAVAC = javac

# Options de compilation
JFLAGS = 

# Fichiers source
SOURCES = Main.java Contact.java ContactManager.java

# Fichiers compilés
CLASSES = $(SOURCES:.java=.class)

# Tâche par défaut
default: run

# Compile les fichiers Java
%.class: %.java
	$(JAVAC) $(JFLAGS) $<

# Compile tous les fichiers source
compile: $(CLASSES)

# Exécute le programme
run: compile
	java Main

clean:
	@echo "Nettoyage des fichiers compilés..."
	rm -f *.class
