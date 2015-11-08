[![Build Status](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master/build_image)](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master)

# gocd-cleanup-artifacts
Tiny (well not so tiny) program that helps you with cleaning up artifacts on GoCD servers. There was a [discussion](https://groups.google.com/forum/#!topic/go-cd/HfOY_74OKhI/discussion) on GoCD mailing list and my own not so good experience managing artifacts on GoCD that led me into this. 

## How is it suposed to work?
- Have a whitelist of pipelines who can't fail because of Artifacts missing error
- Specify how many versions of the pipeline and all it's upstream dependencies you want to keep
- We keep the union of above list and the latest 2 versions of all the pipelines and delete the rest

## Usage
```bash
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
gocd.cleanup {
  server = "http://ci-server:8080"
  username = "admin"
  password = "badget"
  artifacts-dir = "/data/go-server/artifacts/pipelines/"

  pipelines = [{
    name = "Pipeline1"
    # Number of successful runs of this pipeline and all it's upstream dependencies you want to keep
    # This number can't be greater than PipelineConfig.MAX_RUN_LIMIT (5)
    runs = 2
  }]
}
```

## FAQs
### Do I need to add every new pipeline being created to the config? 
Generally No, we only do cleanup for the pipelines that are either direct / transitive dependencies of the pipelines specified in the configuration. But if your new pipeline has the same common dependency as the one specified in the configuration, then you might want to add the new pipeline to the config since they might be dependent on various versions of upstream pipelines.  

### Is there a way to run the Janitor without deleting anything? 
Yes, you could run the janitor with `--dry-run` flag. It doesn't delete but just print the directories that will be deleted.

### Does Janitor keep the run logs or delete that as well?
No, Janitor doesn't keep stage run logs.

### How does Janitor decide if the Pipeline run is a Failure or a Success?
Since there isn't an universal way to say if the pipeline has failed or not, because a stage could fail, but we could deem it unimportant (for the time being) and continue the pipeline.

Janitor is sensitive about what it call failures of a pipeline. The conditions are as follows

1. Any 1 stage failure is considered a pipeline failure.
2. If the pipeline doesn't run to completion (paused or locked) is considered a failure.


## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
