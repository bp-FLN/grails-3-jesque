package grails.plugins.jesque

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import net.greghaines.jesque.Job
import net.greghaines.jesque.worker.WorkerAware
import net.greghaines.jesque.worker.WorkerPoolImpl

import static net.greghaines.jesque.worker.WorkerEvent.JOB_EXECUTE

@Slf4j
@InheritConstructors
class GrailsWorkerImpl extends WorkerPoolImpl {

    @Override
    protected Object execute(final Job job, final String curQueue, final Object instance) throws Exception {
        log.debug "Executing job ${job.className} with args $job.args from queue $curQueue"
        if (instance instanceof WorkerAware) {
            ((WorkerAware) instance).setWorker(this)
        }
        this.listenerDelegate.fireEvent(JOB_EXECUTE, this, curQueue, job, instance, null, null)
        instance.perform(*job.args)
    }

}
