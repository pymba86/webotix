import {Script} from "../../modules/script/types";
import {ADD_SCRIPT, DELETE_SCRIPT, ScriptsActionTypes,
    SELECT_SCRIPT, UPDATE_SCRIPT} from "./types";

export function selectScript(script: Script): ScriptsActionTypes {
    return {
        type: SELECT_SCRIPT,
        payload: {script}
    }
}

export function addScript(script: Script): ScriptsActionTypes {
    return {
        type: ADD_SCRIPT,
        payload: {script}
    }
}

export function saveOrUpdateScript(script: Script): ScriptsActionTypes {
    return {
        type: UPDATE_SCRIPT,
        payload: {script}
    }
}

export function removeScript(id: string): ScriptsActionTypes {
    return {type: DELETE_SCRIPT}
}