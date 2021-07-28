#!/usr/bin/env python

# Copyright 2016-2020 Netflix, Inc.
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

import logging

import yaml
from pygenie.client import Genie
from pygenie.conf import GenieConf

logging.basicConfig(level=logging.INFO)

LOGGER = logging.getLogger(__name__)


def load_yaml(yaml_file: str):
    with open(yaml_file) as _file:
        return yaml.load(_file, Loader=yaml.FullLoader)

genie_conf: GenieConf = GenieConf()
genie_conf.genie.url = "http://genie:8080"

genie: Genie = Genie(genie_conf)

dataproc_cluster_id: str = genie.create_cluster(load_yaml("clusters/dataproc.yml"))
LOGGER.info(f"Created Dataproc cluster with id = {dataproc_cluster_id}")

dataproc_application_id: str = genie.create_application(load_yaml("applications/gcloud354.yml"))
LOGGER.info(f"Created Dataproc application with id = {dataproc_application_id}")

dataproc_command_id: str = genie.create_command(load_yaml("commands/dataproc_job_spark.yml"))
LOGGER.info(f"Created Dataproc command with id = {dataproc_command_id}")

genie.set_application_for_command(dataproc_command_id, [dataproc_application_id])
LOGGER.info(f"Set applications for Dataproc command to = {dataproc_application_id}")

