import React from "react";
import {OrderType, Trade, UserTrade} from "../../modules/socket";
import ReactTable from "react-table";
import {formatDate} from "../../modules/common/date";
import {Icon} from "../../elements/icon";
import {Amount} from "../../elements/amount";

const BUY_SIDE = OrderType.BID;

export interface TradeHistoryProps {
    trades: Array<Trade>;
    excludeFees: boolean;
}

const textStyle = {
    textAlign: "left"
};

const numberStyle = {
    textAlign: "right"
};

const dateColumn = {
    id: "date",
    Header: "Created",
    Cell: ({original}: { original: Trade }) => formatDate(original.timestamp),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 80
};

const orderTypeColumn = {
    id: "orderType",
    Header: <Icon type="shuffle"/>,
    Cell: ({original}: { original: Trade }) => (
        <Icon type={original.type === BUY_SIDE ? "arrow-up" : "arrow-down"}/>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    width: 32
};

const priceColumn = {
    Header: "Price",
    Cell: ({original}: { original: Trade }) => (
        <Amount scale={1} coin={original.coin}
                color={original.type === BUY_SIDE ? "buy" : "sell"}
                value={original.price}
                noflash bare/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
};

const amountColumn = {
    Header: "Amount",
    Cell: ({original}: { original: Trade }) => (
        <Amount scale={1} coin={original.coin}
                color={original.type === BUY_SIDE ? "buy" : "sell"}
                value={original.originalAmount}
                noflash bare/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
};

const feeAmountColumn = {
    Header: "Fee",
    Cell: ({original}: { original: Trade }) => (
        <Amount scale={1} color={original.type === BUY_SIDE ? "buy" : "sell"} noflash bare noValue="--"
                value={original instanceof UserTrade ? (original as UserTrade).feeAmount : undefined}/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
};

const feeCurrencyColumn = {
    Header: "Fee Ccy",
    accessor: "feeCurrency",
    headerStyle: numberStyle,
    style: numberStyle,
    sortable: false,
    resizable: true,
    minWidth: 50
};


const columns = (excludeFees: boolean) =>
    excludeFees
        ? [orderTypeColumn, dateColumn, priceColumn, amountColumn]
        : [orderTypeColumn, dateColumn, priceColumn, amountColumn, feeAmountColumn, feeCurrencyColumn];

export const TradeHistory: React.FC<TradeHistoryProps> = ({trades, excludeFees}) => (
    <ReactTable
        data={trades}
        getTrProps={(state: any, rowInfo: any) => ({
            className: rowInfo.original.type === BUY_SIDE ? "oco-buy" : "oco-sell"
        })}
        columns={columns(excludeFees)}
        showPagination={false}
        resizable={false}
        className="-striped"
        minRows={0}
        noDataText="No trade history"
        defaultPageSize={1000}
    />
);