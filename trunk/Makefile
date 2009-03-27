
JAVAC  = javac
OUTDIR = $(PWD)/bin
LIBDIR = $(PWD)/src/lib

SRCDIR1 = $(PWD)/src/coms6111/proj2
SRCFILES = $(SRCDIR1)/ClassificationNode.java$(SRCDIR1)/Classify.java \
	   $(SRCDIR1)/ContentSummary.java$(SRCDIR1)/ContentSummaryConstructor.java \
	   $(SRCDIR1)/DocumentSampler.java $(SRCDIR1)/LynxRunner.java \
	   $(SRCDIR1)/Query.java $ (SRCDIR1)/Result.Java\
         $(SRCDIR1)/Resultset.java $ (SRCDIR1)/RunnerCLI.Java\

build: $(SRCFILES)
	./build.sh $(OUTDIR)

all: build

exec:
	./run.sh $(LIBDIR)

clean:
	-rm -rf $(OUTDIR)
