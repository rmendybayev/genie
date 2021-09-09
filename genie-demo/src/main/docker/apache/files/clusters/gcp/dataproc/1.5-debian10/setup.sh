#!/bin/bash
#
# Copyright 2021.  EPAM Systems, Inc. (https://www.epam.com/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
#

set -o errexit -o nounset -o pipefail

START_DIR=`pwd`
cd `dirname ${BASH_SOURCE[0]}`
DATAPROC_BASE=`pwd`
cd ${START_DIR}

export DATAPROC_CLUSTER=$( grep -A1 -n "<name>yarn.resourcemanager.hostname</name>" ${DATAPROC_BASE}/config/yarn-site.xml | awk -F'[<>]' '/value/ {print substr($3, 1, length($3)-2)}')

