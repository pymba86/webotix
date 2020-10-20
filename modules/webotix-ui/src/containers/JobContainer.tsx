import React, {useContext} from "react";
import {RouteComponentProps} from "react-router";
import {Modal} from "../elements/modal";
import {ServerContext} from "../modules/server/ServerContext";
import {JobInfo} from "../components/job/JobInfo";

export const JobContainer: React.FC<RouteComponentProps<{ id: string }>>
    = ({history, match}) => {

    const serverApi = useContext(ServerContext);

    const jobId = match.params.id;

    const job = serverApi.jobs.find(j => j.id === jobId)

    return (
        <Modal visible={true} big={true}
               scrolling={true}
               closable={true}
               header={jobId}
               onClose={() => history.push("/")}>
            {job ? <JobInfo job={job}/> : <span>Job not found</span>}
        </Modal>
    )
};