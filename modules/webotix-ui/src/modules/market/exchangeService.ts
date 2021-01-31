import {del, get, put} from "../common/fetch";
import {Coin, Exchange, PartialServerCoin, ServerCoin} from "./types";
import {CoinMetadata} from "../server";
import {CoinPriceList} from "../../store/coins/types";

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

    async fetchReferencePrices(): Promise<CoinPriceList> {
        return await get("subscriptions/referencePrices")
    }

    async setReferencePrice(coin: Coin, price: string | undefined): Promise<Response> {
        return await put(
            "subscriptions/referencePrices/" + coin.exchange + "/" + coin.base + "-" + coin.counter,
            price
        )
    }

    async removeSubscription(ticker: ServerCoin): Promise<Response> {
        return await del("subscriptions", JSON.stringify(ticker));
    }

    async fetchMetadata(coin: Coin): Promise<CoinMetadata> {
        return await get("exchanges/" + coin.exchange + "/pairs/" + coin.base + "-" + coin.counter);
    }
}

export default new ExchangeService();
