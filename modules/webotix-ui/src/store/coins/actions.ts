import {CoinsActionTypes, SET_REFERENCE_PRICE, SET_REFERENCE_PRICES, CoinPriceList} from "./types";
import {Coin} from "../../modules/market";


export function setReferencePrices(list: CoinPriceList): CoinsActionTypes {
    return {
        type: SET_REFERENCE_PRICES,
        payload: list
    }
}

export function setReferencePrice(coin: Coin, price?: number): CoinsActionTypes {
    return {
        type: SET_REFERENCE_PRICE,
        payload: {coin, price}
    }
}
