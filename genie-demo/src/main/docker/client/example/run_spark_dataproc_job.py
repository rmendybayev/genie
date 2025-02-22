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

##################################################################################
# This script assumes init_demo.py has already been run to configure Genie.
##################################################################################

import logging
import sys

import pygenie

logging.basicConfig(level=logging.ERROR)

LOGGER = logging.getLogger(__name__)

pygenie.conf.DEFAULT_GENIE_URL = "http://genie:8080"

# Create a job instance and fill in the required parameters
job = pygenie.jobs.GenieJob() \
    .genie_username("epmc-bdcc") \
    .job_version("3.0.0")

# Set cluster criteria which determine the cluster to run the job on
job.cluster_tags([f"sched:{str(sys.argv[1])}", "type:dataproc"])

# Set command criteria which will determine what command Genie executes for the job
job.command_tags(["type:gcloud", "ver:" + str(sys.argv[2])])
job.job_name("Genie Demo Dataproc Spark " + str(sys.argv[2]) + " Submit Job")

# Any command line arguments to run along with the command. In this case it holds
# the actual query but this could also be done via an attachment or file dependency.
# This jar location is where it is installed on the Genie node but could also pass
# the jar as attachment and use it locally
job.command_arguments(
    "--region=" + str(sys.argv[3]) + " "
    "--class=com.epam.mvplatform.commons.CsvExtractorApp "
    "--properties=spark.driver.extraJavaOptions=-Dconfig.file=app.conf,spark.executor.extraJavaOptions=-Dconfig.file=app.conf "
    "--jars=gs://mvplatform-ci-artefacts/spark-app/ingestion-data-extractor/app.conf,gs://mvplatform-ci-artefacts/spark-app/ingestion-data-extractor/commons-0.1.0-SNAPSHOT.jar "
)

# Submit the job to Genie
running_job = job.execute()

print(f"Job {running_job.job_id} is {running_job.status}")
print(running_job.job_link)

# Block and wait until job is done
running_job.wait()

print(f"Job {running_job.job_id} finished with status {running_job.status}")
