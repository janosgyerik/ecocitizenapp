# Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
#
# This file is part of SenspodApp.
#
# SenspodApp is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# SenspodApp is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with SenspodApp.  If not, see <http://www.gnu.org/licenses/>.
#
source:=images
target:=../res/drawable

co2level0:=$(patsubst $(source)/%,%,$(wildcard $(source)/co2level_*.jpg))
co2level:=$(patsubst %,$(target)/%,$(co2level0))

all: $(co2level)

$(target)/%.jpg: $(source)/%.jpg
	convert -geometry 480x -crop 240x+120+0 $< $@
	identify $@

