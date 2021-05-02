import React, {useContext, useState} from "react";
import {Section, SectionTab} from "../elements/section";
import {ServerContext} from "../modules/server/ServerContext";
import {Jobs} from "../components/job";
import {JobContainer} from "./JobContainer";

enum Mode {
    ONLY_COMPLEX = "complexonly",
    ALL = "all"
}

export const JobsContainer: React.FC = () => {

    const serverApi = useContext(ServerContext);

    const [mode, setMode] = useState(Mode.ONLY_COMPLEX);

    const loading = serverApi.jobsLoading;

    const rawJobs = loading ? [] : serverApi.jobs;

    const [selectId, setSelectId] = useState<string>("");

    return (
        <Section
            id={"jobs"}
            heading={'Running jobs'}
            nopadding={true}
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
            
            <Jobs data={rawJobs}
                  onRemove={(id: string) => serverApi.deleteJob(id)}
                  onSelect={setSelectId}/>
            
            {selectId && (
                <JobContainer id={selectId} onClose={() => setSelectId("")}/>
            )}
        </Section>
    )
}