Grails Jesque
=============

This is a fork of the jesque addon for grails 3: https://github.com/Grails-Plugin-Consortium/grails-jesque

See Release Notes below for a detailed list of changes.



How do I use it?
----------------
Add the jesque plugin to grails, it will automatically pull in jesque with it's dependencies, and the grails redis plugin.

```bash
dependencies {
    compile('org.grails.plugins:jesque:1.2.1')
}
```

You must also have [redis](http://redis.io) installed in your environment.


Example to enqueue

```groovy

class BackgroundJob {
    def someOtherService //auto-wiring supported

    def perform( arg1, arg2 ) {
        def domainObject = DomainClass.get(arg1) //GORM supported
        domainObject.message = arg2
        domainObject.save()
    }
}

class SomeOtherClass {
    def jesqueService

    def doWorkAsync() {
        jesqueService.enqueue( 'myQueueName', BackgroundJob.simpleName, 1, 'hi there')
    }

    def doWorkAsyncLater() {
            jesqueService.enqueueAt(System.currentTimeMillis() + (1000 * 60), 'myQueueName', BackgroundJob.simpleName, 1, 'hi there')
        }
}
```

Workers can be started manually by calling

```groovy
    jesqueService.startWorker( 'DemoJesqueJobQueue', DemoJesqueJob.simpleName, DemoJesqueJob )
```

or automatically upon start-up with the following config

```yaml
---
grails:
    redis:
        port: 6379
        host: localhost
    jesque:
        enabled: true
        failedItemLimit: 1000
        pruneOrphanedScheduledJobsOnStartup: true
        pruneWorkersOnStartup: true
        createWorkersOnStartup: true
        schedulerThreadActive: true
        startPaused: false
        autoFlush: true
        nextQueueStrategy: RESET_TO_HIGHEST_PRIORITY // or DRAIN_WHILE_MESSAGES_EXISTS
        workers:
            DemoJesqueJobPool:
                queueNames:
                    - "DemoQueue1"
                    - "DemoQueue2"
                jobTypes:
                    - org.grails.jesque.demo.DemoJesqueJob
                    - org.grails.jesque.demo.DemoTwoJesqueJob
```

The redis pool used is configured in the [redis](https://github.com/grails-plugins/grails-redis) plugin:

```yaml
grails:
    redis:
        host: localhost
        port: 6379
```

Or using sentinels

```yaml
grails:
    redis:
        sentinels:
            - 10.0.0.1:26379
            - 10.0.0.2:26379
        masterName: foobar        
```

Jobs
----
Jobs should be placed in grails-app/jobs similar to the [Quartz](http://grails.org/plugin/quartz) plugin.
However, to not clash with quartz, and to retain similarties with resque, the method to execute must be called perform.

You can run the script create-jesque-job to create a shell of a job for you automatically.  The
following will create a BackgroundJob in the grails-app/jobs folder.

```bash
grails create-jesque-job org.grails.jesque.demo.DemoJesqueJob
```

```groovy
package org.grails.jesque.demo

import groovy.util.logging.Slf4j

@Slf4j
class DemoJesqueJob {

    static queue = 'DemoJesqueJobQueue'
    static workerPool = 'DemoJesqueJobPool'

    static triggers = {
        cron name: 'DemoJesqueJobTrigger', cronExpression: '0/15 * * * * ? *'
    }

    def perform() {
        log.info "Executing Job"
    }
}
```

Custom Worker Listener
----
You can define one or more custom WorkerListener classes that will be automatically added to all workers started from within `jesqueService`.
You can implement the `GrailsApplicationAware` interface if you need access to the `grailsApplication` in your worker listener.

```groovy
grails {
    jesque {
        custom {
            listener.clazz = [LoggingWorkerListener] // accepts String, Class or List<String> or List<Class>
        }
    }
}
```
 
*All Listeners have to implement the WorkerListener Interface otherwise they will simply be ignored*

Release Notes
----

Note: only changes made by uberall are listed here.

* 1.3.0-UBERALL
    * proper pooling by using jesque classes
    * allow listeners to implement GrailsApplicationAware
    * expose `nextQueueStrategy` setting
    * expose `failedItemLimit` setting
    * use delayed job feature provided by jesque
    * refurbished worker listeners (don't flush if job failed)
    * fix autoFlush bug (allows autoFlush to be set to false)
    * properly close persistenceInterceptor when flush failed
    * synchronous init to prevent race-conditions during app start
    * stop workers as early as possible during application shutdown sequence
    * add ability to prune orphaned scheduled jobs on startup (via `pruneOrphanedScheduledJobsOnStartup`)
    * add ability to remove delayed jobs
    * only stop scheduler thread if it has been started
    * log args of executing job
* 1.3.1-UBERALL
    * statically compile JesqueService and GrailsJesqueJobFactory
    * update to jesque:2.1.6-UBERALL
* 1.3.2-UBERALL
    * add support for pausing all workers of a specific host
    * allow renaming the worker thread name (via `threadNameChangingEnabled`)
    * overhauled stopAllWorkers(); new config items: `shutdownSoftDelay` and `shutdownHardDelay`

License
-------
Copyright 2011 Michael Cameron
Copyright 2020 Uberall GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
