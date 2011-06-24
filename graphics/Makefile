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
treebg_dir:=treebg
tree_dir:=tree
target:=../res/drawable

treebg0:=$(patsubst $(treebg_dir)/%,%,$(wildcard $(treebg_dir)/treebg_*.jpg))
treebg_target:=$(patsubst %,$(target)/%,$(treebg0))

tree0:=$(patsubst $(tree_dir)/%,%,$(wildcard $(tree_dir)/tree_*.png))
tree_target:=$(patsubst %,$(target)/%,$(tree0))

all: $(treebg_target) $(tree_target)

$(target)/%.jpg: $(treebg_dir)/%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

$(target)/%.png: $(tree_dir)/%.png
	convert -crop 370x370+300+265 -geometry 55x $< $@
	identify $@

