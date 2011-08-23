#!/usr/bin/env python
#
# File: bin-proto-view.py
# Purpose: View binary protocol message files decoded according to format specs.
#

import sys
import re

def get_tokens(line):
    return re.split('\s+', line)

def get_line(fh):
    while True:
	line = fh.readline().strip()
	if line == '':
	    break
	if line[0] != '#':
	    break
    return line

def parse_format_file(filename):
    fh = open(filename)

    # parse format params
    params = {}
    while True:
	tokens = get_tokens(get_line(fh))
	param = tokens[0]
	if param != '':
	    params[param] = tokens[1:]
	else:
	    break

    fieldspecs = []
    while True:
	tokens = get_tokens(get_line(fh))
	if tokens[0] == '':
	    break
	fieldspec = {
		'pos': int(tokens[0]),
		'label': ' '.join(tokens[1:]),
		}
	fieldspecs.append(fieldspec)

    for i in xrange(len(fieldspecs) - 1):
	fieldspecs[i]['length'] = fieldspecs[i+1]['pos'] - fieldspecs[i]['pos']

    params['msg_length'] = fieldspecs[-1]['pos']

    return {
	    'params': params,
	    'fieldspecs': fieldspecs,
	    }

def get_msg(fh, msg_start_bytes, msg_length):
    binstr = ''.join([chr(int(b, 16)) for b in msg_start_bytes])

    while True:
	sample = fh.read(msg_length)
	pos = sample.find(binstr)
	if pos > -1:
	    fh.seek(fh.tell() - len(sample) + pos)
	    return fh.read(msg_length)
	if sample == '':
	    return None

def print_messages(messages, fieldspecs):
    for fieldspec in fieldspecs:
	if not fieldspec.has_key('length'):
	    print
	    return
	print '%15s:' % fieldspec['label'][:15],
	pos = fieldspec['pos']
	length = fieldspec['length']
	for msg in messages:
	    val = 0
	    for i in xrange(length):
		val += ord(msg[pos+i]) * 256**i
		# little endian!
	    val = ''
	    for i in xrange(length):
		val += str(ord(msg[pos+i])) + ' '
	    print '%10s' % val,
	print

def decode(filename, fmt):
    fh = open(filename)

    msg_start_bytes = fmt['params']['msg_start_bytes']
    msg_length = fmt['params']['msg_length']

    messages = []
    cols = 5
    cnt = 1

    while True:
	msg = get_msg(fh, msg_start_bytes, msg_length)
	if msg is None:
	    break
	messages.append(msg)
	if len(messages) >= cols:
	    print 'Messages #%d...' % cnt
	    cnt += len(messages)
	    print_messages(messages, fmt['fieldspecs'])
	    messages = []


if __name__ == '__main__':
    formatfile = sys.argv[1]
    datafile = sys.argv[2]

    fmt = parse_format_file(formatfile)
    decode(datafile, fmt)

# eof
