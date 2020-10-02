import React, {useContext, useState} from "react";
import {Section, SectionTab} from "../elements/section";
import {ServerContext} from "../modules/server/ServerContext";
import {JobShort} from "../components/job";

enum Mode {
    ONLY_COMPLEX = "complexonly",
    ALL = "all"
}

export const JobsContainer: React.FC = () => {

    const serverApi = useContext(ServerContext);

    const [mode, setMode] = useState(Mode.ONLY_COMPLEX);

    const loading = serverApi.jobsLoading;

    const rawJobs = loading ? [] : serverApi.jobs;

    const complexOnly = mode === Mode.ONLY_COMPLEX;

    const JobsContent: React.FC = ({children}) => {
        if (loading) {
            return <div>Загрузка...</div>
        } else if (rawJobs.length === 0) {
            if (complexOnly) {
                return <div>No complex jobs. </div>
            } else {
                return <div>No active jobs</div>
            }
        } else {
            return (
                <React.Fragment>
                    {children}
                </React.Fragment>
            )
        }
    }

    return (
        <Section
            id={"jobs"}
            heading={'Running jobs'}
            buttons={() => (
                <React.Fragment>
                    <SectionTab
                        selected={mode === Mode.ONLY_COMPLEX}
                        onClick={() => setMode(Mode.ONLY_COMPLEX)}>
                        Complex only
                    </SectionTab>
                    <SectionTab
                        selected={mode === Mode.ALL}
                        onClick={() => setMode(Mode.ALL)}>
                        All
                    </SectionTab>
                </React.Fragment>
            )}>
            <JobsContent>
                {rawJobs.map(job => (
                    <JobShort key={job.id} job={job} onRemove={() => {
                    }}/>
                ))}
            </JobsContent>
        </Section>
    )
};