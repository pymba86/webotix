import React from "react"
import {AlertJob, Job, JobType, ScriptJob, SoftTrailingStopJob} from "../../modules/server";
import ReactTable from "react-table";
import {TableLink} from "../../elements/table";
import {Icon} from "../../elements/icon";

export type JobCallback = (id: string) => void;

export interface JobShortProps {
    data: Job[];
    onRemove: JobCallback;
    onSelect: JobCallback;
}

const describe = (job: Job) => {
    if (job.jobType === JobType.ALERT) {

        const alertJob = job as AlertJob;

        return "Send alert '" + alertJob.notification.message + "'";

    } else if (job.jobType === JobType.SCRIPT) {

        const scriptJob = job as ScriptJob;
        const ticker = scriptJob.ticker;

        return scriptJob.name + " (" + ticker.base + "/" + ticker.counter + ")"
    } else if (job.jobType === JobType.SOFT_TRAILING_STOP) {

        const scriptJob = job as SoftTrailingStopJob;
        const ticker = scriptJob.tickTrigger;

        return "Trailing stop" + " (" + ticker.base + "/" + ticker.counter + ")"
    } else {
        return "Complex (" + job.jobType + ")"
    }
}


const textStyle = {
    textAlign: "left"
};

const closeColumn = (onRemove: JobCallback) => ({
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

const nameColumn = (onSelect: JobCallback) => ({
    id: "name",
    Header: "Description",
    accessor: "id",
    Cell: ({original}: { original: Job }) => (
        <TableLink onClick={() => onSelect(original.id)}>
            {describe(original)}
        </TableLink>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 80
});

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
                                                  onRemove,
                                                  onSelect
                                              }) => (
    <ReactTable
        data={data}
        columns={[
            closeColumn(onRemove),
            typeColumn,
            nameColumn(onSelect)
        ]}
        showPagination={false}
        resizable={false}
        className="-striped"
        minRows={0}
        noDataText="No running jobs"
        defaultPageSize={1000}
    />
);