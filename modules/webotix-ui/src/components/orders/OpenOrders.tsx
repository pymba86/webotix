import React from "react";
import {Coin} from "../../modules/market";
import {DisplayOrder} from "../../modules/socket";
import ReactTable from "react-table";
import {Icon} from "../../elements/icon";
import {formatDate} from "../../modules/common/date";
import {Amount} from "../../elements/amount";
import {TableLink} from "../../elements/table";

export type CancelOrderHandler = (id: string) => void;

export interface OpenOrdersProps {
    coin: Coin;
    orders: Array<DisplayOrder>;
    onCancelExchange: CancelOrderHandler;
    onCancelServer: CancelOrderHandler;
}

const textStyle = {
    textAlign: "left"
}

const numberStyle = {
    textAlign: "right"
}

const orderTypeColumn = {
    id: "orderType",
    Header: <Icon type="shuffle"/>,
    accessor: "type",
    Cell: ({original}: { original: DisplayOrder }) => (
        <Icon type={!!original.stopPrice
            ? "circle" : original.type === "BID" ? "arrow-up" : "arrow-down"}/>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    width: 32
}

const runningAtColumn = {
    id: "runningAt",
    Header: "At",
    Cell: ({original}: { original: DisplayOrder }) =>
        original.runningAt === "SERVER" ? (
            <Icon type="server"/>
        ) : (
            <Icon type="monitor"/>
        ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    width: 32
}

const createdDateColumn = {
    id: "createdDate",
    accessor: "timestamp",
    Header: "Created",
    Cell: ({original}: { original: DisplayOrder }) =>
        original.timestamp
            ? formatDate(original.timestamp)
            : original.runningAt === "SERVER"
            ? "Not on exchange"
            : "Confirming...",
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 80
}

const limitPriceColumn = (coin: Coin) => ({
    Header: "Limit",
    Cell: ({original}: { original: DisplayOrder }) =>
        !!original.stopPrice && !original.limitPrice ? (
            "MARKET"
        ) : (
            <Amount scale={1} color={original.type === "BID" ? "buy" : "sell"}
                    noflash coin={coin} value={original.limitPrice}/>
        ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
})

const stopPriceColumn = (coin: Coin) => ({
    id: "stopPrice",
    Header: "Trigger",
    Cell: ({original}: { original: DisplayOrder }) => (
        <Amount scale={1} color={original.type === "BID" ? "buy" : "sell"} noflash coin={coin}
                value={original.stopPrice}/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
})

const amountColumn = (coin: Coin) => ({
    Header: "Amount",
    Cell: ({original}: { original: DisplayOrder }) => (
        <Amount scale={1} color={original.type === "BID" ? "buy" : "sell"} noflash coin={coin}
                value={original.originalAmount}/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
})

const filledColumn = (coin: Coin) => ({
    Header: "Filled",
    Cell: ({original}: { original: DisplayOrder }) => (
        <Amount scale={1} color={original.type === "BID" ? "buy" : "sell"} noflash coin={coin}
                value={original.cumulativeAmount}/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
})

const cancelColumn = (onCancelExchange: CancelOrderHandler, onCancelServer: CancelOrderHandler) => ({
    id: "close",
    Header: () => null,
    Cell: ({original}: { original: DisplayOrder }) =>
        original.status === "CANCELED" ? null : (
            <TableLink
                onClick={() => {
                    if (original.runningAt === "SERVER") {
                        onCancelServer(original.jobId)
                    } else {
                        onCancelExchange(original.id)
                    }
                }}>
                <Icon type="x" />
            </TableLink>
        ),
    headerStyle: textStyle,
    style: textStyle,
    width: 32,
    sortable: false,
    resizable: false
})

export const OpenOrders: React.FC<OpenOrdersProps> = ({
                                                          orders, coin,
                                                          onCancelExchange, onCancelServer
                                                      }) => (
    <ReactTable
        data={orders}
        getTrProps={(state: any, rowInfo: any) => ({
            className:
                (rowInfo.original.type === "BID" ? "oco-buy" : "oco-sell") + " oco-" + rowInfo.original.status
        })}
        columns={[
            cancelColumn(onCancelExchange, onCancelServer),
            orderTypeColumn,
            runningAtColumn,
            createdDateColumn,
            limitPriceColumn(coin),
            stopPriceColumn(coin),
            amountColumn(coin),
            filledColumn(coin)
        ]}
        showPagination={false}
        resizable={false}
        className="-striped"
        minRows={0}
        noDataText="No open orders"
        defaultPageSize={1000}
    />
)