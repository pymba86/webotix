import React from "react"
import classNames from "classnames";
import {AlertJob, Job, JobType} from "../../modules/server";

export interface JobShortProps {
    prefixCls?: string;
    className?: string;
    job: Job;

    onRemove(): void;
}

const describe = (job: Job) => {
    if (job.jobType === JobType.ALERT) {
        return "Send alert '" + (job as AlertJob).notification.message + "'";
    } else {
        return "Complex (" + job.jobType + ")";
    }
};

export const JobShort: React.FC<JobShortProps> = (
    {
        prefixCls = 'ui-job',
        className,
        job,
        onRemove,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <div className={classes}>
            <div>{describe(job)}</div>
        </div>
    );
};