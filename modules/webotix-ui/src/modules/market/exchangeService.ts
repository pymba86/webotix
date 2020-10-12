import {get} from "../common/fetch";

class ExchangeService {
    async fetchExchanges(): Promise<Response> {
        return await get("exchanges");
    }

    async fetchPairs(exchange: string): Promise<Response> {
        return await get("exchanges/" + exchange + "/pairs");
    }
}

export default new ExchangeService();