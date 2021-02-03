import {Job, JobType, OcoJob} from "../modules/server";

export const isAlert = (job: Job) =>
    job.jobType === JobType.OCO &&
    ((job: OcoJob) =>
        (job.high && job.high.job.jobType === JobType.ALERT) ||
        (job.low && job.low.job.jobType === JobType.ALERT))(job as OcoJob)

export const isStop = (job: Job) =>
    job.jobType === JobType.OCO &&
    ((job: OcoJob) =>
        (!job.low && job.high && job.high.job.jobType === JobType.LIMIT_ORDER) ||
        (!job.high && job.low && job.low.job.jobType === JobType.LIMIT_ORDER))(job as OcoJob)
