import React, {useContext} from "react";
import {Section} from "../elements/section";
import {ServerContext} from "../modules/server/ServerContext";
import {JobShort} from "../components/job";
import {AlertJob, Job, JobType} from "../modules/server";

export const JobsContainer: React.FC = () => {

    const serverApi = useContext(ServerContext);

    const jobs: Job[] = [
        {jobType: JobType.ALERT, id: "1", notification: {message: "123"}} as AlertJob,
        {jobType: JobType.ALERT, id: "2", notification: {message: "123"}} as AlertJob,
        {jobType: JobType.ALERT, id: "3", notification: {message: "123"}} as AlertJob,
    ];

    return (
        <Section heading={'Running jobs'}>
            <>
                {jobs.map(job => (
                    <JobShort key={job.id} job={job} onRemove={() => {
                    }}/>
                ))}
            </>
        </Section>
    )
};