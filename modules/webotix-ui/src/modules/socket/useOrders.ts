import {useMemo, useReducer} from "react";
import {Order, OrderStatus} from "./types";
import Immutable from "seamless-immutable";

const PLACEHOLDER_ID = "PLACEHOLDER"

export interface UserOrderApi {

    clear(): void;

    updateSnapshot(orders: Array<Order>, timestamp: number): void;

    orderUpdated(order: Order, timestamp: number): void;

    pendingCancelOrder(id: string, timestamp: number): void;

    createPlaceholder(order: Order): void;

    removePlaceholder(): void;
}

interface BaseAction {
    reduce(state: Array<Order>): Array<Order>;
}

class ClearAction implements BaseAction {

    reduce(state: Array<Order>): Array<Order> {
        return [];
    }
}

class FullUpdateAction implements BaseAction {

    private readonly timestamp: number;
    private readonly order: Order;


    constructor(order: Order, timestamp: number) {
        this.timestamp = timestamp;
        this.order = order;
    }

    reduce(state: Array<Order>): Array<Order> {
        return orderUpdated(
            state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state,
            this.order,
            this.timestamp
        )
    }
}

class RemovePlaceholderAction implements BaseAction {

    reduce(state: Array<Order>): Array<Order> {
        return state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state;
    }
}

class CreatePlaceholderAction extends FullUpdateAction {

    constructor(order: Order) {
        super({...order, id: PLACEHOLDER_ID, status: OrderStatus.PENDING_NEW}, new Date().getTime());
    }
}

class UpdateSnapshotAction implements BaseAction {

    private readonly orders: Array<Order>;
    private readonly timestamp: number;

    constructor(orders: Array<Order>, timestamp: number) {
        this.orders = orders;
        this.timestamp = timestamp;
    }

    reduce(state: Array<Order>): Array<Order> {
        let result = state ? state : [];

        const idsPresent = new Set<string>();

        for (const order of this.orders) {
            idsPresent.add(order.id)
            result = orderUpdated(result, order, this.timestamp)
        }

        if (state) {
            for (const order of state) {
                if (order.id === PLACEHOLDER_ID || idsPresent.has(order.id)) {
                    continue
                }
                result = orderUpdated(
                    state,
                    {
                        ...order,
                        id: order.id,
                        status:
                            order.status === OrderStatus.PENDING_CANCEL
                                ? OrderStatus.CANCELED : OrderStatus.PENDING_CANCEL
                    },
                    this.timestamp
                )
            }
        }

        return result;
    }
}

class StateUpdateAction implements BaseAction {
    private readonly timestamp: number
    private readonly id: string
    private readonly status: OrderStatus

    constructor(id: string, status: OrderStatus, timestamp: number) {
        this.id = id
        this.status = status
        this.timestamp = timestamp
    }

    reduce(state: Array<Order>): Array<Order> {

        const order = state.find(o => o.id === this.id);

        if (order) {
            return orderUpdated(
                state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state,
                {...order, status: this.status},
                this.timestamp
            )
        }

        return state;
    }
}

function orderUpdated(state: Array<Order>, order: Order, timestamp: number): Array<Order> {

    const isRemoval =
        order.status === OrderStatus.EXPIRED ||
        order.status === OrderStatus.CANCELED ||
        order.status === OrderStatus.FILLED;

    if (!state) {
        if (isRemoval) {
            return state;
        }

        return [{...order, deleted: false, serverTimestamp: timestamp}]
    }

    const index = state.findIndex(o => o.id === order.id);

    if (index === -1) {
        if (isRemoval) {
            return state;
        }

        return [...state,
            {
                ...order,
                deleted: false,
                serverTimestamp: timestamp
            }
        ]
    }

    const prevVersion = state[index]
    if (prevVersion.deleted) {
        return state;
    }

    if (isRemoval) {
        return replaceOrderContent(state, index, {deleted: true})
    }

    // в случае если версия получена из более поздней отметки времени,
    // чем это обновление
    if (prevVersion.serverTimestamp > timestamp) {
        return state;
    }

    return replaceOrderContent(state, index, {...order, serverTimestamp: timestamp})
}

function replaceOrderContent(state: Array<Order>, index: number, replacement: Partial<Order>): Array<Order> {

    const orders = Immutable.asMutable(state, {deep: true});

    return orders.splice(
        index, 1, {...orders[index], ...replacement});
}


function reducer(state: Array<Order>, action: BaseAction) {
    return action.reduce(state);
}

export function useOrders(): [Array<Order>, UserOrderApi] {
    const [value, dispatch] = useReducer(reducer, []);

    const api: UserOrderApi = useMemo(
        () => ({
            orderUpdated(order: Order, timestamp: number) {
                dispatch(new FullUpdateAction(
                    order, timestamp !== undefined ? timestamp : order.timestamp));
            },
            clear() {
                dispatch(new ClearAction());
            },
            createPlaceholder(order: Order) {
                dispatch(new CreatePlaceholderAction(order));
            },
            pendingCancelOrder(id: string, timestamp: number) {
                dispatch(new StateUpdateAction(id, OrderStatus.PENDING_CANCEL, timestamp));
            },
            removePlaceholder() {
                dispatch(new RemovePlaceholderAction());
            },
            updateSnapshot(orders: Array<Order>, timestamp: number) {
                dispatch(new UpdateSnapshotAction(orders, timestamp));
            }
        }),
        []
    )

    return [value, api];
}