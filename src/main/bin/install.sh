#!/bin/bash
#
# Copyright (c) 2011-2012 André Bargull
# Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
#
# <https://github.com/anba/test262-junit>
#

#
# Description:
# Helper script to compile rhino and install it into the local maven repository
# 

RHINO=${RHINO_HOME:-"/cygdrive/d/git/rhino"}

if ! [ -d ${RHINO} ]; then
  echo "rhino directory not found"
  exit 1
fi

cd "${RHINO}"
RHINO_VERSION=`sed -n 's/^version: \(.*\)$/\1/p' < build.properties`
BUILD="build/rhino${RHINO_VERSION}"
GROUP_ID="org.mozilla"
ARTIFACT_ID="rhino"
VERSION="${RHINO_VERSION/_/.}-SNAPSHOT"

ant deepclean jar copy-source

jar cf ${ARTIFACT_ID}-${VERSION}-sources.jar -C ${BUILD}/src .

mvn install:install-file \
  -DgroupId=${GROUP_ID} \
  -DartifactId=${ARTIFACT_ID} \
  -Dversion=${VERSION} \
  -Dfile=${BUILD}/js.jar \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -DcreateChecksum=true \
  -Dsources=${ARTIFACT_ID}-${VERSION}-sources.jar
