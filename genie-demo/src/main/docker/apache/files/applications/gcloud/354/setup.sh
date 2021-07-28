#!/bin/bash

set -o errexit -o nounset -o pipefail

START_DIR=`pwd`
cd `dirname ${BASH_SOURCE[0]}`
GCLOUD_BASE=`pwd`
cd ${START_DIR}

GCLOUD_DEPS=${GCLOUD_BASE}/dependencies

tar xzvf ${GCLOUD_DEPS}/google-cloud-sdk-354.0.0-linux-x86_64.tar.gz -C ${GCLOUD_DEPS}
export GCLOUD_HOME=${GCLOUD_DEPS}/google-cloud-sdk

export CURRENT_JOB_WORKING_DIR=${GENIE_JOB_DIR}
export CURRENT_JOB_TMP_DIR=${CURRENT_JOB_WORKING_DIR}/tmp

export PATH=$PATH:${GCLOUD_HOME}/bin
rm ${GCLOUD_DEPS}/google-cloud-sdk-354.0.0-linux-x86_64.tar.gz

