import * as types from "./types"
import {Script} from "../../modules/script/types";
import {ScriptsActionTypes} from "./types";

export interface ScriptsState {
    selectedScript?: Script;
    newScript: boolean;
}

const initialState: ScriptsState = {
    newScript: false
};

export function scriptsReducer(
    state: ScriptsState = initialState,
    action: ScriptsActionTypes): ScriptsState {
    switch (action.type) {
        case types.SELECT_SCRIPT:
            return {
                ...state,
                selectedScript: action.payload.script,
                newScript: false
            };
        case types.ADD_SCRIPT:
            return {
                ...state,
                selectedScript: action.payload.script,
                newScript: true
            };
        case types.DELETE_SCRIPT:
            return {
                ...state,
                selectedScript: undefined,
                newScript: false
            };
        case types.UPDATE_SCRIPT:
            return {
                ...state,
                newScript: false
            };
        default:
            return state
    }
}