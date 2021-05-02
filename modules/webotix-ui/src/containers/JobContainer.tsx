import React, {useContext} from "react";
import {Modal} from "../elements/modal";
import {ServerContext} from "../modules/server/ServerContext";
import {JobInfo} from "../components/job/JobInfo";

interface JobContainerProps {
    id: string;
    onClose: () => void;
}

export const JobContainer: React.FC<JobContainerProps>
    = ({id, onClose}) => {

    const serverApi = useContext(ServerContext);

    const job = React.useMemo(
        () => serverApi.jobs.find(j => j.id === id),
        [id, serverApi.jobs]);

    return (
        <Modal visible={true} big={true}
               scrolling={true}
               closable={true}
               header={id}
               onClose={onClose}>
            {job ? <JobInfo job={job}/> : <span>Job not found</span>}
        </Modal>
    )
};