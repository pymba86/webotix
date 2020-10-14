import {CoinsActionTypes, SET_REFERENCE_PRICE} from "./types";
import {Coin} from "../../modules/market";


export function setReferencePrice(coin: Coin, price: number): CoinsActionTypes {
    return {
        type: SET_REFERENCE_PRICE,
        payload: {coin, price}
    }
}