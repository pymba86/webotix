import {combineReducers} from "redux";
import {coinsReducer} from "./coins/reducer";

export const reducers = combineReducers({
    coins: coinsReducer
});

export type RootState = ReturnType<typeof reducers>;