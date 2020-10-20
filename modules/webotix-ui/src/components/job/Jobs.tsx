import React from "react"
import {AlertJob, Job, JobType, ScriptJob} from "../../modules/server";
import ReactTable from "react-table";
import {TableLink} from "../../elements/table";
import {Icon} from "../../elements/icon";
import {Link} from "react-router-dom";

export type JobRemoveCallback = (id: string) => void;

export interface JobShortProps {
    data: Job[];
    onRemove: JobRemoveCallback;
}

const describe = (job: Job) => {
    if (job.jobType === JobType.ALERT) {

        const alertJob = job as AlertJob;

        return "Send alert '" + alertJob.notification.message + "'";

    } else if (job.jobType === JobType.SCRIPT) {

        const scriptJob = job as ScriptJob;
        const ticker = scriptJob.ticker;

        return scriptJob.name + " (" + ticker.base + "/" + ticker.counter + ")"
    } else {
        return "Complex (" + job.jobType + ")"
    }
}


const textStyle = {
    textAlign: "left"
};

const numberStyle = {
    textAlign: "right"
};

const closeColumn = (onRemove: JobRemoveCallback) => ({
    id: "close",
    Header: null,
    Cell: ({original}: { original: Job }) => (
        <TableLink title="Remove job" onClick={() => onRemove(original.id)}>
            <Icon type="x"/>
        </TableLink>
    ),
    headerStyle: textStyle,
    style: textStyle,
    width: 32,
    sortable: false,
    resizable: false
});

const nameColumn = {
    id: "name",
    Header: "Description",
    accessor: "id",
    Cell: ({original}: { original: Job }) => (
        <Link to={"/job/" + original.id}>
            {describe(original)}
        </Link>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 80
};

const typeColumn = {
    id: "type",
    Header: "Type",
    accessor: "jobType",
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 20
};

export const Jobs: React.FC<JobShortProps> = ({
                                                  data,
                                                  onRemove
                                              }) => (
    <ReactTable
        data={data}
        columns={[
            closeColumn(onRemove),
            typeColumn,
            nameColumn
        ]}
        showPagination={false}
        resizable={false}
        className="-striped"
        minRows={0}
        noDataText="No running jobs"
        defaultPageSize={1000}
    />
);