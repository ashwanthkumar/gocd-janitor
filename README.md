[![Build Status](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master/build_image)](https://snap-ci.com/ashwanthkumar/gocd-cleanup-artifacts/branch/master)

# gocd-cleanup-artifacts
Tiny (well not so tiny) program that helps you with cleaning up artifacts on GoCD servers. There was a [discussion](https://groups.google.com/forum/#!topic/go-cd/HfOY_74OKhI/discussion) on GoCD mailing list and my own not so good experience managing artifacts on GoCD that led me into this. 

## How is it suposed to work?
- Have a whitelist of pipelines who can't fail because of Artifacts missing error
- Specify how many versions of the pipeline and all it's upstream dependencies you want to keep
- We keep the union of above list and the latest 2 versions of all the pipelines and delete the rest

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
