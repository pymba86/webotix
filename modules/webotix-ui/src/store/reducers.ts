import {combineReducers} from "redux";
import {coinsReducer} from "./coins/reducer";
import {scriptsReducer} from "./scripts/reducer";
import {supportReducer} from "./support/reducer";

export const reducers = combineReducers({
    coins: coinsReducer,
    scripts: scriptsReducer,
    support: supportReducer
});

export type RootState = ReturnType<typeof reducers>;
