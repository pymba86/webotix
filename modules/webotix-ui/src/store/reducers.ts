import {combineReducers} from "redux";
import {coinsReducer} from "./coins/reducer";
import {scriptsReducer} from "./scripts/reducer";

export const reducers = combineReducers({
    coins: coinsReducer,
    scripts: scriptsReducer
});

export type RootState = ReturnType<typeof reducers>;