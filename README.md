[![Build Status](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master/build_image)](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master)

# gocd-janitor
Tiny program that helps you with cleaning up artifacts on GoCD servers. There was a [discussion](https://groups.google.com/forum/#!topic/go-cd/HfOY_74OKhI/discussion) on GoCD mailing list and my own not-so-good experience managing artifacts on GoCD led me to this.

## How does Janitor work?
- We try to keep a default set of runs for each pipeline and all it's upstream dependencies.
- You can also override the value of runs to keep per pipeline in the configuration

## Usage
```
$ mvn clean package
$ java -cp target/gocd-cleanup-artifacts-0.0.1-jar-with-dependencies.jar in.ashwanthkumar.gocd.artifacts.Janitor --help
Option (* = required)  Description                           
---------------------  -----------                           
* --config             Path to janitor configuration         
--dry-run              Doesn't delete anything just emits the
                         files for deletion                  
--help                 Display this help message             

----

$ java -cp target/gocd-cleanup-artifacts-0.0.1-jar-with-dependencies.jar in.ashwanthkumar.gocd.artifacts.Janitor --conf gocd-purge.conf --dry-run
```

## Configuration
```hocon
gocd.janitor {
  server = "http://ci-server:8080"
  username = "admin"
  password = "badget"
  
  # Path to the location where we've all the pipeline directories
  artifacts-dir = "/data/go-server/artifacts/pipelines/"

  # Default number of pipeline versions to keep for all the pipelines
  pipeline-versions = 5

  pipelines = [{
    name = "Pipeline1"
    # Number of successful runs of this pipeline and all it's upstream dependencies you want to keep
    runs = 2
  }]
}
```

## FAQs
### Where do I run the Janitor from?
It is expected to be run on the go server machine where the artifacts are stored. If you run agents on the server, then you create a pipeline and assign it to an agent on the server machine.

### Do I need to add every new pipeline being created to the config? 
No, we will automatically pick up the new pipeline on the next run of the Janitor.

### Is there a way to run the Janitor without deleting anything? 
Yes, you could run the janitor with `--dry-run` flag. It doesn't delete but just print the directories that will be deleted.

### Does Janitor keep the run logs?
No, Janitor doesn't keep stage run logs. If you want this feature, please raise it as an Issue or even better send a Pull Request.

### How does Janitor decide if the Pipeline run is a Failure or a Success?
Since there is no universal way to say if the pipeline has failed or not, because a stage could fail, but we could deem it unimportant (for the time being) and continue rest of the pipeline.

Janitor is sensitive about what it considers as failures. The conditions are as follows

1. Any 1 stage failure is considered a pipeline failure.
2. If the pipeline doesn't run to completion (because of it being paused or locked) it is considered a failure.

### Does Janitor respect the "Never Cleanup Artifacts" option of the pipeline? 
No. That's inside the GoCD's configuration and we don't have a way to syncing it yet. If you want this feature, please raise it as an Issue or even better send a Pull Request :smile:

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
