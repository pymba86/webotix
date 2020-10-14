package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Журнал ордеров
 */
public class ImmutableOrderBook {

    private final List<LimitOrder> asks;

    private final List<LimitOrder> bids;

    private final Date timestamp;


    ImmutableOrderBook(OrderBook orderBook) {
        this(orderBook.getTimeStamp(), orderBook.getAsks(), orderBook.getBids());
    }

    @JsonCreator
    public ImmutableOrderBook(
            @JsonProperty("timestamp") Date timestamp,
            @JsonProperty("asks") List<LimitOrder> asks,
            @JsonProperty("bids") List<LimitOrder> bids
    ) {
        this.timestamp = timestamp;
        this.asks = ImmutableList.copyOf(asks);
        this.bids = ImmutableList.copyOf(bids);
    }

    public Date getTimestamp() {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    public List<LimitOrder> getAsks() {
        return asks;
    }

    public List<LimitOrder> getBids() {
        return bids;
    }

    public List<LimitOrder> getOrders(OrderType type) {
        return type == OrderType.ASK ? asks : bids;
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = 31 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        for (LimitOrder order : this.bids) {
            hash = 31 * hash + order.hashCode();
        }
        for (LimitOrder order : this.asks) {
            hash = 31 * hash + order.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableOrderBook other = (ImmutableOrderBook) obj;
        if (!Objects.equals(this.timestamp, other.timestamp)) {
            return false;
        }
        return ordersEqual(other);
    }

    public boolean ordersEqual(ImmutableOrderBook other) {
        if (other == null) {
            return false;
        }
        if (asks == null) {
            if (other.asks != null) {
                return false;
            }
        } else if (!asks.equals(other.asks)) {
            return false;
        }
        if (bids == null) {
            return other.bids == null;
        } else return bids.equals(other.bids);
    }

    @Override
    public String toString() {

        return "ImmutableOrderBook [timestamp: "
                + timestamp
                + ", asks="
                + asks.toString()
                + ", bids="
                + bids.toString()
                + "]";
    }
}
