# Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
#
# This file is part of EcoCitizen.
#
# EcoCitizen is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# EcoCitizen is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
#
src_dir:=src
hdpi_dir:=../res/drawable-hdpi
ldpi_dir:=../res/drawable-ldpi
mdpi_dir:=../res/drawable-mdpi

names:=$(patsubst $(src_dir)/%,%,$(wildcard $(src_dir)/*.png $(src_dir)/*.jpg))
hdpi_target:=$(patsubst %,$(hdpi_dir)/%,$(names))
ldpi_target:=$(patsubst %,$(ldpi_dir)/%,$(names))
mdpi_target:=$(patsubst %,$(mdpi_dir)/%,$(names))
launchpad_target:=launchpad/icon.png launchpad/logo.png launchpad/brand.png
market_target:=market/android.png

default: help

help:
	@cat Makefile.readme

hdpi: $(hdpi_target)

ldpi: $(ldpi_target)

mdpi: $(mdpi_target)

lp launchpad: $(launchpad_target)

market: $(market_target)

all: hdpi ldpi mdpi launchpad market

clean: 
	rm $(hdpi_target) $(ldpi_target) $(mdpi_target) $(launchpad_target) $(market_target)

$(hdpi_dir)/treebg_%.jpg: $(src_dir)/treebg_%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

$(ldpi_dir)/treebg_%.jpg: $(src_dir)/treebg_%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

$(mdpi_dir)/treebg_%.jpg: $(src_dir)/treebg_%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

$(hdpi_dir)/tree_%.png: $(src_dir)/tree_%.png
	convert -crop 370x370+300+265 -geometry 55x $< $@
	identify $@

$(ldpi_dir)/tree_%.png: $(src_dir)/tree_%.png
	convert -crop 370x370+300+265 -geometry 55x $< $@
	identify $@

$(mdpi_dir)/tree_%.png: $(src_dir)/tree_%.png
	convert -crop 370x370+300+265 -geometry 55x $< $@
	identify $@

$(hdpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 72x $< $@
	identify $@

$(ldpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 36x $< $@
	identify $@

$(mdpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 48x $< $@
	identify $@

launchpad/icon.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 14x $< $@
	identify $@

launchpad/logo.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 64x $< $@
	identify $@

launchpad/brand.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 192x $< $@
	identify $@

market/android.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 512x $< $@
	identify $@

# eof
