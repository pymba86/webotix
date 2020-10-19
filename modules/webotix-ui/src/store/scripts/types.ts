import {Script} from "../../modules/script/types";

export const SELECT_SCRIPT = "SCRIPTS.SELECT_SCRIPT";
export const ADD_SCRIPT = "SCRIPTS.ADD_SCRIPT";
export const DELETE_SCRIPT = "SCRIPTS.DELETE_SCRIPT";
export const UPDATE_SCRIPT = "SCRIPTS.UPDATE_SCRIPT";

export interface SelectScriptAction {
    type: typeof SELECT_SCRIPT;
    payload: {
        script: Script
    };
}

export interface AddScriptAction {
    type: typeof ADD_SCRIPT;
    payload: {
        script: Script
    };
}

export interface UpdateScriptAction {
    type: typeof UPDATE_SCRIPT;
    payload: {
        script: Script
    };
}

export interface DeleteScriptAction {
    type: typeof DELETE_SCRIPT;
}

export type ScriptsActionTypes = SelectScriptAction | AddScriptAction
    | DeleteScriptAction | UpdateScriptAction;