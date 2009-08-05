#!/bin/sh

#
# this script must be run as root or as an user who has write access to /opt
#

# create a new directory in /opt : /opt/openoffice.org3/program 
mkdir /opt/openoffice.org3
mkdir /opt/openoffice.org3/program

# copy script to launch easily soffice with headless & server modes
# TODO  : add path
cp soffice.bin /opt/openoffice.org3/program

# change rights :
chmod a+x /opt/openoffice.org3/program/soffice.bin

