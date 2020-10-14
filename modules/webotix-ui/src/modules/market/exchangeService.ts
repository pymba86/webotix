import {del, get, put} from "../common/fetch";
import {Coin, Exchange, PartialServerCoin, ServerCoin} from "./types";
import {CoinMetadata} from "../server";

class ExchangeService {

    async fetchSubscriptions(): Promise<ServerCoin[]> {
        return await get("subscriptions");
    }

    async fetchExchanges(): Promise<Exchange[]> {
        return await get("exchanges");
    }

    async fetchPairs(exchange: string): Promise<PartialServerCoin[]> {
        return await get("exchanges/" + exchange + "/pairs");
    }

    async addSubscription(ticker: ServerCoin): Promise<Response> {
        return await put("subscriptions", JSON.stringify(ticker));
    }

    async removeSubscription(ticker: ServerCoin): Promise<Response> {
        return await del("subscriptions", JSON.stringify(ticker));
    }

    async fetchMetadata(coin: Coin): Promise<CoinMetadata> {
        return await get("exchanges/" + coin.exchange + "/pairs/" + coin.base + "-" + coin.counter);
    }
}

export default new ExchangeService();