import React from "react";
import {Coin, Exchange} from "../../modules/market";
import {Ticker} from "../../modules/socket";
import ReactTable from "react-table";
import {Link} from "react-router-dom";
import {Icon} from "../../elements/icon";
import {TableLink} from "../../elements/table";
import {Amount} from "../../elements/amount";

export interface FullCoinData {
    exchangeMeta: Exchange | undefined;
    ticker: Ticker | undefined;
    hasAlert: boolean;
    priceChange: string;
    key: string;
    name: string;
    shortName: string;
    exchange: string;
    base: string;
    counter: string;
}

export type CoinCallback = (coin: Coin) => void;

export type CoinNullableCallback = (coin?: Coin) => void;

export interface CoinsProps {
    data: FullCoinData[];
    onRemove: CoinCallback;
    onClickReferencePrice: CoinCallback;
}

const textStyle = {
    textAlign: "left"
};

const numberStyle = {
    textAlign: "right"
};

const exchangeColumn = {
    id: "exchange",
    Header: "Exchange",
    accessor: "exchange",
    Cell: ({original}: { original: FullCoinData }) => (
        <Link to={"/coin/" + original.key} title="Open coin">
            {original.exchangeMeta ? original.exchangeMeta.name : original.exchange}
        </Link>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 40
};

const nameColumn = {
    id: "name",
    Header: "Name",
    accessor: "shortName",
    Cell: ({original}: { original: FullCoinData }) => (
        <Link to={"/coin/" + original.key} title="Open coin">
            {original.shortName}
        </Link>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    minWidth: 50
};

const priceColumn = {
    id: "price",
    Header: "Price",
    Cell: ({original}: { original: FullCoinData }) => (
        <Amount coin={original}
                value={original.ticker ? original.ticker.last : undefined}/>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    resizable: true,
    minWidth: 56,
    sortable: false
};

const changeColumn = (onClick: CoinCallback) => ({
    id: "change",
    Header: "Change",
    accessor: "change",
    Cell: ({original}: { original: FullCoinData }) => (
        <TableLink
            color={original.priceChange.slice(0, 1) === "-" ? "sell" : "buy"}
            onClick={() => onClick(original)}
            title="Set reference price">
            {original.priceChange}
        </TableLink>
    ),
    headerStyle: numberStyle,
    style: numberStyle,
    resizable: true,
    minWidth: 40
});

const closeColumn = (onRemove: CoinCallback) => ({
    id: "close",
    Header: null,
    Cell: ({original}: { original: FullCoinData }) => (
        <TableLink title="Remove coin" onClick={() => onRemove(original)}>
            <Icon type="x"/>
        </TableLink>
    ),
    headerStyle: textStyle,
    style: textStyle,
    width: 32,
    sortable: false,
    resizable: false
});

export const Coins: React.FC<CoinsProps> = ({
                                                data,
                                                onRemove,
                                                onClickReferencePrice,
                                            }) => (
    <ReactTable
        data={data}
        columns={[
            closeColumn(onRemove),
            exchangeColumn,
            nameColumn,
            priceColumn,
            changeColumn(onClickReferencePrice)
        ]}
        showPagination={false}
        resizable={false}
        className="-striped"
        minRows={0}
        noDataText="Add a coin by clicking +, above"
        defaultPageSize={1000}
    />
);
