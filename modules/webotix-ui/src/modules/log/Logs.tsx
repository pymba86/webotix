import React, {useContext} from 'react';
import {Section} from "../../elements/section";
import {Icon} from "../../elements/icon";
import {SectionLink} from "../../elements/section/SectionLink";
import ReactTable from "react-table";
import {LogContext, LogEntry} from "./LogContext";
import {formatDate} from "../common/date";

const textStyle = {
    textAlign: "left"
}

const iconStyle = {
    textAlign: "center"
}

const columns = [
    {
        id: "icon",
        Header: null,
        accessor: "notificationType",
        Cell: ({original}: { original: LogEntry }) =>
            original.level === "ERROR" ? (
                <Icon type="disc"/>
            ) : original.level === "ALERT" ? (
                <Icon type="check"/>
            ) : (
                <Icon type="terminal"/>
            ),
        headerStyle: iconStyle,
        style: iconStyle,
        resizable: true,
        width: 33
    },
    {
        id: "dateTime",
        Header: "Time",
        accessor: "dateTime",
        Cell: ({original}: { original: LogEntry }) => formatDate(original.dateTime),
        headerStyle: textStyle,
        style: textStyle,
        resizable: false,
        width: 150
    },
    {
        id: "message",
        Header: "Message",
        accessor: "message",
        Cell: ({original}: { original: LogEntry }) => <div title={original.message}>{original.message}</div>,
        headerStyle: textStyle,
        style: textStyle,
        resizable: true
    }
]

export const Logs: React.FC = () => {

    const api = useContext(LogContext);

    return (
        <Section id={"notifications"}
                 heading={"Notifications"}
                 nopadding={true}
                 buttons={() => (
                     <SectionLink onClick={api.clear}>
                         <Icon type={"trash"}/>
                     </SectionLink>
                 )}>
            <ReactTable
                data={api.logs}
                getTrProps={(state: any, original: any) => ({
                    style: {
                        color:
                            original.level === "ERROR"
                                ? '#EB4D5C'
                                : original.level === "ALERT"
                                ? '#3BB3E4'
                                : original.level === "TRACE"
                                    ? '#aaa'
                                    : undefined
                    }
                })}
                columns={columns}
                showPagination={false}
                resizable={false}
                className="-striped"
                minRows={0}
                noDataText="No new notifications"
            />
        </Section>
    )
}