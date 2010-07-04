source:=images
target:=../res/drawable

co2level0:=$(patsubst $(source)/%,%,$(wildcard $(source)/co2level_*.jpg))
co2level:=$(patsubst %,$(target)/%,$(co2level0))

all: $(co2level)

$(target)/%.jpg: $(source)/%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

