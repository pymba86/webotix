import {Coin} from "../../modules/market";

export const SET_REFERENCE_PRICE = "COINS.SET_REFERENCE_PRICE";
export const SET_REFERENCE_PRICES = "COINS.SET_REFERENCE_PRICES";

export type CoinPriceList = {
    [key: string]: number;
}

export interface SetReferencePriceAction {
    type: typeof SET_REFERENCE_PRICE;
    payload: {
        coin: Coin,
        price: number
    };
}

export interface SetReferencePricesAction {
    type: typeof SET_REFERENCE_PRICES;
    payload: CoinPriceList;
}

export type CoinsActionTypes = SetReferencePriceAction | SetReferencePricesAction;