import React from "react";
import {AlertJob, Job, JobType, ScriptJob} from "../../modules/server";
import {Icon} from "../../elements/icon";

export interface JobInfoProps {
    job: Job;
}

const ScriptJobInfo: React.FC<{ job: ScriptJob }> = ({job}) => (
    <React.Fragment>
        <div><b>Name</b>: {job.name}</div>
        <div><b>Exchange</b>: {job.ticker.exchange}</div>
        <div><b>Base</b>: {job.ticker.base}</div>
        <div><b>Counter</b>: {job.ticker.counter}</div>
        <div><b>Code</b>:</div>
        <pre><code>{job.script}</code></pre>
    </React.Fragment>
);

const AlertJobInfo: React.FC<{ job: AlertJob }> = ({job}) => (
    <React.Fragment>
        Sending a telegram message: {job.notification.message}
    </React.Fragment>
);


export const JobInfo: React.FC<JobInfoProps> = ({job}) => {

    if (job.jobType === JobType.SCRIPT) {
        return <ScriptJobInfo job={job as ScriptJob}/>
    } else if (job.jobType === JobType.ALERT) {
        return <AlertJobInfo job={job as AlertJob}/>
    } else {

        return (
            <React.Fragment>
                <Icon type={"text-file"}/>
                {JSON.stringify(job, null, 2)}
            </React.Fragment>
        )
    }
};