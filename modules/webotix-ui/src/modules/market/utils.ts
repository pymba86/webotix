import {Coin, PartialServerCoin, ServerCoin} from "./types";

export function coin(exchange: string, counter: string, base: string): Coin {
    return augmentCoin(
        {
            counter: counter,
            base: base
        },
        exchange
    )
}

export function coinFromKey(key: string): Coin {
    const split = key.split("/");
    return augmentCoin(
        {
            counter: split[1],
            base: split[2]
        },
        split[0]
    )
}

export function augmentCoin(p: ServerCoin | PartialServerCoin, exchange?: string): Coin {
    return {
        ...p,
        exchange: exchange ? exchange : (p as ServerCoin).exchange,
        key: (exchange ? exchange : (p as ServerCoin).exchange) + "/" + p.counter + "/" + p.base,
        name: p.base + "/" + p.counter + " (" + exchange + ")",
        shortName: p.base + "/" + p.counter
    }
}

export function coinFromTicker(t: ServerCoin): Coin {
    return augmentCoin(t, t.exchange)
}

export function tickerFromCoin(coin: Coin): ServerCoin {
    return {
        counter: coin.counter,
        base: coin.base,
        exchange: coin.exchange
    }
}