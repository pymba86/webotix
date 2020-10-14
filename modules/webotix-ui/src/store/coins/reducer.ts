import {CoinPriceList, CoinsActionTypes} from "./types";
import * as types from "./types"

export interface CoinsState {
    referencePrices: CoinPriceList;
}

const initialState: CoinsState = {
    referencePrices: {}
};

export function coinsReducer(
    state: CoinsState = initialState,
    action: CoinsActionTypes): CoinsState {
    switch (action.type) {
        case types.SET_REFERENCE_PRICE:
            return {
                referencePrices: {
                    [action.payload.coin.key]: action.payload.price
                }
            };
        case types.SET_REFERENCE_PRICES:
            return {
               referencePrices: action.payload
            };
        default:
            return state
    }
}