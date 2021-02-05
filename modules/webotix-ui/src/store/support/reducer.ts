import * as types from "./types"
import {SupportMetadata} from "../../modules/support";
import {SupportActionTypes} from "./types";

export interface SupportState {
    metadata: SupportMetadata;
}

const initialState: SupportState = {
    metadata: {version: "0.0.0"}
};

export function supportReducer(
    state: SupportState = initialState,
    action: SupportActionTypes): SupportState {
    switch (action.type) {
        case types.SET_META:
            return {
                ...state,
                metadata: action.payload.metadata,
            };
        default:
            return state
    }
}
