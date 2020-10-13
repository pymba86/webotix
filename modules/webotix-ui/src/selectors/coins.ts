import {Coin} from "../modules/market";
import {coinFromKey} from "../modules/market/utils";
import * as H from "history";

export const locationToCoin = (location: H.Location): Coin | undefined => {
    if (
        location &&
        location.pathname &&
        location.pathname.startsWith("/coin/") &&
        location.pathname.length > 6
    ) {
        return coinFromKey(location.pathname.substring(6))
    } else {
        return undefined
    }
};